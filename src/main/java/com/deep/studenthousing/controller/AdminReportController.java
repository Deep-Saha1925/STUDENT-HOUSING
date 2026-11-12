package com.deep.studenthousing.controller;

import com.deep.studenthousing.entity.*;
import com.deep.studenthousing.service.AccessGuard;
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

    public AdminReportController(ReportService reportService, AccessGuard guard, PropertyService propertyService) {
        this.reportService = reportService;
        this.guard = guard;
        this.propertyService = propertyService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) ReportStatus status, Model model) {
        model.addAttribute("reports", reportService.findAll(status));
        model.addAttribute("statuses", ReportStatus.values());
        model.addAttribute("selected", status);
        return "admin-reports";
    }

    /** Form: admin writes a private message to an owner (optionally about one property). */
    @GetMapping("/new")
    public String newForm(@RequestParam(required = false) Long propertyId,
                          @RequestParam(required = false) Long ownerId,
                          Model model) {
        List<Property> properties = propertyService.findAll();
        Property chosen = propertyId == null ? null
                : properties.stream().filter(p -> p.getId().equals(propertyId)).findFirst().orElse(null);
        if (chosen != null && chosen.getOwner() != null) {
            ownerId = chosen.getOwner().getId();
        }
        model.addAttribute("owners", reportService.findOwners());
        model.addAttribute("properties", properties);
        model.addAttribute("types", ReportType.values());
        model.addAttribute("selectedOwnerId", ownerId);
        model.addAttribute("selectedPropertyId", chosen == null ? null : chosen.getId());
        return "admin-report-new";
    }

    @PostMapping("/new")
    public String create(@RequestParam(required = false) Long ownerId,
                         @RequestParam(required = false) Long propertyId,
                         @RequestParam ReportType type,
                         @RequestParam String subject,
                         @RequestParam String message,
                         Authentication authentication) {
        User admin = guard.currentUser(authentication);
        try {
            Report r = reportService.createFromAdmin(admin, ownerId, propertyId, type, subject, message);
            return "redirect:/admin/reports/" + r.getId() + "?success=sent";
        } catch (IllegalArgumentException e) {
            return "redirect:/admin/reports/new?error=invalid" + (propertyId != null ? "&propertyId=" + propertyId : "");
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
