package com.orbit.recommendation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbit.recommendation.model.Recommendation;
import com.orbit.recommendation.model.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private static final String CACHE_KEY_PREFIX = "recommendation:apicache:user:";
    private static final String CACHE_KEY_SUFFIX = ":next";

    private final ContextAggregationService contextAggregationService;
    private final RecommendationEngine recommendationEngine;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${cache.recommendation.ttl-seconds:30}")
    private long cacheTtlSeconds;

    /**
     * Get next recommendation for a user (read-through cache, short TTL).
     */
    public Mono<Recommendation> getNextRecommendation(String userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId + CACHE_KEY_SUFFIX;
        return reactiveRedisTemplate.opsForValue().get(cacheKey)
                .flatMap(cached -> {
                    try {
                        Recommendation rec = objectMapper.readValue(cached, Recommendation.class);
                        log.debug("Recommendation cache hit: {}", cacheKey);
                        return Mono.just(rec);
                    } catch (JsonProcessingException e) {
                        log.warn("Failed to deserialize cached recommendation, recomputing", e);
                        return computeNextRecommendation(userId)
                                .flatMap(rec -> setCache(cacheKey, rec).thenReturn(rec));
                    }
                })
                .switchIfEmpty(computeNextRecommendation(userId)
                        .flatMap(rec -> setCache(cacheKey, rec).thenReturn(rec)));
    }

    private Mono<Recommendation> computeNextRecommendation(String userId) {
        return contextAggregationService.aggregateUserContext(userId)
                .map(context -> {
                    Optional<Recommendation> recommendation = recommendationEngine.generateRecommendation(context);
                    return recommendation.orElseGet(() -> createFallbackRecommendation(userId));
                })
                .doOnSuccess(rec -> log.info("Generated recommendation for userId: {}, rule: {}, type: {}",
                        userId, rec.getRuleApplied(), rec.getType()))
                .doOnError(error -> log.error("Failed to generate recommendation for userId: {}", userId, error))
                .onErrorResume(error -> {
                    log.warn("Error generating recommendation for userId: {}, using fallback", userId, error);
                    return Mono.just(createFallbackRecommendation(userId));
                });
    }

    private Mono<Void> setCache(String cacheKey, Recommendation rec) {
        try {
            String value = objectMapper.writeValueAsString(rec);
            return reactiveRedisTemplate.opsForValue()
                    .set(cacheKey, value, Duration.ofSeconds(cacheTtlSeconds))
                    .then();
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize recommendation for cache", e);
            return Mono.empty();
        }
    }

    /**
     * Get all recommendations for a user (currently returns single next recommendation)
     */
    public Mono<List<Recommendation>> getAllRecommendations(String userId) {
        return getNextRecommendation(userId)
                .map(Collections::singletonList)
                .defaultIfEmpty(Collections.emptyList());
    }

    /**
     * Get recommendation with specific context (course/path)
     */
    public Mono<Recommendation> getRecommendation(String userId, String courseId, String pathId) {
        return contextAggregationService.aggregateUserContext(userId, courseId, pathId)
                .map(context -> {
                    Optional<Recommendation> recommendation = recommendationEngine.generateRecommendation(context);
                    return recommendation.orElseGet(() -> createFallbackRecommendation(userId));
                })
                .doOnError(error -> log.error("Failed to generate recommendation for userId: {}, courseId: {}, pathId: {}", 
                    userId, courseId, pathId, error))
                .onErrorResume(error -> {
                    log.warn("Error generating recommendation with context, using fallback", error);
                    return Mono.just(createFallbackRecommendation(userId));
                });
    }

    private Recommendation createFallbackRecommendation(String userId) {
        return new Recommendation(
                com.orbit.recommendation.model.RecommendationType.LESSON,
                "starter-lesson-1",
                "Start learning",
                "Begin your learning journey with this starter lesson.",
                0.3,
                com.orbit.recommendation.model.RuleType.SAFE_DEFAULT
        );
    }
}

