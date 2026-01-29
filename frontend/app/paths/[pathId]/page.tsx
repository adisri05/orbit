'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { motion } from 'framer-motion';
import MainLayout from '@/components/layout/MainLayout';
import { getUserId } from '@/lib/api/config';
import { learningApi } from '@/lib/api/learning';
import { progressApi } from '@/lib/api/progress';
import { LearningPath, Course, UserProgressSnapshot } from '@/lib/types';
import { BookOpen, ArrowLeft, CheckCircle2, Circle } from 'lucide-react';

export default function PathDetailPage() {
  const params = useParams();
  const router = useRouter();
  const pathId = params.pathId as string;
  const userId = getUserId();
  const [path, setPath] = useState<LearningPath | null>(null);
  const [courses, setCourses] = useState<Course[]>([]);
  const [pathProgress, setPathProgress] = useState<UserProgressSnapshot | null>(null);
  const [courseProgresses, setCourseProgresses] = useState<Record<string, UserProgressSnapshot>>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!userId || !pathId) return;

    const fetchPathData = async () => {
      try {
        const [paths, fetchedCourses] = await Promise.all([
          learningApi.getAllLearningPaths(),
          learningApi.getCoursesByPathId(pathId),
        ]);
        const foundPath = paths.find((p) => p.id === pathId);
        setPath(foundPath || null);
        setCourses(fetchedCourses.sort((a, b) => a.orderIndex - b.orderIndex));

        const [pathProg, ...courseProgressResults] = await Promise.all([
          progressApi.getPathProgress(userId || '', pathId),
          ...fetchedCourses.map((course) =>
            progressApi.getCourseProgress(userId || '', course.id).then((p) => ({ courseId: course.id, progress: p }))
          ),
        ]);
        setPathProgress(pathProg);
        const progresses: Record<string, UserProgressSnapshot> = {};
        courseProgressResults.forEach(({ courseId, progress }) => {
          if (progress) progresses[courseId] = progress;
        });
        setCourseProgresses(progresses);
      } catch (error) {
        console.error('Failed to fetch path data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchPathData();
  }, [userId, pathId]);

  if (loading) {
    return (
      <MainLayout>
        <div className="flex items-center justify-center h-64">
          <div className="text-gray-500">Loading...</div>
        </div>
      </MainLayout>
    );
  }

  if (!path) {
    return (
      <MainLayout>
        <div className="text-center py-12">
          <p className="text-gray-500">Path not found.</p>
        </div>
      </MainLayout>
    );
  }

  const completionPercentage = pathProgress?.completionPercentage || 0;

  return (
    <MainLayout>
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        className="space-y-8"
      >
        <button
          onClick={() => router.back()}
          className="flex items-center gap-2 text-gray-600 hover:text-gray-800 transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          Back to Learning Paths
        </button>

        <div className="card">
          <div className="flex items-start gap-6 mb-6">
            <div className="p-4 bg-primary-100 rounded-card">
              <BookOpen className="w-8 h-8 text-primary-600" />
            </div>
            <div className="flex-1">
              <h1 className="text-3xl font-bold text-gray-800 mb-2">{path.title}</h1>
              <p className="text-gray-600 mb-2">{path.description}</p>
              <span className="inline-block px-3 py-1 bg-muted-100 text-gray-700 rounded-button text-sm">
                {path.level}
              </span>
            </div>
          </div>

          {/* Path Progress Ring */}
          <div className="flex items-center gap-6 mb-6">
            <div className="relative w-24 h-24">
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
                  transition={{ duration: 1 }}
                />
              </svg>
              <div className="absolute inset-0 flex items-center justify-center">
                <span className="text-lg font-bold text-gray-800">
                  {Math.round(completionPercentage)}%
                </span>
              </div>
            </div>
            <div>
              <p className="text-sm text-gray-600">Path Progress</p>
              <p className="text-2xl font-bold text-gray-800">
                {pathProgress?.completedLessonsCount || 0} / {pathProgress?.totalLessons || 0} lessons
              </p>
            </div>
          </div>
        </div>

        {/* Courses List */}
        <div>
          <h2 className="text-xl font-semibold text-gray-800 mb-4">Courses</h2>
          <div className="space-y-4">
            {courses.map((course, index) => {
              const courseProgress = courseProgresses[course.id];
              const courseCompletionPercentage = courseProgress?.completionPercentage || 0;

              return (
                <motion.div
                  key={course.id}
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: index * 0.1 }}
                  className="card card-hover"
                >
                  <div className="flex items-start gap-4">
                    <div className="mt-1">
                      {courseProgress && courseCompletionPercentage === 100 ? (
                        <CheckCircle2 className="w-6 h-6 text-primary-500" />
                      ) : (
                        <Circle className="w-6 h-6 text-gray-300" />
                      )}
                    </div>
                    <div className="flex-1">
                      <h3 className="text-lg font-semibold text-gray-800 mb-1">
                        {course.title}
                      </h3>
                      <p className="text-sm text-gray-600 mb-3">{course.description}</p>
                      {courseProgress && (
                        <div>
                          <div className="flex items-center justify-between text-sm mb-2">
                            <span className="text-gray-600">Progress</span>
                            <span className="font-medium text-gray-800">
                              {Math.round(courseCompletionPercentage)}%
                            </span>
                          </div>
                          <div className="w-full bg-gray-100 rounded-full h-2">
                            <motion.div
                              initial={{ width: 0 }}
                              animate={{ width: `${courseCompletionPercentage}%` }}
                              transition={{ duration: 0.5, delay: index * 0.1 }}
                              className="bg-primary-500 h-2 rounded-full"
                            />
                          </div>
                        </div>
                      )}
                    </div>
                  </div>
                </motion.div>
              );
            })}
          </div>
        </div>
      </motion.div>
    </MainLayout>
  );
}

