package com.orbit.recommendation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Aggregated user context from Progress and Analytics services
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {
    // From Progress Service
    private String userId;
    private String activePathId;
    private String activeCourseId;
    private List<String> completedLessons;
    private List<String> startedButIncompleteLessons;
    private String lastCompletedLesson;
    private int totalLessonsInActiveCourse;
    private int completedLessonsCount;
    private double completionPercentage;
    
    // From Analytics Service
    private Instant lastActiveAt;
    private Long lessonsStartedCount;
    private Long lessonsCompletedCount;
    private Long courseDropOffCount;
    private Long courseTotalStarts;
    private Double courseDropOffRate;
    
    // Computed fields
    private boolean isNewUser;
    private boolean isInactive;
    private int daysSinceLastActivity;
    private boolean isConsistentlyActive;
    private boolean isBingeLearning;
}

