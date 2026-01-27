package br.gov.mt.seplag.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.function.Supplier;

@Configuration
public class RateLimitConfig {

    @Bean
    public LoadingCache<String, Bucket> rateLimitCache() {
        return Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(10))
                .build(key -> createBucket(key));
    }

    private Bucket createBucket(String key) {
        Bandwidth limit;

        if (key.startsWith("user:")) {
            limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        } else {
            limit = Bandwidth.classic(20, Refill.intervally(20, Duration.ofMinutes(1)));
        }

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @Bean
    public Supplier<Bucket> bucketSupplier() {
        return () -> {
            Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
            return Bucket.builder().addLimit(limit).build();
        };
    }
}