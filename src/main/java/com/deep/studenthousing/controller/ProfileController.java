package com.deep.studenthousing.controller;

import com.deep.studenthousing.entity.User;
import com.deep.studenthousing.exception.UserNotFoundException;
import com.deep.studenthousing.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProfileController {

    private final UserService userService;

    @org.springframework.beans.factory.annotation.Autowired
    private com.deep.studenthousing.service.AccessGuard guard;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile/{id}")
    public String viewProfile(@PathVariable Long id, Model model,
                              org.springframework.security.core.Authentication authentication){
        // Profiles hold email, phone and the user's listings: only the user themself (or an admin).
        guard.requireSelfOrAdmin(id, authentication);
        User user = userService.findById(id);

        if(user != null){
            model.addAttribute("user", user);
            return "profile";
        }else{
            throw new RuntimeException("user not found!!");
        }
    }

    @GetMapping("/profile/admin")
    public String viewAdminProfile(Model model){
        User admin = userService.findFirstByRole();
        if(admin != null){
            User newUser = new User();
            newUser.setEmail(admin.getEmail());
            newUser.setPhone(admin.getPhone());
            model.addAttribute("user", newUser);
            return "admin-profile";
        }else{
            throw new UserNotFoundException("Admin not found");
        }
    }
}
