package com.deep.studenthousing.config;

import com.deep.studenthousing.entity.User;
import com.deep.studenthousing.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.io.IOException;

@Configuration
public class CustomLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UserRepository userRepo;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    public CustomLoginSuccessHandler(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        String email = authentication.getName();
        User user = userRepo.findByEmail(email);

        String targetUrl = switch (user.getRole()) {
            case STUDENT -> "/student/dashboard";
            case OWNER -> "/properties/owner/" + user.getId();
            case ADMIN -> "/admin/dashboard";
            default -> "/login?error=true";
        };

        clearAuthenticationAttributes(request);
        redirectStrategy.sendRedirect(request, response, targetUrl);
    }
}
