import { recommendationClient } from './client';
import { Recommendation } from '../types';

export const recommendationApi = {
  getNextRecommendation: async (
    userId: string,
    courseId?: string,
    pathId?: string
  ): Promise<Recommendation | null> => {
    try {
      const params = new URLSearchParams();
      if (courseId) params.append('courseId', courseId);
      if (pathId) params.append('pathId', pathId);
      
      const url = `/recommendations/users/${userId}/next${params.toString() ? `?${params.toString()}` : ''}`;
      const response = await recommendationClient.get<Recommendation>(url);
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  getAllRecommendations: async (userId: string): Promise<Recommendation[]> => {
    try {
      const response = await recommendationClient.get<Recommendation[]>(`/recommendations/users/${userId}`);
      return response.data || [];
    } catch (error: any) {
      if (error.response?.status === 404) {
        return [];
      }
      throw error;
    }
  },
};

