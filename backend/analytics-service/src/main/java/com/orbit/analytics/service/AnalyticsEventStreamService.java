package com.orbit.analytics.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Server-Sent Events for analytics updates. Read-only stream; REST remains source of truth.
 * Notifies subscribers when analytics for a user have changed (after event consumption).
 */
@Service
@Slf4j
public class AnalyticsEventStreamService {

    private static final long SSE_TIMEOUT_MS = 5 * 60 * 1000; // 5 minutes

    private final Map<String, CopyOnWriteArrayList<SseEmitter>> emittersByUserId = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        emittersByUserId.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> remove(userId, emitter));
        emitter.onError((e) -> remove(userId, emitter));

        try {
            emitter.send(SseEmitter.event().name("connected").data("analytics"));
        } catch (IOException e) {
            log.debug("Failed to send initial SSE event", e);
            remove(userId, emitter);
        }
        return emitter;
    }

    /**
     * Notify subscribers that analytics for this user were updated (call after event processing).
     */
    public void pushAnalyticsUpdated(String userId) {
        CopyOnWriteArrayList<SseEmitter> emitters = emittersByUserId.get(userId);
        if (emitters == null || emitters.isEmpty()) return;
        String payload = "{\"type\":\"analytics_updated\",\"userId\":\"" + userId + "\"}";
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().name("analytics_updated").data(payload));
            } catch (IOException e) {
                remove(userId, emitter);
            }
        });
    }

    private void remove(String userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list = emittersByUserId.get(userId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                emittersByUserId.remove(userId);
            }
        }
    }
}
