package com.orbit.recommendation.service;

import com.orbit.recommendation.client.AnalyticsServiceClient;
import com.orbit.recommendation.client.ProgressServiceClient;
import com.orbit.recommendation.model.AnalyticsData;
import com.orbit.recommendation.model.ProgressData;
import com.orbit.recommendation.model.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContextAggregationService {

    private final ProgressServiceClient progressServiceClient;
    private final AnalyticsServiceClient analyticsServiceClient;

    private static final int INACTIVITY_THRESHOLD_DAYS = 7;
    private static final int CONSISTENT_ACTIVITY_THRESHOLD_DAYS = 3;
    private static final int BINGE_THRESHOLD_LESSONS = 5;
    private static final double HIGH_DROPOFF_RATE = 0.3; // 30%

    public Mono<UserContext> aggregateUserContext(String userId) {
        // Fetch user analytics
        Mono<AnalyticsData> userAnalyticsMono = analyticsServiceClient.getUserAnalytics(userId)
                .defaultIfEmpty(createEmptyUserAnalytics(userId));

        // Fetch progress data (we'll try to get from active course/path)
        // Since we don't know the active course/path upfront, we'll use analytics to infer
        return userAnalyticsMono.flatMap(userAnalytics -> {
            // For now, we'll create a simplified context
            // In a real implementation, you'd fetch progress for known courses/paths
            return Mono.just(buildUserContext(userId, userAnalytics, null, null));
        });
    }

    public Mono<UserContext> aggregateUserContext(String userId, String courseId, String pathId) {
        Mono<AnalyticsData> userAnalyticsMono = analyticsServiceClient.getUserAnalytics(userId)
                .defaultIfEmpty(createEmptyUserAnalytics(userId));

        Mono<ProgressData> courseProgressMono = courseId != null 
                ? progressServiceClient.getCourseProgress(userId, courseId)
                : Mono.just(null);

        Mono<ProgressData> pathProgressMono = pathId != null
                ? progressServiceClient.getPathProgress(userId, pathId)
                : Mono.just(null);

        Mono<AnalyticsData> courseAnalyticsMono = courseId != null
                ? analyticsServiceClient.getCourseAnalytics(courseId)
                        .defaultIfEmpty(createEmptyCourseAnalytics(courseId))
                : Mono.just(null);

        return Mono.zip(userAnalyticsMono, courseProgressMono, pathProgressMono, courseAnalyticsMono)
                .map(tuple -> {
                    AnalyticsData userAnalytics = tuple.getT1();
                    ProgressData courseProgress = tuple.getT2();
                    ProgressData pathProgress = tuple.getT3();
                    AnalyticsData courseAnalytics = tuple.getT4();

                    return buildUserContext(userId, userAnalytics, courseProgress, pathProgress, courseAnalytics);
                })
                .onErrorResume(error -> {
                    log.debug("Error aggregating context with courseId/pathId, using basic context", error);
                    // Fallback to basic context aggregation
                    return aggregateUserContext(userId);
                });
    }

    private UserContext buildUserContext(String userId, AnalyticsData userAnalytics, 
                                        ProgressData courseProgress, ProgressData pathProgress) {
        return buildUserContext(userId, userAnalytics, courseProgress, pathProgress, null);
    }

    private AnalyticsData createEmptyCourseAnalytics(String courseId) {
        AnalyticsData analytics = new AnalyticsData();
        analytics.setCourseId(courseId);
        analytics.setTotalLessonStarts(0L);
        analytics.setTotalLessonCompletions(0L);
        analytics.setDropOffCount(0L);
        return analytics;
    }

    private UserContext buildUserContext(String userId, AnalyticsData userAnalytics,
                                        ProgressData courseProgress, ProgressData pathProgress,
                                        AnalyticsData courseAnalytics) {
        UserContext.UserContextBuilder builder = UserContext.builder()
                .userId(userId)
                .lessonsStartedCount(userAnalytics != null ? userAnalytics.getLessonsStartedCount() : 0L)
                .lessonsCompletedCount(userAnalytics != null ? userAnalytics.getLessonsCompletedCount() : 0L)
                .lastActiveAt(userAnalytics != null ? userAnalytics.getLastActiveAt() : null)
                .completedLessons(Collections.emptyList()) // Would need detailed progress API
                .startedButIncompleteLessons(Collections.emptyList()) // Would need detailed progress API
                .lastCompletedLesson(null) // Would need detailed progress API
                .isNewUser(userAnalytics == null || 
                          (userAnalytics.getLessonsStartedCount() == null || userAnalytics.getLessonsStartedCount() == 0));

        if (courseProgress != null) {
            builder.activeCourseId(courseProgress.getCourseId())
                   .totalLessonsInActiveCourse(courseProgress.getTotalLessons())
                   .completedLessonsCount(courseProgress.getCompletedLessonsCount())
                   .completionPercentage(courseProgress.getCompletionPercentage());
        }

        if (pathProgress != null) {
            builder.activePathId(pathProgress.getPathId());
        }

        if (courseAnalytics != null) {
            builder.courseDropOffCount(courseAnalytics.getDropOffCount())
                   .courseTotalStarts(courseAnalytics.getTotalLessonStarts());
            
            if (courseAnalytics.getTotalLessonStarts() != null && courseAnalytics.getTotalLessonStarts() > 0) {
                double dropOffRate = (double) courseAnalytics.getDropOffCount() / courseAnalytics.getTotalLessonStarts();
                builder.courseDropOffRate(dropOffRate);
            }
        }

        // Compute derived fields
        UserContext context = builder.build();
        computeDerivedFields(context);
        
        return context;
    }

    private void computeDerivedFields(UserContext context) {
        // Compute inactivity
        if (context.getLastActiveAt() != null) {
            long daysSinceLastActivity = Duration.between(context.getLastActiveAt(), Instant.now()).toDays();
            context.setDaysSinceLastActivity((int) daysSinceLastActivity);
            context.setInactive(daysSinceLastActivity > INACTIVITY_THRESHOLD_DAYS);
        } else {
            context.setInactive(true);
            context.setDaysSinceLastActivity(Integer.MAX_VALUE);
        }

        // Compute consistent activity (simplified - would need historical data)
        context.setConsistentlyActive(context.getDaysSinceLastActivity() <= CONSISTENT_ACTIVITY_THRESHOLD_DAYS && 
                                      context.getDaysSinceLastActivity() > 0);

        // Compute binge learning (simplified)
        if (context.getLessonsCompletedCount() != null && context.getLessonsCompletedCount() >= BINGE_THRESHOLD_LESSONS) {
            // If many lessons completed recently, might be binge learning
            context.setBingeLearning(context.getDaysSinceLastActivity() <= 1);
        } else {
            context.setBingeLearning(false);
        }
    }

    private AnalyticsData createEmptyUserAnalytics(String userId) {
        AnalyticsData analytics = new AnalyticsData();
        analytics.setUserId(userId);
        analytics.setLessonsStartedCount(0L);
        analytics.setLessonsCompletedCount(0L);
        return analytics;
    }
}

