package com.deep.studenthousing.controller;

import com.deep.studenthousing.entity.Property;
import com.deep.studenthousing.entity.User;
import com.deep.studenthousing.service.PropertyService;
import com.deep.studenthousing.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final PropertyService propertyService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @GetMapping("/update/{id}")
    public String showUpdateUserForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        return "edit-user";
    }

    @PostMapping("/users/edit/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User updatedUser) {
        User user = userService.findById(id);
        if(user != null){
            user.setFullName(updatedUser.getFullName());
            user.setEmail(updatedUser.getEmail());
            user.setPhone(updatedUser.getPhone());
            user.setRole(updatedUser.getRole());
            if(updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()){
                user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }
            userService.save(user);
        }
        return "redirect:/admin/users?success=updated";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, Principal principal) {
        User currentAdmin = userService.findByEmail(principal.getName());

        if (currentAdmin.getId().equals(id)) {
            // Prevent deleting self
            return "redirect:/admin/users?error=selfDelete";
        }
        userService.deleteById(id);
        return "redirect:/admin/users?success=deleted";
    }

}
