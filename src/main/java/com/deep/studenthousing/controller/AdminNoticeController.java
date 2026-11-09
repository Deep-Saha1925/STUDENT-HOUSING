package com.deep.studenthousing.controller;

import com.deep.studenthousing.entity.*;
import com.deep.studenthousing.service.NoticeService;
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

    public AdminNoticeController(NoticeService noticeService, UserService userService) {
        this.noticeService = noticeService;
        this.userService = userService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("notices", noticeService.findAll());
        model.addAttribute("severities", NoticeSeverity.values());
        model.addAttribute("audiences", NoticeAudience.values());
        return "admin-notices";
    }

    @PostMapping
    public String create(@RequestParam String title,
                         @RequestParam String message,
                         @RequestParam NoticeSeverity severity,
                         @RequestParam NoticeAudience audience,
                         @RequestParam(required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expiresAt,
                         Authentication authentication) {

        title = title.trim();
        message = message.trim();
        if (title.isEmpty() || message.isEmpty() || title.length() > 150 || message.length() > 1000) {
            return "redirect:/admin/notices?error=invalid";
        }
        if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) {
            return "redirect:/admin/notices?error=pastDate";
        }

        Notice n = new Notice();
        n.setTitle(title);
        n.setMessage(message);
        n.setSeverity(severity);
        n.setAudience(audience);
        n.setExpiresAt(expiresAt);
        n.setCreatedBy(userService.findByEmail(authentication.getName()));
        noticeService.save(n);
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
}
