package com.deep.studenthousing.service;

import com.deep.studenthousing.entity.*;
import com.deep.studenthousing.exception.UnauthorizedActionException;
import com.deep.studenthousing.repository.BookingRepository;
import com.deep.studenthousing.repository.ComplaintMessageRepository;
import com.deep.studenthousing.repository.ComplaintRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

@Service
public class ComplaintService {

    // Abuse limits (students sign up freely, so their input is untrusted too).
    private static final int MAX_PER_DAY = 3;
    private static final int MAX_MESSAGES = 100;
    private static final int MAX_SUBJECT = 150;
    private static final int MAX_TEXT = 2000;

    public static class LimitExceededException extends RuntimeException {
        public LimitExceededException(String m) { super(m); }
    }
    /** The student already has an unfinished issue about this property. */
    public static class AlreadyOpenException extends RuntimeException {
        public AlreadyOpenException(String m) { super(m); }
    }

    private final ComplaintRepository complaintRepository;
    private final ComplaintMessageRepository messageRepository;
    private final BookingRepository bookingRepository;

    public ComplaintService(ComplaintRepository complaintRepository,
                            ComplaintMessageRepository messageRepository,
                            BookingRepository bookingRepository) {
        this.complaintRepository = complaintRepository;
        this.messageRepository = messageRepository;
        this.bookingRepository = bookingRepository;
    }

    // ---------- reads ----------

    /** Only approved (confirmed) bookings give the right to raise an issue. */
    public List<Booking> eligibleBookings(User student) {
        return bookingRepository.findByStudentOrderByStartDateDesc(student).stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED && b.getProperty() != null)
                .toList();
    }

    public List<Complaint> findForStudent(User student) {
        return complaintRepository.findByStudentOrderByUpdatedAtDesc(student);
    }

    public List<Complaint> findAll(ComplaintStatus status) {
        return status == null
                ? complaintRepository.findAllByOrderByUpdatedAtDesc()
                : complaintRepository.findByStatusOrderByUpdatedAtDesc(status);
    }

    public Complaint findById(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Issue not found."));
    }

    /** Same answer for "missing" and "someone else's", so ids can't be probed. */
    public Complaint findForStudentById(Long id, User student) {
        return complaintRepository.findById(id)
                .filter(c -> c.getStudent().getId().equals(student.getId()))
                .orElseThrow(() -> new UnauthorizedActionException("You can only view your own issues."));
    }

    public List<ComplaintMessage> messages(Complaint c) {
        return messageRepository.findByComplaintOrderByCreatedAtAsc(c);
    }

    public long countAdminUnread() { return complaintRepository.countByAdminUnreadTrue(); }

    public long countStudentUnread(User student) {
        return complaintRepository.countByStudentAndStudentUnreadTrue(student);
    }

    // ---------- writes ----------

    @Transactional
    public Complaint create(User student, Long bookingId, ComplaintCategory category,
                            String subject, String details) {
        subject = TextSanitizer.clean(subject);
        details = TextSanitizer.clean(details);
        if (subject.isEmpty() || details.isEmpty() || subject.length() > MAX_SUBJECT || details.length() > MAX_TEXT) {
            throw new IllegalArgumentException("Subject and details are required.");
        }
        if (complaintRepository.countByStudentAndCreatedAtAfter(student, LocalDateTime.now().minusDays(1)) >= MAX_PER_DAY) {
            throw new LimitExceededException("Too many issues raised today.");
        }
        // The booking must be this student's own, and approved. The property comes from it,
        // never from the form, so nobody can raise an issue about a place they never booked.
        Booking booking = bookingRepository.findById(bookingId)
                .filter(b -> b.getStudent() != null && b.getStudent().getId().equals(student.getId()))
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED && b.getProperty() != null)
                .orElseThrow(() -> new IllegalArgumentException("Choose one of your approved bookings."));
        Property property = booking.getProperty();

        if (complaintRepository.existsByStudentAndPropertyIdAndStatusIn(student, property.getId(),
                EnumSet.of(ComplaintStatus.NEW, ComplaintStatus.UNDER_REVIEW, ComplaintStatus.SENT_TO_OWNER))) {
            throw new AlreadyOpenException("You already have an open issue for this property.");
        }

        Complaint c = new Complaint();
        c.setStudent(student);
        c.setBookingId(booking.getId());
        c.setPropertyId(property.getId());
        c.setPropertyTitle(property.getTitle());
        c.setCategory(category);
        c.setSubject(subject);
        complaintRepository.save(c);
        addMessage(c, student, false, details, null);
        return c;
    }

    @Transactional
    public void studentReply(Complaint c, User student, String text) {
        if (c.getStatus().isClosed()) throw new IllegalArgumentException("This issue is closed.");
        if (messageRepository.countByComplaint(c) >= MAX_MESSAGES) {
            throw new LimitExceededException("This conversation is too long.");
        }
        addMessage(c, student, false, text, null);
        c.setAdminUnread(true);
        c.setUpdatedAt(LocalDateTime.now());
        complaintRepository.save(c);
    }

    @Transactional
    public void adminReply(Complaint c, User admin, String text, ComplaintStatus newStatus) {
        boolean statusChanged = newStatus != null && newStatus != c.getStatus();
        boolean hasText = text != null && !text.isBlank();
        if (!statusChanged && !hasText) return;

        addMessage(c, admin, true, hasText ? text : "", statusChanged ? newStatus : null);
        if (statusChanged) c.setStatus(newStatus);
        c.setStudentUnread(true);
        c.setUpdatedAt(LocalDateTime.now());
        complaintRepository.save(c);
    }

    /** Called after the admin sent this issue to the owner (a Step 3a message). */
    @Transactional
    public void markForwarded(Long complaintId, Long reportId, User admin) {
        complaintRepository.findById(complaintId).ifPresent(c -> {
            c.setForwardedReportId(reportId);
            if (!c.getStatus().isClosed()) {
                c.setStatus(ComplaintStatus.SENT_TO_OWNER);
                addMessage(c, admin, true,
                        "We have sent your issue to the property owner. We will update you here.",
                        ComplaintStatus.SENT_TO_OWNER);
            }
            c.setStudentUnread(true);
            c.setUpdatedAt(LocalDateTime.now());
            complaintRepository.save(c);
        });
    }

    @Transactional
    public void markAdminRead(Complaint c) {
        if (c.isAdminUnread()) { c.setAdminUnread(false); complaintRepository.save(c); }
    }

    @Transactional
    public void markStudentRead(Complaint c) {
        if (c.isStudentUnread()) { c.setStudentUnread(false); complaintRepository.save(c); }
    }

    private void addMessage(Complaint c, User sender, boolean fromAdmin, String text, ComplaintStatus statusAfter) {
        String body = TextSanitizer.clean(text);
        if (body.length() > MAX_TEXT) throw new IllegalArgumentException("Message is too long.");
        ComplaintMessage m = new ComplaintMessage();
        m.setComplaint(c);
        m.setSender(sender);
        m.setFromAdmin(fromAdmin);
        m.setMessage(body);
        m.setStatusAfter(statusAfter);
        messageRepository.save(m);
    }
}
