package com.orbit.progress.controller;

import com.orbit.progress.model.UserProgressSnapshot;
import com.orbit.progress.service.ProgressEventStreamService;
import com.orbit.progress.service.ProgressStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressStorageService progressStorageService;
    private final ProgressEventStreamService progressEventStreamService;

    /**
     * Server-Sent Events stream for progress updates. Read-only; REST remains source of truth.
     * Client should refetch from REST when progress_updated is received.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamProgress(@RequestParam String userId) {
        return progressEventStreamService.subscribe(userId);
    }

    @GetMapping("/users/{userId}/courses/{courseId}")
    public ResponseEntity<UserProgressSnapshot> getCourseProgress(
            @PathVariable String userId,
            @PathVariable String courseId) {
        UserProgressSnapshot progress = progressStorageService.getCourseProgress(userId, courseId);
        if (progress == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/users/{userId}/paths/{pathId}")
    public ResponseEntity<UserProgressSnapshot> getPathProgress(
            @PathVariable String userId,
            @PathVariable String pathId) {
        UserProgressSnapshot progress = progressStorageService.getPathProgress(userId, pathId);
        if (progress == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(progress);
    }
}

