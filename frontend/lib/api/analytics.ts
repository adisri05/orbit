import { analyticsClient } from './client';
import { UserAnalytics, CourseAnalytics, PlatformAnalytics } from '../types';

export const analyticsApi = {
  getUserAnalytics: async (userId: string): Promise<UserAnalytics | null> => {
    try {
      const response = await analyticsClient.get<UserAnalytics>(`/analytics/users/${userId}`);
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  getCourseAnalytics: async (courseId: string): Promise<CourseAnalytics | null> => {
    try {
      const response = await analyticsClient.get<CourseAnalytics>(`/analytics/courses/${courseId}`);
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  getPlatformOverview: async (): Promise<PlatformAnalytics> => {
    const response = await analyticsClient.get<PlatformAnalytics>('/analytics/platform/overview');
    return response.data;
  },
};

