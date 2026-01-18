package br.gov.mt.seplag.security;

import com.github.benmanes.caffeine.cache.LoadingCache;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final LoadingCache<String, Bucket> rateLimitCache;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String key = getRateLimitKey(request);
        Bucket bucket = rateLimitCache.get(key);

        if (bucket == null) {
            log.error("Failed to get rate limit bucket for key: {}", key);
            return true;
        }

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        }

        long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;

        log.warn("Rate limit exceeded for key: {}. Retry after: {}s", key, waitForRefill);

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
        response.setContentType("application/json");
        response.getWriter().write(String.format(
                "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Try again in %d seconds.\",\"retryAfter\":%d}",
                waitForRefill, waitForRefill
        ));

        return false;
    }

    private String getRateLimitKey(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "user:" + auth.getName();
        }

        String ip = getClientIP(request);
        return "ip:" + ip;
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}