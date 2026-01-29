'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { motion } from 'framer-motion';
import MainLayout from '@/components/layout/MainLayout';
import { getUserId } from '@/lib/api/config';
import { learningApi } from '@/lib/api/learning';
import { progressApi } from '@/lib/api/progress';
import { LearningPath, UserProgressSnapshot } from '@/lib/types';
import { BookOpen, ArrowRight } from 'lucide-react';

export default function LearningPathsPage() {
  const router = useRouter();
  const userId = getUserId();
  const [paths, setPaths] = useState<LearningPath[]>([]);
  const [pathProgresses, setPathProgresses] = useState<Record<string, UserProgressSnapshot>>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!userId) return;

    const fetchPaths = async () => {
      try {
        const fetchedPaths = await learningApi.getAllLearningPaths();
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
        console.error('Failed to fetch learning paths:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchPaths();
  }, [userId]);

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
          <h1 className="text-3xl font-bold text-gray-800 mb-2">Learning Paths</h1>
          <p className="text-gray-600">Explore and continue your learning journeys</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {paths.map((path, index) => {
            const progress = pathProgresses[path.id];
            const completionPercentage = progress?.completionPercentage || 0;
            const hasProgress = progress !== undefined;

            return (
              <motion.div
                key={path.id}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.1 }}
                className="card card-hover cursor-pointer"
                onClick={() => router.push(`/paths/${path.id}`)}
              >
                <div className="flex items-start gap-4 mb-4">
                  <div className="p-3 bg-primary-100 rounded-button">
                    <BookOpen className="w-6 h-6 text-primary-600" />
                  </div>
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold text-gray-800 mb-1">
                      {path.title}
                    </h3>
                    <p className="text-sm text-gray-600 mb-2">{path.level}</p>
                  </div>
                </div>

                <p className="text-sm text-gray-600 mb-4 line-clamp-2">
                  {path.description}
                </p>

                {hasProgress && (
                  <div className="mb-4">
                    <div className="flex items-center justify-between text-sm mb-2">
                      <span className="text-gray-600">Progress</span>
                      <span className="font-medium text-gray-800">
                        {Math.round(completionPercentage)}%
                      </span>
                    </div>
                    <div className="w-full bg-gray-100 rounded-full h-2">
                      <motion.div
                        initial={{ width: 0 }}
                        animate={{ width: `${completionPercentage}%` }}
                        transition={{ duration: 0.5, delay: index * 0.1 }}
                        className="bg-primary-500 h-2 rounded-full"
                      />
                    </div>
                  </div>
                )}

                <button className="flex items-center gap-2 text-primary-600 hover:text-primary-700 font-medium text-sm mt-4">
                  {hasProgress ? 'Continue' : 'Start'}
                  <ArrowRight className="w-4 h-4" />
                </button>
              </motion.div>
            );
          })}
        </div>

        {paths.length === 0 && (
          <div className="text-center py-12">
            <p className="text-gray-500">No learning paths available yet.</p>
          </div>
        )}
      </motion.div>
    </MainLayout>
  );
}

