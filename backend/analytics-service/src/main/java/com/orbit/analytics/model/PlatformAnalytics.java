package com.orbit.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformAnalytics {
    private Long totalEventsProcessed;
    private Long totalLessonCompletions;
}

