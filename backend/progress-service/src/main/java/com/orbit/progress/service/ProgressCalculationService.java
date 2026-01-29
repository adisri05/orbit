package com.orbit.progress.service;

import com.orbit.progress.event.LearningEvent;
import com.orbit.progress.event.LearningEventType;
import com.orbit.progress.model.UserProgressSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressCalculationService {

    private final ProgressStorageService progressStorageService;
    private final ProgressEventStreamService progressEventStreamService;
    private static final int DEFAULT_TOTAL_LESSONS = 10; // Stubbed - should come from config or external service

    public void processEvent(LearningEvent event) {
        if (event.getEventType() == LearningEventType.LESSON_STARTED) {
            handleLessonStarted(event);
        } else if (event.getEventType() == LearningEventType.LESSON_COMPLETED) {
            handleLessonCompleted(event);
        }
    }

    private void handleLessonStarted(LearningEvent event) {
        UserProgressSnapshot courseProgress = getOrCreateCourseProgress(event);
        courseProgress.setLastUpdatedAt(event.getOccurredAt());
        progressStorageService.saveCourseProgress(courseProgress);
        progressStorageService.invalidateCourseProgressCache(event.getUserId(), event.getCourseId());

        UserProgressSnapshot pathProgress = getOrCreatePathProgress(event);
        pathProgress.setLastUpdatedAt(event.getOccurredAt());
        progressStorageService.savePathProgress(pathProgress);
        progressStorageService.invalidatePathProgressCache(event.getUserId(), event.getPathId());

        progressEventStreamService.pushProgressUpdated(event.getUserId());
    }

    private void handleLessonCompleted(LearningEvent event) {
        UserProgressSnapshot courseProgress = getOrCreateCourseProgress(event);
        courseProgress.setCompletedLessonsCount(courseProgress.getCompletedLessonsCount() + 1);
        courseProgress.setCompletionPercentage(calculatePercentage(courseProgress.getCompletedLessonsCount(), courseProgress.getTotalLessons()));
        courseProgress.setLastUpdatedAt(event.getOccurredAt());
        progressStorageService.saveCourseProgress(courseProgress);
        progressStorageService.invalidateCourseProgressCache(event.getUserId(), event.getCourseId());

        UserProgressSnapshot pathProgress = getOrCreatePathProgress(event);
        pathProgress.setCompletedLessonsCount(pathProgress.getCompletedLessonsCount() + 1);
        pathProgress.setCompletionPercentage(calculatePercentage(pathProgress.getCompletedLessonsCount(), pathProgress.getTotalLessons()));
        pathProgress.setLastUpdatedAt(event.getOccurredAt());
        progressStorageService.savePathProgress(pathProgress);
        progressStorageService.invalidatePathProgressCache(event.getUserId(), event.getPathId());

        progressEventStreamService.pushProgressUpdated(event.getUserId());
    }

    private UserProgressSnapshot getOrCreateCourseProgress(LearningEvent event) {
        UserProgressSnapshot progress = progressStorageService.getCourseProgress(event.getUserId(), event.getCourseId());
        if (progress == null) {
            progress = new UserProgressSnapshot();
            progress.setUserId(event.getUserId());
            progress.setCourseId(event.getCourseId());
            progress.setPathId(event.getPathId());
            progress.setTotalLessons(DEFAULT_TOTAL_LESSONS);
            progress.setCompletedLessonsCount(0);
            progress.setCompletionPercentage(0.0);
            progress.setLastUpdatedAt(Instant.now());
        }
        return progress;
    }

    private UserProgressSnapshot getOrCreatePathProgress(LearningEvent event) {
        UserProgressSnapshot progress = progressStorageService.getPathProgress(event.getUserId(), event.getPathId());
        if (progress == null) {
            progress = new UserProgressSnapshot();
            progress.setUserId(event.getUserId());
            progress.setPathId(event.getPathId());
            progress.setTotalLessons(DEFAULT_TOTAL_LESSONS);
            progress.setCompletedLessonsCount(0);
            progress.setCompletionPercentage(0.0);
            progress.setLastUpdatedAt(Instant.now());
        }
        return progress;
    }

    private double calculatePercentage(int completed, int total) {
        if (total == 0) {
            return 0.0;
        }
        return (completed * 100.0) / total;
    }
}

