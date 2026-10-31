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

@Controller
public class RegisterController {

    @Autowired
    private PasswordEncoder passwordEncoder;
    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    private static final java.util.regex.Pattern EMAIL_PATTERN =
            java.util.regex.Pattern.compile("^[A-Za-z0-9._%+'-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private String registrationError(Model model, String message) {
        model.addAttribute("errorMessage", message);
        return "error-page";
    }

    @GetMapping("/register-user")
    public String registerForm(Model model) {

        model.addAttribute("user", new User());
        return "register-only-user";
    }

    @PostMapping("/register-user")
    public String register(@ModelAttribute("user") User submitted, Model model) {
        // Never save the object the browser built: it could carry an id (overwriting an
        // existing account) or role=ADMIN. Copy only the fields a visitor may choose.
        String email = submitted.getEmail() == null ? "" : submitted.getEmail().trim();
        String name = submitted.getFullName() == null ? "" : submitted.getFullName().trim();
        String phone = submitted.getPhone() == null ? "" : submitted.getPhone().trim();
        String rawPassword = submitted.getPassword() == null ? "" : submitted.getPassword();

        if (!EMAIL_PATTERN.matcher(email).matches() || email.length() > 254) {
            return registrationError(model, "Please enter a valid email address.");
        }
        if (name.isEmpty() || name.length() > 100 || phone.isEmpty() || phone.length() > 20) {
            return registrationError(model, "Please enter a valid name and phone number.");
        }
        if (rawPassword.length() < 8 || rawPassword.length() > 72) {
            // 72 = BCrypt's real limit; longer passwords are silently truncated.
            return registrationError(model, "Password must be between 8 and 72 characters.");
        }
        // Public sign-up may only create students or owners. Admins are created by the
        // server (DataInitializer) or by an existing admin.
        Role role = submitted.getRole();
        if (role != Role.STUDENT && role != Role.OWNER) {
            return registrationError(model, "Please choose a valid account type.");
        }
        if (userService.findByEmail(email) != null) {
            return registrationError(model, "Email already exists.");
        }

        User user = new User();
        user.setFullName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(rawPassword));
        userService.save(user);
        return "redirect:/login?message=success";
    }


}
