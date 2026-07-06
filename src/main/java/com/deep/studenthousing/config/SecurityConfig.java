package com.deep.studenthousing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
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
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/admin/users/**", "/admin/update/**"))
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
