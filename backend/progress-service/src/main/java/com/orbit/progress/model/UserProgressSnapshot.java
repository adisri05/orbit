package com.orbit.progress.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProgressSnapshot {

    private String userId;
    private String pathId;
    private String courseId;
    private int totalLessons;
    private int completedLessonsCount;
    private double completionPercentage;
    private Instant lastUpdatedAt;
}

