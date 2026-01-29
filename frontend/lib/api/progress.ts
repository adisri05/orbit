import { progressClient } from './client';
import { UserProgressSnapshot } from '../types';

export const progressApi = {
  getCourseProgress: async (userId: string, courseId: string): Promise<UserProgressSnapshot | null> => {
    try {
      const response = await progressClient.get<UserProgressSnapshot>(
        `/progress/users/${userId}/courses/${courseId}`
      );
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  getPathProgress: async (userId: string, pathId: string): Promise<UserProgressSnapshot | null> => {
    try {
      const response = await progressClient.get<UserProgressSnapshot>(
        `/progress/users/${userId}/paths/${pathId}`
      );
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },
};

