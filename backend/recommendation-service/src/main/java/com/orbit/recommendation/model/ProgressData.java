package com.orbit.recommendation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProgressData {
    private String userId;
    private String pathId;
    private String courseId;
    private int totalLessons;
    private int completedLessonsCount;
    private double completionPercentage;
    private Instant lastUpdatedAt;
}

