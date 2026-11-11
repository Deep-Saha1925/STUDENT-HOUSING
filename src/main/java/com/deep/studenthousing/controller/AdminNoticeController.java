package com.deep.studenthousing.controller;

import com.deep.studenthousing.entity.*;
import com.deep.studenthousing.service.NoticeService;
import com.deep.studenthousing.service.ReportService;
import com.deep.studenthousing.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

// Everything under /admin/** is already ADMIN-only in SecurityConfig.
@Controller
@RequestMapping("/admin/notices")
public class AdminNoticeController {

    private final NoticeService noticeService;
    private final UserService userService;
    private final ReportService reportService;

    public AdminNoticeController(NoticeService noticeService, UserService userService, ReportService reportService) {
        this.noticeService = noticeService;
        this.userService = userService;
        this.reportService = reportService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) Long fromReport, Model model) {
        model.addAttribute("notices", noticeService.findAll());
        model.addAttribute("severities", NoticeSeverity.values());
        model.addAttribute("audiences", NoticeAudience.values());

        // "Create notice from this report": pre-fill the form; the admin edits before publishing.
        if (fromReport != null) {
            try {
                Report r = reportService.findById(fromReport);
                if (r.getStatus() == ReportStatus.APPROVED && !r.isNoticePublished()) {
                    String body = reportService.messages(r).stream()
                            .filter(m -> !m.isFromAdmin())
                            .map(ReportMessage::getMessage)
                            .findFirst().orElse("");
                    if (r.getPropertyTitle() != null) body = r.getPropertyTitle() + ": " + body;

                    boolean serious = r.getType() == ReportType.MAINTENANCE
                            || r.getType() == ReportType.PROPERTY_ISSUE;

                    model.addAttribute("fromReport", r);
                    model.addAttribute("prefillTitle", cut(r.getSubject(), 150));
                    model.addAttribute("prefillMessage", cut(body, 1000));
                    model.addAttribute("prefillSeverity", serious ? NoticeSeverity.WARNING : NoticeSeverity.INFO);
                    model.addAttribute("prefillAudience", NoticeAudience.ALL);
                }
            } catch (IllegalArgumentException ignored) {
                // unknown report id -> just show the normal empty form
            }
        }
        return "admin-notices";
    }

    @PostMapping
    public String create(@RequestParam String title,
                         @RequestParam String message,
                         @RequestParam NoticeSeverity severity,
                         @RequestParam NoticeAudience audience,
                         @RequestParam(required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expiresAt,
                         @RequestParam(required = false) Long reportId,
                         Authentication authentication) {

        String back = reportId != null ? "&fromReport=" + reportId : "";
        title = title.trim();
        message = message.trim();
        if (title.isEmpty() || message.isEmpty() || title.length() > 150 || message.length() > 1000) {
            return "redirect:/admin/notices?error=invalid" + back;
        }
        if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) {
            return "redirect:/admin/notices?error=pastDate" + back;
        }

        User admin = userService.findByEmail(authentication.getName());

        Notice n = new Notice();
        n.setTitle(title);
        n.setMessage(message);
        n.setSeverity(severity);
        n.setAudience(audience);
        n.setExpiresAt(expiresAt);
        n.setCreatedBy(admin);
        noticeService.save(n);

        // If this notice came from a report, mark it (the service only accepts APPROVED reports).
        if (reportId != null) {
            reportService.markNoticePublished(reportId, admin);
        }
        return "redirect:/admin/notices?success=created";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id) {
        noticeService.toggle(id);
        return "redirect:/admin/notices?success=updated";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        noticeService.delete(id);
        return "redirect:/admin/notices?success=deleted";
    }

    private static String cut(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
