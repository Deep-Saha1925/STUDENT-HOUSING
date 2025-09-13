package com.deep.studenthousing.controller;

import com.deep.studenthousing.entity.Role;
import com.deep.studenthousing.entity.User;
import com.deep.studenthousing.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private PasswordEncoder passwordEncoder;
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // registration form
    @GetMapping("/register")
    public String registerForm(Model model, Authentication authentication) {

        if(authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))){

            model.addAttribute("user", new User());
            return "register";
        }
        // redirect non-admins
        return "redirect:/access-denied";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("user") User user, Authentication authentication) {

        if(authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))){

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userService.save(user);
            return "redirect:/admin/dashboard?success";
        }
        return "redirect:/access-denied";
    }
}
