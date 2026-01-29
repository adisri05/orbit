'use client';

import { useCallback, useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import MainLayout from '@/components/layout/MainLayout';
import { getUserId } from '@/lib/api/config';
import { analyticsApi } from '@/lib/api/analytics';
import { progressApi } from '@/lib/api/progress';
import { learningApi } from '@/lib/api/learning';
import { useRealtimeUpdates } from '@/lib/hooks/useRealtimeUpdates';
import { UserAnalytics, UserProgressSnapshot, LearningPath } from '@/lib/types';
import { TrendingUp, Calendar, Target, Award } from 'lucide-react';

export default function ProgressPage() {
  const userId = getUserId();
  const [userAnalytics, setUserAnalytics] = useState<UserAnalytics | null>(null);
  const [paths, setPaths] = useState<LearningPath[]>([]);
  const [pathProgresses, setPathProgresses] = useState<Record<string, UserProgressSnapshot>>({});
  const [loading, setLoading] = useState(true);

  const fetchProgressData = useCallback(async () => {
    if (!userId) return;
    try {
      const [analytics, fetchedPaths] = await Promise.all([
        analyticsApi.getUserAnalytics(userId || ''),
        learningApi.getAllLearningPaths(),
      ]);
      setUserAnalytics(analytics);
      setPaths(fetchedPaths);
      const progressResults = await Promise.all(
        fetchedPaths.map((path) =>
          progressApi.getPathProgress(userId || '', path.id).then((p) => ({ pathId: path.id, progress: p }))
        )
      );
      const progresses: Record<string, UserProgressSnapshot> = {};
      progressResults.forEach(({ pathId, progress }) => {
        if (progress) progresses[pathId] = progress;
      });
      setPathProgresses(progresses);
    } catch (error) {
      console.error('Failed to fetch progress data:', error);
    } finally {
      setLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    if (!userId) return;
    setLoading(true);
    fetchProgressData();
  }, [userId, fetchProgressData]);

  useRealtimeUpdates(userId, fetchProgressData);

  if (loading) {
    return (
      <MainLayout>
        <div className="flex items-center justify-center h-64">
          <div className="text-gray-500">Loading...</div>
        </div>
      </MainLayout>
    );
  }

  // Calculate overall stats (frontend only renders, doesn't compute)
  const totalLessonsCompleted = userAnalytics?.lessonsCompletedCount || 0;
  const totalLessonsStarted = userAnalytics?.lessonsStartedCount || 0;

  return (
    <MainLayout>
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        className="space-y-8"
      >
        <div>
          <h1 className="text-3xl font-bold text-gray-800 mb-2">Progress & Analytics</h1>
          <p className="text-gray-600">Track your learning journey and achievements</p>
        </div>

        {/* Overview Stats */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
            className="card card-hover"
          >
            <div className="flex items-center gap-4">
              <div className="p-3 bg-primary-100 rounded-button">
                <Target className="w-6 h-6 text-primary-600" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Lessons Completed</p>
                <p className="text-2xl font-bold text-gray-800">{totalLessonsCompleted}</p>
              </div>
            </div>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
            className="card card-hover"
          >
            <div className="flex items-center gap-4">
              <div className="p-3 bg-accent-100 rounded-button">
                <TrendingUp className="w-6 h-6 text-accent-600" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Lessons Started</p>
                <p className="text-2xl font-bold text-gray-800">{totalLessonsStarted}</p>
              </div>
            </div>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.3 }}
            className="card card-hover"
          >
            <div className="flex items-center gap-4">
              <div className="p-3 bg-muted-200 rounded-button">
                <Calendar className="w-6 h-6 text-gray-700" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Last Active</p>
                <p className="text-sm font-medium text-gray-800">
                  {userAnalytics?.lastActiveAt
                    ? new Date(userAnalytics.lastActiveAt).toLocaleDateString()
                    : 'Never'}
                </p>
              </div>
            </div>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.4 }}
            className="card card-hover"
          >
            <div className="flex items-center gap-4">
              <div className="p-3 bg-primary-100 rounded-button">
                <Award className="w-6 h-6 text-primary-600" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Active Paths</p>
                <p className="text-2xl font-bold text-gray-800">
                  {Object.keys(pathProgresses).length}
                </p>
              </div>
            </div>
          </motion.div>
        </div>

        {/* Skill Mastery Rings */}
        <div className="card">
          <h2 className="text-xl font-semibold text-gray-800 mb-6">Path Progress</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {paths.map((path, index) => {
              const progress = pathProgresses[path.id];
              const completionPercentage = progress?.completionPercentage || 0;

              return (
                <motion.div
                  key={path.id}
                  initial={{ opacity: 0, scale: 0.9 }}
                  animate={{ opacity: 1, scale: 1 }}
                  transition={{ delay: index * 0.1 }}
                  className="flex flex-col items-center p-4 bg-muted-50 rounded-card"
                >
                  <div className="relative w-24 h-24 mb-4">
                    <svg className="w-24 h-24 transform -rotate-90">
                      <circle
                        cx="48"
                        cy="48"
                        r="40"
                        stroke="#E5E5DB"
                        strokeWidth="8"
                        fill="none"
                      />
                      <motion.circle
                        cx="48"
                        cy="48"
                        r="40"
                        stroke="#4CBE73"
                        strokeWidth="8"
                        fill="none"
                        strokeDasharray={`${2 * Math.PI * 40}`}
                        initial={{ strokeDashoffset: 2 * Math.PI * 40 }}
                        animate={{ strokeDashoffset: 2 * Math.PI * 40 * (1 - completionPercentage / 100) }}
                        transition={{ duration: 1, delay: index * 0.1 }}
                      />
                    </svg>
                    <div className="absolute inset-0 flex items-center justify-center">
                      <span className="text-lg font-bold text-gray-800">
                        {Math.round(completionPercentage)}%
                      </span>
                    </div>
                  </div>
                  <h3 className="text-sm font-semibold text-gray-800 text-center mb-1">
                    {path.title}
                  </h3>
                  <p className="text-xs text-gray-600 text-center">
                    {progress?.completedLessonsCount || 0} / {progress?.totalLessons || 0} lessons
                  </p>
                </motion.div>
              );
            })}
          </div>
        </div>
      </motion.div>
    </MainLayout>
  );
}

