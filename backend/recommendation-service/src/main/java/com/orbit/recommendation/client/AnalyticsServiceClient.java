package com.orbit.recommendation.client;

import com.orbit.recommendation.model.AnalyticsData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceClient {

    private final WebClient.Builder webClientBuilder;
    
    @Value("${services.analytics.base-url}")
    private String analyticsServiceUrl;

    public Mono<AnalyticsData> getUserAnalytics(String userId) {
        WebClient webClient = webClientBuilder.baseUrl(analyticsServiceUrl).build();
        
        return webClient.get()
                .uri("/analytics/users/{userId}", userId)
                .retrieve()
                .bodyToMono(AnalyticsData.class)
                .doOnError(error -> log.debug("Failed to fetch user analytics for userId: {} - {}", userId, error.getMessage()))
                .onErrorResume(error -> {
                    log.debug("User analytics not found for userId: {}, using empty analytics", userId);
                    return Mono.just(null);
                });
    }

    public Mono<AnalyticsData> getCourseAnalytics(String courseId) {
        WebClient webClient = webClientBuilder.baseUrl(analyticsServiceUrl).build();
        
        return webClient.get()
                .uri("/analytics/courses/{courseId}", courseId)
                .retrieve()
                .bodyToMono(AnalyticsData.class)
                .doOnError(error -> log.debug("Failed to fetch course analytics for courseId: {} - {}", courseId, error.getMessage()))
                .onErrorResume(error -> {
                    log.debug("Course analytics not found for courseId: {}, using empty analytics", courseId);
                    return Mono.just(null);
                });
    }
}

