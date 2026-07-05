package com.deep.studenthousing.config;

import com.deep.studenthousing.entity.User;
import com.deep.studenthousing.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

@Configuration
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepo;

    public CustomLoginSuccessHandler(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String email = authentication.getName();
        User user = userRepo.findByEmail(email);

        switch (user.getRole()) {
            case STUDENT:
                response.sendRedirect("/student/dashboard");
                break;
            case OWNER:
                response.sendRedirect("/properties/owner/" + user.getId());
                break;
            case ADMIN:
                response.sendRedirect("/admin/dashboard");
                break;
            default:
                response.sendRedirect("/login?error=true");
                break;
        }
    }
}
