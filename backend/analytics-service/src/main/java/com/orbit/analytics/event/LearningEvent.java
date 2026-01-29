package com.orbit.analytics.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningEvent {

    private LearningEventType eventType;
    private String userId;
    private String pathId;
    private String courseId;
    private String lessonId;
    private Instant occurredAt;
}

