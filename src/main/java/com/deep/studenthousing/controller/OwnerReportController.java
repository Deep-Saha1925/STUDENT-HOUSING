package com.deep.studenthousing.controller;

import com.deep.studenthousing.entity.*;
import com.deep.studenthousing.service.AccessGuard;
import com.deep.studenthousing.service.ReportService;
import com.deep.studenthousing.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// /owner/** is OWNER-only in SecurityConfig.
@Controller
@RequestMapping("/owner/reports")
public class OwnerReportController {

    private final ReportService reportService;
    private final UserService userService;
    private final AccessGuard guard;

    public OwnerReportController(ReportService reportService, UserService userService, AccessGuard guard) {
        this.reportService = reportService;
        this.userService = userService;
        this.guard = guard;
    }

    @GetMapping
    public String list(Model model, Authentication authentication) {
        User owner = guard.currentUser(authentication);
        model.addAttribute("owner", owner);
        model.addAttribute("reports", reportService.findForOwner(owner));
        model.addAttribute("types", ReportType.values());
        model.addAttribute("properties", userService.findByOwner(owner));
        return "owner-reports";
    }

    @PostMapping
    public String create(@RequestParam ReportType type,
                         @RequestParam(required = false) Long propertyId,
                         @RequestParam String subject,
                         @RequestParam String description,
                         Authentication authentication) {
        User owner = guard.currentUser(authentication);
        subject = subject.trim();
        description = description.trim();
        if (subject.isEmpty() || description.isEmpty() || subject.length() > 150 || description.length() > 2000) {
            return "redirect:/owner/reports?error=invalid";
        }
        Report r = reportService.create(owner, type, propertyId, subject, description);
        return "redirect:/owner/reports/" + r.getId() + "?success=created";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, Authentication authentication) {
        User owner = guard.currentUser(authentication);
        Report report = reportService.findForOwnerById(id, owner);
        reportService.markOwnerRead(report);

        model.addAttribute("report", report);
        model.addAttribute("messages", reportService.messages(report));
        model.addAttribute("admin", false);
        model.addAttribute("owner", owner);
        return "report-detail";
    }

    @PostMapping("/{id}/reply")
    public String reply(@PathVariable Long id, @RequestParam String message, Authentication authentication) {
        User owner = guard.currentUser(authentication);
        Report report = reportService.findForOwnerById(id, owner);
        message = message.trim();
        if (message.isEmpty() || message.length() > 2000) {
            return "redirect:/owner/reports/" + id + "?error=invalid";
        }
        if (report.getStatus().isClosed()) {
            return "redirect:/owner/reports/" + id + "?error=closed";
        }
        reportService.ownerReply(report, owner, message);
        return "redirect:/owner/reports/" + id + "?success=sent";
    }
}
