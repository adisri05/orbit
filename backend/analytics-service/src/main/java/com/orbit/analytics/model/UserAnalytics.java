package com.orbit.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAnalytics {
    private String userId;
    private Long lessonsStartedCount;
    private Long lessonsCompletedCount;
    private Instant lastActiveAt;
}

