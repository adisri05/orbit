// Auth Types
export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  userId: string;
  email: string;
  role: string;
}

export interface User {
  userId: string;
  email: string;
  role: string;
}

// Learning Path Types
export interface LearningPath {
  id: string;
  title: string;
  description: string;
  level: string;
  createdAt: string;
}

export interface Course {
  id: string;
  title: string;
  description: string;
  learningPathId: string;
  orderIndex: number;
  createdAt: string;
}

// Progress Types
export interface UserProgressSnapshot {
  userId: string;
  pathId?: string;
  courseId?: string;
  totalLessons: number;
  completedLessonsCount: number;
  completionPercentage: number;
  lastUpdatedAt: string;
}

// Analytics Types
export interface UserAnalytics {
  userId: string;
  lessonsStartedCount: number;
  lessonsCompletedCount: number;
  lastActiveAt: string;
}

export interface CourseAnalytics {
  courseId: string;
  totalLessonStarts: number;
  totalLessonCompletions: number;
  dropOffCount: number;
}

export interface PlatformAnalytics {
  totalEventsProcessed: number;
  totalLessonCompletions: number;
}

// Recommendation Types
export enum RecommendationType {
  LESSON = 'LESSON',
  COURSE = 'COURSE',
  PATH = 'PATH',
}

export enum RuleType {
  RESUME_INCOMPLETE = 'RESUME_INCOMPLETE',
  SEQUENTIAL_PROGRESS = 'SEQUENTIAL_PROGRESS',
  PATH_CONTINUATION = 'PATH_CONTINUATION',
  INACTIVITY_NUDGE = 'INACTIVITY_NUDGE',
  CONSISTENCY_REINFORCEMENT = 'CONSISTENCY_REINFORCEMENT',
  BINGE_CONTROL = 'BINGE_CONTROL',
  DROPOFF_AVOIDANCE = 'DROPOFF_AVOIDANCE',
  PREREQUISITE_REINFORCEMENT = 'PREREQUISITE_REINFORCEMENT',
  EXPLORATION_BOOST = 'EXPLORATION_BOOST',
  SKILL_DIVERSIFICATION = 'SKILL_DIVERSIFICATION',
  COLD_START = 'COLD_START',
  SAFE_DEFAULT = 'SAFE_DEFAULT',
}

export interface Recommendation {
  type: RecommendationType;
  targetId: string;
  title: string;
  reason: string;
  confidence: number;
  ruleApplied: RuleType;
}

