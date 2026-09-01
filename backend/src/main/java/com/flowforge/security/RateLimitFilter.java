package com.flowforge.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowforge.exception.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final int requestsPerMinute;

    public RateLimitFilter(StringRedisTemplate redisTemplate, ObjectMapper objectMapper, @Value("${flowforge.rate-limit.requests-per-minute:100}") int requestsPerMinute) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.requestsPerMinute = requestsPerMinute;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String key = "rate-limit:" + clientKey(request);
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key, Duration.ofMinutes(1));
            }
            if (count != null && count > requestsPerMinute) {
                response.setStatus(429);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                objectMapper.writeValue(response.getOutputStream(), ApiError.of(429, "Too Many Requests", "Rate limit exceeded", request.getServletPath()));
                return;
            }
        } catch (RedisConnectionFailureException ignored) {
            filterChain.doFilter(request, response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String clientKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getName() != null) {
            return "user:" + authentication.getName();
        }
        return "ip:" + request.getRemoteAddr();
    }
}
