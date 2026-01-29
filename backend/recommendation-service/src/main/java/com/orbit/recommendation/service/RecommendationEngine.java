package com.orbit.recommendation.service;

import com.orbit.recommendation.model.Recommendation;
import com.orbit.recommendation.model.RecommendationType;
import com.orbit.recommendation.model.RuleType;
import com.orbit.recommendation.model.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationEngine {

    private static final double HIGH_DROPOFF_RATE = 0.3;
    private static final int INACTIVITY_THRESHOLD_DAYS = 7;

    /**
     * Generate recommendation by evaluating rules in priority order.
     * First matching rule returns the recommendation.
     */
    public Optional<Recommendation> generateRecommendation(UserContext context) {
        // Rule 1: Resume Incomplete Lesson (Highest Priority)
        Optional<Recommendation> rec = applyResumeIncompleteRule(context);
        if (rec.isPresent()) {
            log.debug("Applied rule: RESUME_INCOMPLETE for userId: {}", context.getUserId());
            return rec;
        }

        // Rule 2: Sequential Progress
        rec = applySequentialProgressRule(context);
        if (rec.isPresent()) {
            log.debug("Applied rule: SEQUENTIAL_PROGRESS for userId: {}", context.getUserId());
            return rec;
        }

        // Rule 3: Path Continuation
        rec = applyPathContinuationRule(context);
        if (rec.isPresent()) {
            log.debug("Applied rule: PATH_CONTINUATION for userId: {}", context.getUserId());
            return rec;
        }

        // Rule 4: Inactivity Nudge
        rec = applyInactivityNudgeRule(context);
        if (rec.isPresent()) {
            log.debug("Applied rule: INACTIVITY_NUDGE for userId: {}", context.getUserId());
            return rec;
        }

        // Rule 5: Consistency Reinforcement
        rec = applyConsistencyReinforcementRule(context);
        if (rec.isPresent()) {
            log.debug("Applied rule: CONSISTENCY_REINFORCEMENT for userId: {}", context.getUserId());
            return rec;
        }

        // Rule 6: Binge Control
        rec = applyBingeControlRule(context);
        if (rec.isPresent()) {
            log.debug("Applied rule: BINGE_CONTROL for userId: {}", context.getUserId());
            return rec;
        }

        // Rule 7: Drop-Off Avoidance
        rec = applyDropoffAvoidanceRule(context);
        if (rec.isPresent()) {
            log.debug("Applied rule: DROPOFF_AVOIDANCE for userId: {}", context.getUserId());
            return rec;
        }

        // Rule 8: Prerequisite Reinforcement
        rec = applyPrerequisiteReinforcementRule(context);
        if (rec.isPresent()) {
            log.debug("Applied rule: PREREQUISITE_REINFORCEMENT for userId: {}", context.getUserId());
            return rec;
        }

        // Rule 9: Exploration Boost
        rec = applyExplorationBoostRule(context);
        if (rec.isPresent()) {
            log.debug("Applied rule: EXPLORATION_BOOST for userId: {}", context.getUserId());
            return rec;
        }

        // Rule 10: Skill Diversification
        rec = applySkillDiversificationRule(context);
        if (rec.isPresent()) {
            log.debug("Applied rule: SKILL_DIVERSIFICATION for userId: {}", context.getUserId());
            return rec;
        }

        // Rule 11: Cold Start
        rec = applyColdStartRule(context);
        if (rec.isPresent()) {
            log.debug("Applied rule: COLD_START for userId: {}", context.getUserId());
            return rec;
        }

        // Rule 12: Safe Default (Lowest Priority)
        rec = applySafeDefaultRule(context);
        if (rec.isPresent()) {
            log.debug("Applied rule: SAFE_DEFAULT for userId: {}", context.getUserId());
            return rec;
        }

        return Optional.empty();
    }

    // Rule 1: Resume Incomplete Lesson
    private Optional<Recommendation> applyResumeIncompleteRule(UserContext context) {
        if (context.getStartedButIncompleteLessons() != null && 
            !context.getStartedButIncompleteLessons().isEmpty()) {
            String lessonId = context.getStartedButIncompleteLessons().get(0);
            return Optional.of(new Recommendation(
                RecommendationType.LESSON,
                lessonId,
                "Resume incomplete lesson",
                "You started this lesson but haven't completed it yet. Let's finish what you started!",
                0.9,
                RuleType.RESUME_INCOMPLETE
            ));
        }
        return Optional.empty();
    }

    // Rule 2: Sequential Progress
    private Optional<Recommendation> applySequentialProgressRule(UserContext context) {
        if (context.getActiveCourseId() != null && 
            context.getCompletedLessonsCount() > 0 &&
            context.getTotalLessonsInActiveCourse() > context.getCompletedLessonsCount()) {
            // Recommend next lesson (simplified - would need lesson order from content service)
            int nextLessonIndex = context.getCompletedLessonsCount() + 1;
            String nextLessonId = context.getActiveCourseId() + "-lesson-" + nextLessonIndex;
            return Optional.of(new Recommendation(
                RecommendationType.LESSON,
                nextLessonId,
                "Continue with next lesson",
                String.format("You've completed %d lessons. Continue with lesson %d in this course.",
                    context.getCompletedLessonsCount(), nextLessonIndex),
                0.85,
                RuleType.SEQUENTIAL_PROGRESS
            ));
        }
        return Optional.empty();
    }

    // Rule 3: Path Continuation
    private Optional<Recommendation> applyPathContinuationRule(UserContext context) {
        if (context.getActiveCourseId() != null &&
            context.getTotalLessonsInActiveCourse() > 0 &&
            context.getCompletedLessonsCount() >= context.getTotalLessonsInActiveCourse()) {
            // Course completed, recommend next course in path
            String nextCourseId = context.getActivePathId() + "-course-next";
            return Optional.of(new Recommendation(
                RecommendationType.COURSE,
                nextCourseId,
                "Continue to next course",
                "You've completed this course! Continue your learning journey with the next course in this path.",
                0.8,
                RuleType.PATH_CONTINUATION
            ));
        }
        return Optional.empty();
    }

    // Rule 4: Inactivity Nudge
    private Optional<Recommendation> applyInactivityNudgeRule(UserContext context) {
        if (context.isInactive() && context.getDaysSinceLastActivity() > INACTIVITY_THRESHOLD_DAYS) {
            // Recommend shortest pending lesson (simplified)
            String lessonId = context.getActiveCourseId() != null 
                ? context.getActiveCourseId() + "-lesson-1"
                : "starter-lesson-1";
            return Optional.of(new Recommendation(
                RecommendationType.LESSON,
                lessonId,
                "Get back on track",
                String.format("It's been %d days since your last activity. Start with a quick lesson to get back into the flow!",
                    context.getDaysSinceLastActivity()),
                0.75,
                RuleType.INACTIVITY_NUDGE
            ));
        }
        return Optional.empty();
    }

    // Rule 5: Consistency Reinforcement
    private Optional<Recommendation> applyConsistencyReinforcementRule(UserContext context) {
        if (context.isConsistentlyActive() && context.getActiveCourseId() != null) {
            // Recommend next lesson in most active course
            int nextLessonIndex = context.getCompletedLessonsCount() + 1;
            String nextLessonId = context.getActiveCourseId() + "-lesson-" + nextLessonIndex;
            return Optional.of(new Recommendation(
                RecommendationType.LESSON,
                nextLessonId,
                "Keep up the momentum",
                "You've been consistently active! Continue with the next lesson to maintain your learning streak.",
                0.8,
                RuleType.CONSISTENCY_REINFORCEMENT
            ));
        }
        return Optional.empty();
    }

    // Rule 6: Binge Control
    private Optional<Recommendation> applyBingeControlRule(UserContext context) {
        if (context.isBingeLearning()) {
            // Recommend lighter content or revision
            String lessonId = context.getActiveCourseId() != null
                ? context.getActiveCourseId() + "-review"
                : "review-content";
            return Optional.of(new Recommendation(
                RecommendationType.LESSON,
                lessonId,
                "Take a lighter approach",
                "You've been learning a lot! Consider reviewing previous lessons or taking a shorter, lighter lesson.",
                0.65,
                RuleType.BINGE_CONTROL
            ));
        }
        return Optional.empty();
    }

    // Rule 7: Drop-Off Avoidance
    private Optional<Recommendation> applyDropoffAvoidanceRule(UserContext context) {
        if (context.getCourseDropOffRate() != null && 
            context.getCourseDropOffRate() > HIGH_DROPOFF_RATE &&
            context.getActiveCourseId() != null) {
            // Recommend alternative course or prerequisite
            String alternativeCourseId = context.getActivePathId() + "-course-alternative";
            return Optional.of(new Recommendation(
                RecommendationType.COURSE,
                alternativeCourseId,
                "Try an alternative path",
                String.format("This course has a high drop-off rate (%.0f%%). Consider trying an alternative course or reviewing prerequisites.",
                    context.getCourseDropOffRate() * 100),
                0.7,
                RuleType.DROPOFF_AVOIDANCE
            ));
        }
        return Optional.empty();
    }

    // Rule 8: Prerequisite Reinforcement
    private Optional<Recommendation> applyPrerequisiteReinforcementRule(UserContext context) {
        // Simplified: if user has many incomplete lessons, recommend prerequisites
        if (context.getStartedButIncompleteLessons() != null &&
            context.getStartedButIncompleteLessons().size() >= 3) {
            String prerequisiteLessonId = context.getActiveCourseId() != null
                ? context.getActiveCourseId() + "-prerequisite-1"
                : "prerequisite-lesson-1";
            return Optional.of(new Recommendation(
                RecommendationType.LESSON,
                prerequisiteLessonId,
                "Strengthen your foundation",
                "You've started several lessons but haven't completed them. Consider reviewing prerequisite lessons to build a stronger foundation.",
                0.78,
                RuleType.PREREQUISITE_REINFORCEMENT
            ));
        }
        return Optional.empty();
    }

    // Rule 9: Exploration Boost
    private Optional<Recommendation> applyExplorationBoostRule(UserContext context) {
        // If user completed a major milestone (all lessons in course)
        if (context.getActiveCourseId() != null &&
            context.getTotalLessonsInActiveCourse() > 0 &&
            context.getCompletedLessonsCount() >= context.getTotalLessonsInActiveCourse()) {
            String relatedPathId = "related-path-" + context.getActivePathId();
            return Optional.of(new Recommendation(
                RecommendationType.PATH,
                relatedPathId,
                "Explore related topics",
                "Congratulations on completing this course! Explore a related learning path to expand your skills.",
                0.6,
                RuleType.EXPLORATION_BOOST
            ));
        }
        return Optional.empty();
    }

    // Rule 10: Skill Diversification
    private Optional<Recommendation> applySkillDiversificationRule(UserContext context) {
        // Simplified: if user has been on same path for too long (based on completion percentage)
        if (context.getActivePathId() != null &&
            context.getCompletionPercentage() > 0.8 &&
            context.getCompletionPercentage() < 1.0) {
            String complementaryPathId = "complementary-path";
            return Optional.of(new Recommendation(
                RecommendationType.PATH,
                complementaryPathId,
                "Diversify your skills",
                "You've made great progress on this path! Consider exploring a complementary learning path to broaden your skill set.",
                0.55,
                RuleType.SKILL_DIVERSIFICATION
            ));
        }
        return Optional.empty();
    }

    // Rule 11: Cold Start
    private Optional<Recommendation> applyColdStartRule(UserContext context) {
        if (context.isNewUser()) {
            String popularPathId = "popular-starter-path";
            return Optional.of(new Recommendation(
                RecommendationType.PATH,
                popularPathId,
                "Start your learning journey",
                "Welcome! Begin with our most popular starter path to get started on your learning journey.",
                0.5,
                RuleType.COLD_START
            ));
        }
        return Optional.empty();
    }

    // Rule 12: Safe Default
    private Optional<Recommendation> applySafeDefaultRule(UserContext context) {
        // Default: recommend next lesson from most recently active path
        if (context.getActivePathId() != null) {
            String lessonId = context.getActiveCourseId() != null
                ? context.getActiveCourseId() + "-lesson-next"
                : context.getActivePathId() + "-lesson-1";
            return Optional.of(new Recommendation(
                RecommendationType.LESSON,
                lessonId,
                "Continue learning",
                "Continue with the next lesson from your current learning path.",
                0.4,
                RuleType.SAFE_DEFAULT
            ));
        }
        return Optional.empty();
    }
}

