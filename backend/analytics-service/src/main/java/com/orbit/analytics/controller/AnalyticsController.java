package com.orbit.analytics.controller;

import com.orbit.analytics.model.CourseAnalytics;
import com.orbit.analytics.model.PlatformAnalytics;
import com.orbit.analytics.model.UserAnalytics;
import com.orbit.analytics.service.AnalyticsEventStreamService;
import com.orbit.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AnalyticsEventStreamService analyticsEventStreamService;

    /**
     * Server-Sent Events stream for analytics updates. Read-only; REST remains source of truth.
     * Client should refetch from REST when analytics_updated is received.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAnalytics(@RequestParam String userId) {
        return analyticsEventStreamService.subscribe(userId);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserAnalytics> getUserAnalytics(@PathVariable String userId) {
        UserAnalytics analytics = analyticsService.getUserAnalytics(userId);
        if (analytics == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<CourseAnalytics> getCourseAnalytics(@PathVariable String courseId) {
        CourseAnalytics analytics = analyticsService.getCourseAnalytics(courseId);
        if (analytics == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/platform/overview")
    public ResponseEntity<PlatformAnalytics> getPlatformOverview() {
        PlatformAnalytics analytics = analyticsService.getPlatformAnalytics();
        return ResponseEntity.ok(analytics);
    }
}

