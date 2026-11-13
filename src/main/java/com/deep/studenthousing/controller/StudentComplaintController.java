package com.deep.studenthousing.controller;

import com.deep.studenthousing.entity.*;
import com.deep.studenthousing.service.AccessGuard;
import com.deep.studenthousing.service.ComplaintService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// /student/** is STUDENT-only in SecurityConfig.
@Controller
@RequestMapping("/student/complaints")
public class StudentComplaintController {

    private final ComplaintService complaintService;
    private final AccessGuard guard;

    public StudentComplaintController(ComplaintService complaintService, AccessGuard guard) {
        this.complaintService = complaintService;
        this.guard = guard;
    }

    @GetMapping
    public String list(Model model, Authentication authentication) {
        User student = guard.currentUser(authentication);
        model.addAttribute("complaints", complaintService.findForStudent(student));
        model.addAttribute("bookings", complaintService.eligibleBookings(student));
        model.addAttribute("categories", ComplaintCategory.values());
        return "student-complaints";
    }

    @PostMapping
    public String create(@RequestParam Long bookingId,
                         @RequestParam ComplaintCategory category,
                         @RequestParam String subject,
                         @RequestParam String description,
                         Authentication authentication) {
        User student = guard.currentUser(authentication);
        try {
            Complaint c = complaintService.create(student, bookingId, category, subject, description);
            return "redirect:/student/complaints/" + c.getId() + "?success=created";
        } catch (ComplaintService.LimitExceededException e) {
            return "redirect:/student/complaints?error=limit";
        } catch (ComplaintService.AlreadyOpenException e) {
            return "redirect:/student/complaints?error=open";
        } catch (IllegalArgumentException e) {
            return "redirect:/student/complaints?error=invalid";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, Authentication authentication) {
        User student = guard.currentUser(authentication);
        Complaint c = complaintService.findForStudentById(id, student);
        complaintService.markStudentRead(c);
        model.addAttribute("complaint", c);
        model.addAttribute("messages", complaintService.messages(c));
        model.addAttribute("admin", false);
        return "complaint-detail";
    }

    @PostMapping("/{id}/reply")
    public String reply(@PathVariable Long id, @RequestParam String message, Authentication authentication) {
        User student = guard.currentUser(authentication);
        Complaint c = complaintService.findForStudentById(id, student);
        message = message.trim();
        if (message.isEmpty() || message.length() > 2000) {
            return "redirect:/student/complaints/" + id + "?error=invalid";
        }
        if (c.getStatus().isClosed()) {
            return "redirect:/student/complaints/" + id + "?error=closed";
        }
        try {
            complaintService.studentReply(c, student, message);
        } catch (ComplaintService.LimitExceededException e) {
            return "redirect:/student/complaints/" + id + "?error=limit";
        } catch (IllegalArgumentException e) {
            return "redirect:/student/complaints/" + id + "?error=invalid";
        }
        return "redirect:/student/complaints/" + id + "?success=sent";
    }
}
