package com.deep.studenthousing.controller;

import com.deep.studenthousing.entity.Property;
import com.deep.studenthousing.entity.User;
import com.deep.studenthousing.service.PropertyService;
import com.deep.studenthousing.service.UserService;
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
    public String dashboard(Model model) {
        List<User> allUsers = userService.findAll();
        List<Property> allProperties = propertyService.findAll();

        model.addAttribute("users", allUsers);
        model.addAttribute("properties", allProperties);

        return "admin-dashboard";
    }

}
