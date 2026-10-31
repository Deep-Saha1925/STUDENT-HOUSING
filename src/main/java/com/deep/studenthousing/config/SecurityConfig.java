package com.deep.studenthousing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;

@Configuration
public class SecurityConfig {

    private final CustomLoginSuccessHandler successHandler;

    public SecurityConfig(CustomLoginSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/users/register").hasRole("ADMIN")
                        .requestMatchers("/properties/owner/**").hasRole("OWNER")
                        .requestMatchers("/student/**").hasRole("STUDENT")
                        // Booking actions need a login. They sit under /properties/** (public
                        // for property pages), so list them before that permitAll rule.
                        .requestMatchers("/properties/my-bookings", "/properties/bookings/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/properties/*/book").authenticated()
                        .requestMatchers("/", "/search", "/fragments/**", "/access-denied", "/logo.png", "/css/**", "/js/**", "/images/**", "/properties/**").permitAll()
                        .requestMatchers("/register-user").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .successHandler(successHandler)
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(((request, response, accessDeniedException) -> {
                            if(accessDeniedException instanceof InvalidCsrfTokenException
                                || accessDeniedException instanceof MissingCsrfTokenException){
                                // CSRF token mismatch on login POST - NOT a real
                                // authorization failure. Send back to login instead
                                // of the access-denied page.

                                response.sendRedirect("/login?error=csrf");
                            }else{
                                response.sendRedirect("/access-denied");
                            }
                        }))
                )
                // CSRF protection stays ON for every state-changing request, including the
                // admin screens (an exemption there would let any website edit users/roles
                // using an admin's logged-in browser). Thymeleaf th:action forms add the token.
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "frame-ancestors 'self'; base-uri 'self'; object-src 'none'; form-action 'self'"))
                        .referrerPolicy(ref -> ref.policy(
                                org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))
                .addFilterBefore(new LoginRateLimitFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
