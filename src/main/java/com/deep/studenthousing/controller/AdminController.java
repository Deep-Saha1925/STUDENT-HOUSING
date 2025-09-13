package com.deep.studenthousing.controller;

import com.deep.studenthousing.entity.Property;
import com.deep.studenthousing.entity.User;
import com.deep.studenthousing.service.PropertyService;
import com.deep.studenthousing.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final PropertyService propertyService;

    public AdminController(UserService userService, PropertyService propertyService) {
        this.userService = userService;
        this.propertyService = propertyService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        String email = authentication.getName();
        User admin = userService.findByEmail(email);

        model.addAttribute("admin", admin);

        return "admin-dashboard";
    }

    // Users Page
    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin-users";
    }

    // Properties Page
    @GetMapping("/properties")
    public String manageProperties(Model model) {
        model.addAttribute("properties", propertyService.findAll());
        return "admin-properties";
    }
}
