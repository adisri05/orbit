import { learningClient } from './client';
import { LearningPath, Course } from '../types';

export const learningApi = {
  getAllLearningPaths: async (): Promise<LearningPath[]> => {
    const response = await learningClient.get<LearningPath[]>('/paths');
    return response.data;
  },

  getCoursesByPathId: async (pathId: string): Promise<Course[]> => {
    const response = await learningClient.get<Course[]>(`/paths/${pathId}/courses`);
    return response.data;
  },
};

