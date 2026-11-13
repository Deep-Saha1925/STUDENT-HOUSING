package com.deep.studenthousing.controller;

import com.deep.studenthousing.entity.*;
import com.deep.studenthousing.service.AccessGuard;
import com.deep.studenthousing.service.ComplaintService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// Everything under /admin/** is already ADMIN-only in SecurityConfig.
@Controller
@RequestMapping("/admin/complaints")
public class AdminComplaintController {

    private final ComplaintService complaintService;
    private final AccessGuard guard;

    public AdminComplaintController(ComplaintService complaintService, AccessGuard guard) {
        this.complaintService = complaintService;
        this.guard = guard;
    }

    @GetMapping
    public String list(@RequestParam(required = false) ComplaintStatus status, Model model) {
        model.addAttribute("complaints", complaintService.findAll(status));
        model.addAttribute("statuses", ComplaintStatus.values());
        model.addAttribute("selected", status);
        return "admin-complaints";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Complaint c = complaintService.findById(id);
        complaintService.markAdminRead(c);
        model.addAttribute("complaint", c);
        model.addAttribute("messages", complaintService.messages(c));
        model.addAttribute("statuses", ComplaintStatus.values());
        model.addAttribute("admin", true);
        return "complaint-detail";
    }

    @PostMapping("/{id}/reply")
    public String reply(@PathVariable Long id,
                        @RequestParam(required = false, defaultValue = "") String message,
                        @RequestParam ComplaintStatus status,
                        Authentication authentication) {
        User admin = guard.currentUser(authentication);
        Complaint c = complaintService.findById(id);
        message = message.trim();

        if (message.length() > 2000) {
            return "redirect:/admin/complaints/" + id + "?error=invalid";
        }
        // "New" and "Sent to owner" can't be picked by hand (the second happens when you send it to the owner).
        if ((status == ComplaintStatus.NEW || status == ComplaintStatus.SENT_TO_OWNER) && status != c.getStatus()) {
            return "redirect:/admin/complaints/" + id + "?error=invalid";
        }
        // Dismissing needs a reason the student can read.
        if (status == ComplaintStatus.DISMISSED && status != c.getStatus() && message.isEmpty()) {
            return "redirect:/admin/complaints/" + id + "?error=needText";
        }
        // A plain reply on a brand-new issue means the admin has started reviewing it.
        if (status == ComplaintStatus.NEW && !message.isEmpty()) {
            status = ComplaintStatus.UNDER_REVIEW;
        }
        complaintService.adminReply(c, admin, message, status);
        return "redirect:/admin/complaints/" + id + "?success=sent";
    }
}
