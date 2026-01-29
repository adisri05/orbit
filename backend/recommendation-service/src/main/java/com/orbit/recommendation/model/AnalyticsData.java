package com.orbit.recommendation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Unified model for both UserAnalytics and CourseAnalytics responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyticsData {
    // User Analytics fields
    private String userId;
    private Long lessonsStartedCount;
    private Long lessonsCompletedCount;
    private Instant lastActiveAt;
    
    // Course Analytics fields
    private String courseId;
    private Long totalLessonStarts;
    private Long totalLessonCompletions;
    private Long dropOffCount;
}

