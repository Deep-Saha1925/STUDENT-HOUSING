package com.deep.studenthousing.controller;

import com.deep.studenthousing.entity.*;
import com.deep.studenthousing.service.AccessGuard;
import com.deep.studenthousing.service.ReportService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// Everything under /admin/** is already ADMIN-only in SecurityConfig.
@Controller
@RequestMapping("/admin/reports")
public class AdminReportController {

    private final ReportService reportService;
    private final AccessGuard guard;

    public AdminReportController(ReportService reportService, AccessGuard guard) {
        this.reportService = reportService;
        this.guard = guard;
    }

    @GetMapping
    public String list(@RequestParam(required = false) ReportStatus status, Model model) {
        model.addAttribute("reports", reportService.findAll(status));
        model.addAttribute("statuses", ReportStatus.values());
        model.addAttribute("selected", status);
        return "admin-reports";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Report report = reportService.findById(id);
        reportService.markAdminRead(report);

        model.addAttribute("report", report);
        model.addAttribute("messages", reportService.messages(report));
        model.addAttribute("statuses", ReportStatus.values());
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
