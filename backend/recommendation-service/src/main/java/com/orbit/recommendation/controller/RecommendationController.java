package com.orbit.recommendation.controller;

import com.orbit.recommendation.model.Recommendation;
import com.orbit.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/users/{userId}/next")
    public Mono<ResponseEntity<Recommendation>> getNextRecommendation(
            @PathVariable String userId,
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) String pathId) {
        
        Mono<Recommendation> recommendationMono;
        
        if (courseId != null || pathId != null) {
            recommendationMono = recommendationService.getRecommendation(userId, courseId, pathId);
        } else {
            recommendationMono = recommendationService.getNextRecommendation(userId);
        }
        
        return recommendationMono
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/{userId}")
    public Mono<ResponseEntity<List<Recommendation>>> getAllRecommendations(@PathVariable String userId) {
        return recommendationService.getAllRecommendations(userId)
                .map(recommendations -> {
                    if (recommendations == null || recommendations.isEmpty()) {
                        return ResponseEntity.<List<Recommendation>>notFound().build();
                    }
                    return ResponseEntity.ok(recommendations);
                });
    }
}

