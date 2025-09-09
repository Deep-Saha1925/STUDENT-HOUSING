package com.deep.studenthousing.controller;

import com.deep.studenthousing.entity.User;
import com.deep.studenthousing.entity.Property;
import com.deep.studenthousing.repository.UserRepository;
import com.deep.studenthousing.service.PropertyService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class StudentController {

    private final UserRepository userRepo;
    private final PropertyService propertyService;

    public StudentController(UserRepository userRepo, PropertyService propertyService) {
        this.userRepo = userRepo;
        this.propertyService = propertyService;
    }

    @GetMapping("/student/dashboard")
    public String studentDashboard(Authentication authentication, Model model) {
        // fetch logged-in user
        String email = authentication.getName();
        User user = userRepo.findByEmail(email);

        // fetch all properties
        List<Property> properties = propertyService.findAll();

        // add data to model
        model.addAttribute("user", user);
        model.addAttribute("properties", properties);

        // point to student dashboard template
        return "student-dashboard";   // NOTE: file should be templates/student/dashboard.html
    }
}