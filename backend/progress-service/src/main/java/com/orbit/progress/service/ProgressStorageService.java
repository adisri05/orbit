package com.orbit.progress.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbit.progress.model.UserProgressSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressStorageService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${cache.progress.ttl-seconds:60}")
    private long cacheTtlSeconds;

    private static final String COURSE_PROGRESS_KEY_PREFIX = "progress:user:";
    private static final String COURSE_PROGRESS_KEY_SUFFIX = ":course:";
    private static final String PATH_PROGRESS_KEY_PREFIX = "progress:user:";
    private static final String PATH_PROGRESS_KEY_SUFFIX = ":path:";
    private static final String CACHE_PREFIX_COURSE = "progress:apicache:user:";
    private static final String CACHE_SUFFIX_COURSE = ":course:";
    private static final String CACHE_PREFIX_PATH = "progress:apicache:user:";
    private static final String CACHE_SUFFIX_PATH = ":path:";

    public void saveCourseProgress(UserProgressSnapshot progress) {
        try {
            String key = COURSE_PROGRESS_KEY_PREFIX + progress.getUserId() + COURSE_PROGRESS_KEY_SUFFIX + progress.getCourseId();
            String value = objectMapper.writeValueAsString(progress);
            redisTemplate.opsForValue().set(key, value);
            log.debug("Saved course progress: {}", key);
        } catch (JsonProcessingException e) {
            log.error("Failed to save course progress: {}", progress, e);
        }
    }

    public void savePathProgress(UserProgressSnapshot progress) {
        try {
            String key = PATH_PROGRESS_KEY_PREFIX + progress.getUserId() + PATH_PROGRESS_KEY_SUFFIX + progress.getPathId();
            String value = objectMapper.writeValueAsString(progress);
            redisTemplate.opsForValue().set(key, value);
            log.debug("Saved path progress: {}", key);
        } catch (JsonProcessingException e) {
            log.error("Failed to save path progress: {}", progress, e);
        }
    }

    /**
     * Invalidate read-through cache for course progress (call after event updates).
     */
    public void invalidateCourseProgressCache(String userId, String courseId) {
        String cacheKey = CACHE_PREFIX_COURSE + userId + CACHE_SUFFIX_COURSE + courseId;
        Boolean removed = redisTemplate.delete(cacheKey);
        if (Boolean.TRUE.equals(removed)) {
            log.debug("Invalidated progress cache: {}", cacheKey);
        }
    }

    /**
     * Invalidate read-through cache for path progress (call after event updates).
     */
    public void invalidatePathProgressCache(String userId, String pathId) {
        String cacheKey = CACHE_PREFIX_PATH + userId + CACHE_SUFFIX_PATH + pathId;
        Boolean removed = redisTemplate.delete(cacheKey);
        if (Boolean.TRUE.equals(removed)) {
            log.debug("Invalidated progress cache: {}", cacheKey);
        }
    }

    public UserProgressSnapshot getCourseProgress(String userId, String courseId) {
        try {
            String cacheKey = CACHE_PREFIX_COURSE + userId + CACHE_SUFFIX_COURSE + courseId;
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("Progress cache hit: {}", cacheKey);
                return objectMapper.readValue(cached, UserProgressSnapshot.class);
            }
            String key = COURSE_PROGRESS_KEY_PREFIX + userId + COURSE_PROGRESS_KEY_SUFFIX + courseId;
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            UserProgressSnapshot snapshot = objectMapper.readValue(value, UserProgressSnapshot.class);
            redisTemplate.opsForValue().set(cacheKey, value, cacheTtlSeconds, TimeUnit.SECONDS);
            return snapshot;
        } catch (JsonProcessingException e) {
            log.error("Failed to get course progress for userId: {}, courseId: {}", userId, courseId, e);
            return null;
        }
    }

    public UserProgressSnapshot getPathProgress(String userId, String pathId) {
        try {
            String cacheKey = CACHE_PREFIX_PATH + userId + CACHE_SUFFIX_PATH + pathId;
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("Progress cache hit: {}", cacheKey);
                return objectMapper.readValue(cached, UserProgressSnapshot.class);
            }
            String key = PATH_PROGRESS_KEY_PREFIX + userId + PATH_PROGRESS_KEY_SUFFIX + pathId;
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            UserProgressSnapshot snapshot = objectMapper.readValue(value, UserProgressSnapshot.class);
            redisTemplate.opsForValue().set(cacheKey, value, cacheTtlSeconds, TimeUnit.SECONDS);
            return snapshot;
        } catch (JsonProcessingException e) {
            log.error("Failed to get path progress for userId: {}, pathId: {}", userId, pathId, e);
            return null;
        }
    }
}

