package com.deep.studenthousing.controller;

import com.deep.studenthousing.entity.*;
import com.deep.studenthousing.service.AccessGuard;
import com.deep.studenthousing.service.ComplaintService;
import com.deep.studenthousing.service.TextSanitizer;
import com.deep.studenthousing.service.PropertyService;
import com.deep.studenthousing.service.ReportService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

// Everything under /admin/** is already ADMIN-only in SecurityConfig.
@Controller
@RequestMapping("/admin/reports")
public class AdminReportController {

    private final ReportService reportService;
    private final AccessGuard guard;
    private final PropertyService propertyService;
    private final ComplaintService complaintService;

    public AdminReportController(ReportService reportService, AccessGuard guard, PropertyService propertyService,
                                 ComplaintService complaintService) {
        this.reportService = reportService;
        this.guard = guard;
        this.propertyService = propertyService;
        this.complaintService = complaintService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) ReportStatus status, Model model) {
        model.addAttribute("reports", reportService.findAll(status));
        model.addAttribute("statuses", ReportStatus.values());
        model.addAttribute("selected", status);
        return "admin-reports";
    }

    /** Form: admin writes a private message to an owner (optionally about one property or a student's issue). */
    @GetMapping("/new")
    public String newForm(@RequestParam(required = false) Long propertyId,
                          @RequestParam(required = false) Long ownerId,
                          @RequestParam(required = false) Long complaintId,
                          Model model) {
        Complaint complaint = null;
        if (complaintId != null) {
            try { complaint = complaintService.findById(complaintId); }
            catch (IllegalArgumentException ignored) { /* unknown id -> plain form */ }
        }
        final Long wantedProperty = (complaint != null && propertyId == null) ? complaint.getPropertyId() : propertyId;

        List<Property> properties = propertyService.findAll();
        Property chosen = wantedProperty == null ? null
                : properties.stream().filter(p -> p.getId().equals(wantedProperty)).findFirst().orElse(null);
        if (chosen != null && chosen.getOwner() != null) {
            ownerId = chosen.getOwner().getId();
        }
        model.addAttribute("owners", reportService.findOwners());
        model.addAttribute("properties", properties);
        model.addAttribute("types", ReportType.values());
        model.addAttribute("selectedOwnerId", ownerId);
        model.addAttribute("selectedPropertyId", chosen == null ? null : chosen.getId());

        if (complaint != null) {
            model.addAttribute("complaint", complaint);
            model.addAttribute("prefillSubject", TextSanitizer.cut("Issue raised: " + complaint.getSubject(), 150));
            model.addAttribute("prefillMessage", buildComplaintMessage(complaint));
        }
        return "admin-report-new";
    }

    /** Text for the owner. The student's name is shown; contact details in the text are hidden. */
    private String buildComplaintMessage(Complaint c) {
        String details = complaintService.messages(c).stream()
                .filter(m -> !m.isFromAdmin())
                .map(ComplaintMessage::getMessage)
                .findFirst().orElse("");
        String text = "A student has raised an issue about your property"
                + (c.getPropertyTitle() != null ? " \"" + c.getPropertyTitle() + "\"" : "") + ".\n"
                + "Category: " + c.getCategory().getLabel() + "\n"
                + "Raised by: " + c.getStudent().getFullName() + "\n\n"
                + "Details from the student:\n" + TextSanitizer.redact(details) + "\n\n"
                + "Please reply here with your response and the action you will take.";
        return TextSanitizer.cut(text, 2000);
    }

    @PostMapping("/new")
    public String create(@RequestParam(required = false) Long ownerId,
                         @RequestParam(required = false) Long propertyId,
                         @RequestParam ReportType type,
                         @RequestParam String subject,
                         @RequestParam String message,
                         @RequestParam(required = false) Long complaintId,
                         Authentication authentication) {
        User admin = guard.currentUser(authentication);
        try {
            Report r = reportService.createFromAdmin(admin, ownerId, propertyId, type, subject, message);
            if (complaintId != null) {
                complaintService.markForwarded(complaintId, r.getId(), admin);   // tells the student too
            }
            return "redirect:/admin/reports/" + r.getId() + "?success=sent";
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/reports/new?error=invalid"
                    + (propertyId != null ? "&propertyId=" + propertyId : "")
                    + (complaintId != null ? "&complaintId=" + complaintId : "");
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Report report = reportService.findById(id);
        reportService.markAdminRead(report);

        model.addAttribute("report", report);
        model.addAttribute("messages", reportService.messages(report));
        // Messages the admin started only use three states: awaiting owner / owner replied / resolved.
        model.addAttribute("statuses", report.isStartedByAdmin()
                ? List.of(ReportStatus.NEED_INFO, ReportStatus.UNDER_REVIEW, ReportStatus.APPROVED)
                : Arrays.asList(ReportStatus.values()));
        model.addAttribute("admin", true);
        return "report-detail";
    }

    @PostMapping("/{id}/reply")
    public String reply(@PathVariable Long id,
                        @RequestParam(required = false, defaultValue = "") String message,
                        @RequestParam ReportStatus status,
                        Authentication authentication) {
        User admin = guard.currentUser(authentication);
        Report report = reportService.findById(id);
        message = message.trim();

        if (message.length() > 2000) {
            return "redirect:/admin/reports/" + id + "?error=invalid";
        }
        if (report.isStartedByAdmin() && (status == ReportStatus.NEW || status == ReportStatus.REJECTED)) {
            return "redirect:/admin/reports/" + id + "?error=invalid";
        }
        // Can't go back to "New", and asking for info / rejecting needs a written reason.
        if (status == ReportStatus.NEW && report.getStatus() != ReportStatus.NEW) {
            return "redirect:/admin/reports/" + id + "?error=invalid";
        }
        if ((status == ReportStatus.NEED_INFO || status == ReportStatus.REJECTED)
                && status != report.getStatus() && message.isEmpty()) {
            return "redirect:/admin/reports/" + id + "?error=needText";
        }
        // A plain reply on a brand-new report means the admin has started reviewing it.
        if (status == ReportStatus.NEW && !message.isEmpty()) {
            status = ReportStatus.UNDER_REVIEW;
        }
        reportService.adminReply(report, admin, message, status);
        return "redirect:/admin/reports/" + id + "?success=sent";
    }
}
