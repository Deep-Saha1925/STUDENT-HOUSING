package com.deep.studenthousing.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Slows down password guessing on POST /login.
 *
 * After MAX_FAILURES wrong passwords for the same (IP address + email) within
 * WINDOW, further attempts are bounced to /login?error=locked until the window
 * passes. State is in memory, so it resets on restart and is per server
 * instance, which is fine for a single-instance deployment.
 */
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_FAILURES = 5;
    private static final long WINDOW_MS = Duration.ofMinutes(15).toMillis();
    private static final int MAX_TRACKED_KEYS = 10_000;

    private record Attempts(int count, long firstFailureAt) {}

    private final ConcurrentHashMap<String, Attempts> failures = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod()) && "/login".equals(request.getServletPath()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String key = request.getRemoteAddr() + "|" + normalise(request.getParameter("username"));
        long now = System.currentTimeMillis();

        Attempts current = failures.get(key);
        if (current != null && now - current.firstFailureAt() > WINDOW_MS) {
            failures.remove(key);
            current = null;
        }
        if (current != null && current.count() >= MAX_FAILURES) {
            response.sendRedirect(request.getContextPath() + "/login?error=locked");
            return;
        }

        chain.doFilter(request, response);

        // Spring's default failure handler redirects to /login?error, success redirects elsewhere.
        String location = response.getHeader("Location");
        if (location != null && location.contains("/login?error")) {
            recordFailure(key, now);
        } else if (location != null) {
            failures.remove(key);
        }
    }

    private void recordFailure(String key, long now) {
        if (failures.size() > MAX_TRACKED_KEYS) {
            failures.entrySet().removeIf(e -> now - e.getValue().firstFailureAt() > WINDOW_MS);
        }
        failures.merge(key, new Attempts(1, now),
                (old, ignored) -> new Attempts(old.count() + 1, old.firstFailureAt()));
    }

    private String normalise(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}
