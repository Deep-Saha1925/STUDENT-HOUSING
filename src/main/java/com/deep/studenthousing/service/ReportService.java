package com.deep.studenthousing.service;

import com.deep.studenthousing.entity.*;
import com.deep.studenthousing.exception.UnauthorizedActionException;
import com.deep.studenthousing.repository.PropertyRepository;
import com.deep.studenthousing.repository.ReportMessageRepository;
import com.deep.studenthousing.repository.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportMessageRepository messageRepository;
    private final PropertyRepository propertyRepository;

    public ReportService(ReportRepository reportRepository,
                         ReportMessageRepository messageRepository,
                         PropertyRepository propertyRepository) {
        this.reportRepository = reportRepository;
        this.messageRepository = messageRepository;
        this.propertyRepository = propertyRepository;
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
        Report r = findById(id);
        if (!r.getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedActionException("You can only view your own reports.");
        }
        return r;
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

    // ---------- writes ----------

    @Transactional
    public Report create(User owner, ReportType type, Long propertyId, String subject, String description) {
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
        m.setMessage(text == null ? "" : text.trim());
        m.setStatusAfter(statusAfter);
        messageRepository.save(m);
    }
}
