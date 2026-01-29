package com.orbit.analytics.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbit.analytics.event.LearningEvent;
import com.orbit.analytics.event.LearningEventType;
import com.orbit.analytics.model.CourseAnalytics;
import com.orbit.analytics.model.PlatformAnalytics;
import com.orbit.analytics.model.UserAnalytics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final AnalyticsEventStreamService analyticsEventStreamService;

    @Value("${cache.analytics.ttl-seconds:60}")
    private long cacheTtlSeconds;

    private static final String USER_ANALYTICS_KEY_PREFIX = "analytics:user:";
    private static final String COURSE_ANALYTICS_KEY_PREFIX = "analytics:course:";
    private static final String PLATFORM_ANALYTICS_KEY = "analytics:platform";
    private static final String CACHE_PREFIX_USER = "analytics:apicache:user:";
    private static final String CACHE_PREFIX_COURSE = "analytics:apicache:course:";
    private static final String CACHE_PLATFORM = "analytics:apicache:platform";

    public void processEvent(LearningEvent event) {
        if (event.getEventType() == LearningEventType.LESSON_STARTED) {
            processLessonStarted(event);
        } else if (event.getEventType() == LearningEventType.LESSON_COMPLETED) {
            processLessonCompleted(event);
        }
    }

    private void processLessonStarted(LearningEvent event) {
        updateUserAnalyticsOnStart(event);
        updateCourseAnalyticsOnStart(event);
        updatePlatformAnalytics(event);
        invalidateCacheForEvent(event);
    }

    private void processLessonCompleted(LearningEvent event) {
        updateUserAnalyticsOnComplete(event);
        updateCourseAnalyticsOnComplete(event);
        updatePlatformAnalytics(event);
        invalidateCacheForEvent(event);
    }

    /**
     * Invalidate read-through cache for affected keys (event-driven invalidation).
     */
    private void invalidateCacheForEvent(LearningEvent event) {
        redisTemplate.delete(CACHE_PREFIX_USER + event.getUserId());
        if (event.getCourseId() != null) {
            redisTemplate.delete(CACHE_PREFIX_COURSE + event.getCourseId());
        }
        redisTemplate.delete(CACHE_PLATFORM);
        log.debug("Invalidated analytics cache for userId={}, courseId={}", event.getUserId(), event.getCourseId());
        analyticsEventStreamService.pushAnalyticsUpdated(event.getUserId());
    }

    private void updateUserAnalyticsOnStart(LearningEvent event) {
        String userId = event.getUserId();
        
        UserAnalytics analytics = getUserAnalytics(userId);
        if (analytics == null) {
            analytics = new UserAnalytics(userId, 0L, 0L, null);
        }
        
        analytics.setLessonsStartedCount(analytics.getLessonsStartedCount() + 1);
        analytics.setLastActiveAt(event.getOccurredAt());
        
        saveUserAnalytics(analytics);
    }

    private void updateUserAnalyticsOnComplete(LearningEvent event) {
        String userId = event.getUserId();
        
        UserAnalytics analytics = getUserAnalytics(userId);
        if (analytics == null) {
            analytics = new UserAnalytics(userId, 0L, 0L, null);
        }
        
        analytics.setLessonsCompletedCount(analytics.getLessonsCompletedCount() + 1);
        analytics.setLastActiveAt(event.getOccurredAt());
        
        saveUserAnalytics(analytics);
    }

    private void updateCourseAnalyticsOnStart(LearningEvent event) {
        if (event.getCourseId() == null) {
            return;
        }
        
        String courseId = event.getCourseId();
        CourseAnalytics analytics = getCourseAnalytics(courseId);
        if (analytics == null) {
            analytics = new CourseAnalytics(courseId, 0L, 0L, 0L);
        }
        
        analytics.setTotalLessonStarts(analytics.getTotalLessonStarts() + 1);
        
        // Recalculate drop-off count: starts - completions
        recalculateCourseDropOff(analytics);
        
        saveCourseAnalytics(analytics);
    }

    private void updateCourseAnalyticsOnComplete(LearningEvent event) {
        if (event.getCourseId() == null) {
            return;
        }
        
        String courseId = event.getCourseId();
        CourseAnalytics analytics = getCourseAnalytics(courseId);
        if (analytics == null) {
            analytics = new CourseAnalytics(courseId, 0L, 0L, 0L);
        }
        
        analytics.setTotalLessonCompletions(analytics.getTotalLessonCompletions() + 1);
        
        // Recalculate drop-off count: starts - completions
        recalculateCourseDropOff(analytics);
        
        saveCourseAnalytics(analytics);
    }

    private void updatePlatformAnalytics(LearningEvent event) {
        PlatformAnalytics analytics = getPlatformAnalytics();
        if (analytics == null) {
            analytics = new PlatformAnalytics(0L, 0L);
        }
        
        analytics.setTotalEventsProcessed(analytics.getTotalEventsProcessed() + 1);
        
        if (event.getEventType() == LearningEventType.LESSON_COMPLETED) {
            analytics.setTotalLessonCompletions(analytics.getTotalLessonCompletions() + 1);
        }
        
        savePlatformAnalytics(analytics);
    }

    private void recalculateCourseDropOff(CourseAnalytics analytics) {
        // Drop-off count = total starts - total completions
        Long dropOffCount = analytics.getTotalLessonStarts() - analytics.getTotalLessonCompletions();
        analytics.setDropOffCount(Math.max(0L, dropOffCount));
    }

    private void saveUserAnalytics(UserAnalytics analytics) {
        try {
            String key = USER_ANALYTICS_KEY_PREFIX + analytics.getUserId();
            String value = objectMapper.writeValueAsString(analytics);
            redisTemplate.opsForValue().set(key, value);
        } catch (JsonProcessingException e) {
            log.error("Failed to save user analytics for userId: {}", analytics.getUserId(), e);
        }
    }

    private void saveCourseAnalytics(CourseAnalytics analytics) {
        try {
            String key = COURSE_ANALYTICS_KEY_PREFIX + analytics.getCourseId();
            String value = objectMapper.writeValueAsString(analytics);
            redisTemplate.opsForValue().set(key, value);
        } catch (JsonProcessingException e) {
            log.error("Failed to save course analytics for courseId: {}", analytics.getCourseId(), e);
        }
    }

    private void savePlatformAnalytics(PlatformAnalytics analytics) {
        try {
            String value = objectMapper.writeValueAsString(analytics);
            redisTemplate.opsForValue().set(PLATFORM_ANALYTICS_KEY, value);
        } catch (JsonProcessingException e) {
            log.error("Failed to save platform analytics", e);
        }
    }

    public UserAnalytics getUserAnalytics(String userId) {
        try {
            String cacheKey = CACHE_PREFIX_USER + userId;
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("Analytics cache hit: {}", cacheKey);
                return objectMapper.readValue(cached, UserAnalytics.class);
            }
            String key = USER_ANALYTICS_KEY_PREFIX + userId;
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            UserAnalytics result = objectMapper.readValue(value, UserAnalytics.class);
            redisTemplate.opsForValue().set(cacheKey, value, cacheTtlSeconds, TimeUnit.SECONDS);
            return result;
        } catch (JsonProcessingException e) {
            log.error("Failed to get user analytics for userId: {}", userId, e);
            return null;
        }
    }

    public CourseAnalytics getCourseAnalytics(String courseId) {
        try {
            String cacheKey = CACHE_PREFIX_COURSE + courseId;
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("Analytics cache hit: {}", cacheKey);
                return objectMapper.readValue(cached, CourseAnalytics.class);
            }
            String key = COURSE_ANALYTICS_KEY_PREFIX + courseId;
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            CourseAnalytics result = objectMapper.readValue(value, CourseAnalytics.class);
            redisTemplate.opsForValue().set(cacheKey, value, cacheTtlSeconds, TimeUnit.SECONDS);
            return result;
        } catch (JsonProcessingException e) {
            log.error("Failed to get course analytics for courseId: {}", courseId, e);
            return null;
        }
    }

    public PlatformAnalytics getPlatformAnalytics() {
        try {
            String cached = redisTemplate.opsForValue().get(CACHE_PLATFORM);
            if (cached != null) {
                log.debug("Analytics cache hit: {}", CACHE_PLATFORM);
                return objectMapper.readValue(cached, PlatformAnalytics.class);
            }
            String value = redisTemplate.opsForValue().get(PLATFORM_ANALYTICS_KEY);
            if (value == null) {
                return new PlatformAnalytics(0L, 0L);
            }
            PlatformAnalytics result = objectMapper.readValue(value, PlatformAnalytics.class);
            redisTemplate.opsForValue().set(CACHE_PLATFORM, value, cacheTtlSeconds, TimeUnit.SECONDS);
            return result;
        } catch (JsonProcessingException e) {
            log.error("Failed to get platform analytics", e);
            return new PlatformAnalytics(0L, 0L);
        }
    }
}

