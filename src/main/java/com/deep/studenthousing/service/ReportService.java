package com.deep.studenthousing.service;

import com.deep.studenthousing.entity.*;
import com.deep.studenthousing.exception.UnauthorizedActionException;
import com.deep.studenthousing.repository.PropertyRepository;
import com.deep.studenthousing.repository.ReportMessageRepository;
import com.deep.studenthousing.repository.ReportRepository;
import com.deep.studenthousing.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportMessageRepository messageRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public ReportService(ReportRepository reportRepository,
                         ReportMessageRepository messageRepository,
                         PropertyRepository propertyRepository,
                         UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.messageRepository = messageRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    // Abuse limits. Anyone can self-register as an OWNER, so owner input is untrusted:
    // without caps one account could flood the admin inbox and the database.
    private static final int MAX_REPORTS_PER_HOUR = 5;
    private static final int MAX_REPORTS_PER_DAY = 15;
    private static final int MAX_MESSAGES_PER_REPORT = 100;
    private static final int MAX_SUBJECT = 150;
    private static final int MAX_TEXT = 2000;

    /** Thrown when a rate/size limit is hit; controllers turn it into a friendly message. */
    public static class LimitExceededException extends RuntimeException {
        public LimitExceededException(String message) { super(message); }
    }

    /** Drops control characters (e.g. a NUL byte makes PostgreSQL reject the row) but keeps line breaks. */
    private static String clean(String s) {
        return s == null ? "" : s.replaceAll("[\\p{Cntrl}&&[^\\n\\r\\t]]", "").trim();
    }

    // ---------- reads ----------

    public List<Report> findForOwner(User owner) {
        return reportRepository.findByOwnerOrderByUpdatedAtDesc(owner);
    }

    public List<Report> findAll(ReportStatus status) {
        return status == null
                ? reportRepository.findAllByOrderByUpdatedAtDesc()
                : reportRepository.findByStatusOrderByUpdatedAtDesc(status);
    }

    public Report findById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found."));
    }

    /** Owner may only open their own reports. */
    public Report findForOwnerById(Long id, User owner) {
        // Same answer for "doesn't exist" and "belongs to someone else", so ids can't be probed.
        return reportRepository.findById(id)
                .filter(r -> r.getOwner().getId().equals(owner.getId()))
                .orElseThrow(() -> new UnauthorizedActionException("You can only view your own reports."));
    }

    public List<ReportMessage> messages(Report report) {
        return messageRepository.findByReportOrderByCreatedAtAsc(report);
    }

    public long countAdminUnread() {
        return reportRepository.countByAdminUnreadTrue();
    }

    public long countOwnerUnread(User owner) {
        return reportRepository.countByOwnerAndOwnerUnreadTrue(owner);
    }

    public List<User> findOwners() {
        return userRepository.findByRoleOrderByFullNameAsc(Role.OWNER);
    }

    // ---------- writes ----------

    /**
     * Admin starts a private conversation with an owner (optionally about one property).
     * Reuses the report thread, so the owner sees it in "My Reports" with an unread badge.
     */
    @Transactional
    public Report createFromAdmin(User admin, Long ownerId, Long propertyId,
                                  ReportType type, String subject, String message) {
        subject = clean(subject);
        message = clean(message);
        if (subject.isEmpty() || message.isEmpty() || subject.length() > MAX_SUBJECT || message.length() > MAX_TEXT) {
            throw new IllegalArgumentException("Subject and message are required (max " + MAX_SUBJECT + " / " + MAX_TEXT + " characters).");
        }

        Property property = null;
        if (propertyId != null) {
            property = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new IllegalArgumentException("Property not found."));
            if (ownerId == null && property.getOwner() != null) {
                ownerId = property.getOwner().getId();   // property chosen first -> its owner
            }
        }
        if (ownerId == null) throw new IllegalArgumentException("Please choose an owner.");

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found."));
        if (owner.getRole() != Role.OWNER) {
            throw new IllegalArgumentException("That user is not an owner.");
        }
        if (property != null && (property.getOwner() == null || !property.getOwner().getId().equals(owner.getId()))) {
            throw new IllegalArgumentException("That property does not belong to the chosen owner.");
        }

        Report r = new Report();
        r.setOwner(owner);
        r.setType(type);
        r.setSubject(subject);
        r.setStartedByAdmin(true);
        r.setStatus(ReportStatus.NEED_INFO);   // shown as "Awaiting owner reply"
        r.setAdminUnread(false);
        r.setOwnerUnread(true);
        if (property != null) {
            r.setPropertyId(property.getId());
            r.setPropertyTitle(property.getTitle());
        }
        reportRepository.save(r);
        addMessage(r, admin, true, message, ReportStatus.NEED_INFO);
        return r;
    }

    @Transactional
    public Report create(User owner, ReportType type, Long propertyId, String subject, String description) {
        subject = clean(subject);
        description = clean(description);
        if (subject.isEmpty() || description.isEmpty() || subject.length() > MAX_SUBJECT || description.length() > MAX_TEXT) {
            throw new IllegalArgumentException("Subject and details are required (max " + MAX_SUBJECT + " / " + MAX_TEXT + " characters).");
        }
        LocalDateTime now = LocalDateTime.now();
        if (reportRepository.countByOwnerAndCreatedAtAfter(owner, now.minusHours(1)) >= MAX_REPORTS_PER_HOUR
                || reportRepository.countByOwnerAndCreatedAtAfter(owner, now.minusDays(1)) >= MAX_REPORTS_PER_DAY) {
            throw new LimitExceededException("Too many reports sent recently. Please wait before sending another.");
        }

        Report r = new Report();
        r.setOwner(owner);
        r.setType(type);
        r.setSubject(subject);

        if (propertyId != null) {
            Property p = propertyRepository.findById(propertyId).orElse(null);
            // Only accept a property that really belongs to this owner.
            if (p != null && p.getOwner() != null && p.getOwner().getId().equals(owner.getId())) {
                r.setPropertyId(p.getId());
                r.setPropertyTitle(p.getTitle());
            }
        }
        reportRepository.save(r);
        addMessage(r, owner, false, description, null);
        return r;
    }

    @Transactional
    public void ownerReply(Report report, User owner, String text) {
        if (report.getStatus().isClosed()) {
            throw new IllegalArgumentException("This report is closed.");
        }
        if (messageRepository.countByReport(report) >= MAX_MESSAGES_PER_REPORT) {
            throw new LimitExceededException("This conversation is too long. Please start a new report.");
        }
        addMessage(report, owner, false, text, null);
        // The owner answered a "Need info" request -> back to the admin's queue.
        if (report.getStatus() == ReportStatus.NEED_INFO) {
            report.setStatus(ReportStatus.UNDER_REVIEW);
        }
        report.setAdminUnread(true);
        report.setUpdatedAt(LocalDateTime.now());
        reportRepository.save(report);
    }

    @Transactional
    public void adminReply(Report report, User admin, String text, ReportStatus newStatus) {
        boolean statusChanged = newStatus != null && newStatus != report.getStatus();
        boolean hasText = text != null && !text.isBlank();
        if (!statusChanged && !hasText) return;

        addMessage(report, admin, true, hasText ? text : "", statusChanged ? newStatus : null);
        if (statusChanged) report.setStatus(newStatus);
        report.setOwnerUnread(true);
        report.setUpdatedAt(LocalDateTime.now());
        reportRepository.save(report);
    }

    /** Called after an approved report was turned into a public notice. */
    @Transactional
    public void markNoticePublished(Long reportId, User admin) {
        reportRepository.findById(reportId).ifPresent(r -> {
            if (r.getStatus() != ReportStatus.APPROVED || r.isNoticePublished()) return;
            r.setNoticePublished(true);
            addMessage(r, admin, true, "This report has been published as a public notice.", null);
            r.setOwnerUnread(true);
            r.setUpdatedAt(LocalDateTime.now());
            reportRepository.save(r);
        });
    }

    @Transactional
    public void markAdminRead(Report report) {
        if (report.isAdminUnread()) {
            report.setAdminUnread(false);
            reportRepository.save(report);
        }
    }

    @Transactional
    public void markOwnerRead(Report report) {
        if (report.isOwnerUnread()) {
            report.setOwnerUnread(false);
            reportRepository.save(report);
        }
    }

    private void addMessage(Report report, User sender, boolean fromAdmin, String text, ReportStatus statusAfter) {
        ReportMessage m = new ReportMessage();
        m.setReport(report);
        m.setSender(sender);
        m.setFromAdmin(fromAdmin);
        String body = clean(text);
        if (body.length() > MAX_TEXT) {
            throw new IllegalArgumentException("Message is too long (max " + MAX_TEXT + " characters).");
        }
        m.setMessage(body);
        m.setStatusAfter(statusAfter);
        messageRepository.save(m);
    }
}
