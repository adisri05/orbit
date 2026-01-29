'use client';

import { useCallback, useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import MainLayout from '@/components/layout/MainLayout';
import { getUserId } from '@/lib/api/config';
import { progressApi } from '@/lib/api/progress';
import { analyticsApi } from '@/lib/api/analytics';
import { recommendationApi } from '@/lib/api/recommendation';
import { learningApi } from '@/lib/api/learning';
import { useRealtimeUpdates } from '@/lib/hooks/useRealtimeUpdates';
import { UserProgressSnapshot, UserAnalytics, Recommendation, LearningPath } from '@/lib/types';
import { BookOpen, Clock, Flame, TrendingUp } from 'lucide-react';

export default function DashboardPage() {
  const userId = getUserId();
  const [overallProgress, setOverallProgress] = useState<UserProgressSnapshot | null>(null);
  const [userAnalytics, setUserAnalytics] = useState<UserAnalytics | null>(null);
  const [recommendation, setRecommendation] = useState<Recommendation | null>(null);
  const [learningPaths, setLearningPaths] = useState<LearningPath[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchDashboardData = useCallback(async () => {
    if (!userId) return;
    try {
      const [analytics, rec, paths] = await Promise.all([
        analyticsApi.getUserAnalytics(userId || ''),
        recommendationApi.getNextRecommendation(userId || ''),
        learningApi.getAllLearningPaths(),
      ]);
      setUserAnalytics(analytics);
      setRecommendation(rec);
      setLearningPaths(paths);
      if (paths.length > 0) {
        const pathProgress = await progressApi.getPathProgress(userId || '', paths[0].id);
        setOverallProgress(pathProgress);
      }
    } catch (error) {
      console.error('Failed to fetch dashboard data:', error);
    } finally {
      setLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    if (!userId) return;
    setLoading(true);
    fetchDashboardData();
  }, [userId, fetchDashboardData]);

  useRealtimeUpdates(userId, fetchDashboardData);

  if (loading) {
    return (
      <MainLayout>
        <div className="flex items-center justify-center h-64">
          <div className="text-gray-500">Loading...</div>
        </div>
      </MainLayout>
    );
  }

  return (
    <MainLayout>
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        className="space-y-8"
      >
        <div>
          <h1 className="text-3xl font-bold text-gray-800 mb-2">Dashboard</h1>
          <p className="text-gray-600">Your learning journey at a glance</p>
        </div>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
            className="card card-hover"
          >
            <div className="flex items-center gap-4">
              <div className="p-3 bg-primary-100 rounded-button">
                <BookOpen className="w-6 h-6 text-primary-600" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Lessons Completed</p>
                <p className="text-2xl font-bold text-gray-800">
                  {userAnalytics?.lessonsCompletedCount || 0}
                </p>
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
                <Flame className="w-6 h-6 text-accent-600" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Current Streak</p>
                <p className="text-2xl font-bold text-gray-800">0</p>
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
                <Clock className="w-6 h-6 text-gray-700" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Learning Time</p>
                <p className="text-2xl font-bold text-gray-800">--</p>
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
                <TrendingUp className="w-6 h-6 text-primary-600" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Overall Progress</p>
                <p className="text-2xl font-bold text-gray-800">
                  {overallProgress ? `${Math.round(overallProgress.completionPercentage)}%` : '0%'}
                </p>
              </div>
            </div>
          </motion.div>
        </div>

        {/* Today's Focus */}
        {recommendation && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.5 }}
            className="card"
          >
            <h2 className="text-xl font-semibold text-gray-800 mb-4">Today's Focus</h2>
            <div className="bg-primary-50 rounded-card p-6 border border-primary-100">
              <div className="flex items-start gap-4">
                <div className="p-3 bg-primary-500 rounded-button text-white">
                  <BookOpen className="w-6 h-6" />
                </div>
                <div className="flex-1">
                  <h3 className="text-lg font-semibold text-gray-800 mb-1">
                    {recommendation.title}
                  </h3>
                  <p className="text-gray-600 mb-3">{recommendation.reason}</p>
                  <div className="flex items-center gap-4 text-sm text-gray-500">
                    <span>Type: {recommendation.type}</span>
                    <span>Confidence: {Math.round(recommendation.confidence * 100)}%</span>
                  </div>
                </div>
              </div>
            </div>
          </motion.div>
        )}
      </motion.div>
    </MainLayout>
  );
}

