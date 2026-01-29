package com.orbit.progress.consumer;

import com.orbit.progress.event.LearningEvent;
import com.orbit.progress.service.ProgressCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LearningEventConsumer {

    private final ProgressCalculationService progressCalculationService;
    private static final String TOPIC = "learning-events";

    @KafkaListener(topics = TOPIC, groupId = "progress-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeLearningEvent(LearningEvent event) {
        try {
            log.debug("Consumed learning event: {}", event);
            progressCalculationService.processEvent(event);
        } catch (Exception e) {
            log.error("Failed to process learning event: {}", event, e);
            // Don't throw - allow processing to continue for other events
        }
    }
}

