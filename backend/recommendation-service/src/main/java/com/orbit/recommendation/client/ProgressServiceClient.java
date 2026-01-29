package com.orbit.recommendation.client;

import com.orbit.recommendation.model.ProgressData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProgressServiceClient {

    private final WebClient.Builder webClientBuilder;
    
    @Value("${services.progress.base-url}")
    private String progressServiceUrl;

    public Mono<ProgressData> getCourseProgress(String userId, String courseId) {
        WebClient webClient = webClientBuilder.baseUrl(progressServiceUrl).build();
        
        return webClient.get()
                .uri("/progress/users/{userId}/courses/{courseId}", userId, courseId)
                .retrieve()
                .bodyToMono(ProgressData.class)
                .doOnError(error -> log.debug("Failed to fetch course progress for userId: {}, courseId: {} - {}", userId, courseId, error.getMessage()))
                .onErrorResume(error -> {
                    log.debug("Course progress not found for userId: {}, courseId: {}", userId, courseId);
                    return Mono.just(null);
                });
    }

    public Mono<ProgressData> getPathProgress(String userId, String pathId) {
        WebClient webClient = webClientBuilder.baseUrl(progressServiceUrl).build();
        
        return webClient.get()
                .uri("/progress/users/{userId}/paths/{pathId}", userId, pathId)
                .retrieve()
                .bodyToMono(ProgressData.class)
                .doOnError(error -> log.debug("Failed to fetch path progress for userId: {}, pathId: {} - {}", userId, pathId, error.getMessage()))
                .onErrorResume(error -> {
                    log.debug("Path progress not found for userId: {}, pathId: {}", userId, pathId);
                    return Mono.just(null);
                });
    }
}

