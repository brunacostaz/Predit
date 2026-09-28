package br.com.fiap.predit.risk.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private static final long WINDOW_SECONDS = 60;
    private final int maxRequests;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimitFilter(@Value("${predit.rate-limit.requests-per-minute:120}") int maxRequests) {
        this.maxRequests = maxRequests;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api/")) { chain.doFilter(request, response); return; }
        String key = request.getRemoteAddr();
        long now = Instant.now().getEpochSecond();
        Window window = windows.compute(key, (ignored, current) -> current == null || now >= current.startedAt + WINDOW_SECONDS
                ? new Window(now, 1) : new Window(current.startedAt, current.count + 1));
        response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequests));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, maxRequests - window.count)));
        if (window.count > maxRequests) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.setHeader("Retry-After", String.valueOf(window.startedAt + WINDOW_SECONDS - now));
            response.getWriter().write("{\"type\":\"https://predit.com.br/problems/429\",\"title\":\"Too Many Requests\","
                    + "\"status\":429,\"detail\":\"Request limit exceeded, try again later\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    private record Window(long startedAt, int count) {}
}
