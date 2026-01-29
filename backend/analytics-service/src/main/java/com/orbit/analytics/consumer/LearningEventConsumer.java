package com.orbit.analytics.consumer;

import com.orbit.analytics.event.LearningEvent;
import com.orbit.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LearningEventConsumer {

    private final AnalyticsService analyticsService;
    private static final String TOPIC = "learning-events";

    @KafkaListener(topics = TOPIC, groupId = "analytics-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeLearningEvent(LearningEvent event) {
        try {
            log.info("Consumed learning event: eventType={}, userId={}, courseId={}, lessonId={}", 
                    event.getEventType(), event.getUserId(), event.getCourseId(), event.getLessonId());
            analyticsService.processEvent(event);
            log.debug("Analytics updated for event: {}", event);
        } catch (Exception e) {
            log.error("Failed to process learning event: {}", event, e);
            // Don't throw - allow processing to continue for other events
        }
    }
}

