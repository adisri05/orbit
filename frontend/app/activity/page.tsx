'use client';

import { useCallback, useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import MainLayout from '@/components/layout/MainLayout';
import { getUserId } from '@/lib/api/config';
import { analyticsApi } from '@/lib/api/analytics';
import { useRealtimeUpdates } from '@/lib/hooks/useRealtimeUpdates';
import { UserAnalytics } from '@/lib/types';
import { CheckCircle2, BookOpen, Trophy, Calendar } from 'lucide-react';

export default function ActivityPage() {
  const userId = getUserId();
  const [userAnalytics, setUserAnalytics] = useState<UserAnalytics | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchActivityData = useCallback(async () => {
    if (!userId) return;
    try {
      const analytics = await analyticsApi.getUserAnalytics(userId || '');
      setUserAnalytics(analytics);
    } catch (error) {
      console.error('Failed to fetch activity data:', error);
    } finally {
      setLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    if (!userId) return;
    setLoading(true);
    fetchActivityData();
  }, [userId, fetchActivityData]);

  useRealtimeUpdates(userId, fetchActivityData);

  if (loading) {
    return (
      <MainLayout>
        <div className="flex items-center justify-center h-64">
          <div className="text-gray-500">Loading...</div>
        </div>
      </MainLayout>
    );
  }

  // Generate activity feed items (simplified - in real app, this would come from analytics service)
  const activityItems = [
    {
      id: '1',
      type: 'lesson_completed',
      title: 'Completed a lesson',
      description: 'You completed a lesson in your learning path',
      icon: CheckCircle2,
      color: 'text-primary-600',
      bgColor: 'bg-primary-50',
      timestamp: userAnalytics?.lastActiveAt || new Date().toISOString(),
    },
    {
      id: '2',
      type: 'course_started',
      title: 'Started a new course',
      description: 'You began your learning journey',
      icon: BookOpen,
      color: 'text-accent-600',
      bgColor: 'bg-accent-50',
      timestamp: new Date(Date.now() - 86400000).toISOString(), // 1 day ago
    },
    {
      id: '3',
      type: 'milestone',
      title: 'Milestone reached',
      description: `Completed ${userAnalytics?.lessonsCompletedCount || 0} lessons`,
      icon: Trophy,
      color: 'text-yellow-600',
      bgColor: 'bg-yellow-50',
      timestamp: new Date(Date.now() - 172800000).toISOString(), // 2 days ago
    },
  ];

  return (
    <MainLayout>
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        className="space-y-8"
      >
        <div>
          <h1 className="text-3xl font-bold text-gray-800 mb-2">Activity Feed</h1>
          <p className="text-gray-600">Your learning journey timeline</p>
        </div>

        <div className="space-y-4">
          {activityItems.map((item, index) => {
            const Icon = item.icon;
            return (
              <motion.div
                key={item.id}
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: index * 0.1 }}
                className="card card-hover"
              >
                <div className="flex items-start gap-4">
                  <div className={`p-3 ${item.bgColor} rounded-button`}>
                    <Icon className={`w-6 h-6 ${item.color}`} />
                  </div>
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold text-gray-800 mb-1">
                      {item.title}
                    </h3>
                    <p className="text-gray-600 mb-2">{item.description}</p>
                    <div className="flex items-center gap-2 text-sm text-gray-500">
                      <Calendar className="w-4 h-4" />
                      <span>
                        {new Date(item.timestamp).toLocaleDateString('en-US', {
                          year: 'numeric',
                          month: 'long',
                          day: 'numeric',
                          hour: '2-digit',
                          minute: '2-digit',
                        })}
                      </span>
                    </div>
                  </div>
                </div>
              </motion.div>
            );
          })}
        </div>

        {activityItems.length === 0 && (
          <div className="text-center py-12">
            <p className="text-gray-500">No activity to display yet.</p>
          </div>
        )}
      </motion.div>
    </MainLayout>
  );
}

