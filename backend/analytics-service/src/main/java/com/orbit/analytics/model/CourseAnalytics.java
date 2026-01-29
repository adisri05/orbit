package com.orbit.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseAnalytics {
    private String courseId;
    private Long totalLessonStarts;
    private Long totalLessonCompletions;
    private Long dropOffCount;
}

