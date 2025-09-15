package com.deep.studenthousing.controller;

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

@Controller
public class RegisterController {

    @Autowired
    private PasswordEncoder passwordEncoder;
    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register-user")
    public String registerForm(Model model) {

        model.addAttribute("user", new User());
        return "register-only-user";
    }

    @PostMapping("/register-user")
    public String register(@ModelAttribute("user") User user) {

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.save(user);
        return "redirect:/login?message=success";
    }


}
