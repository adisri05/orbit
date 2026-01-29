import axios, { AxiosInstance, AxiosError } from 'axios';
import { API_CONFIG, getAuthToken } from './config';

// Create axios instance with default config
const createApiClient = (baseURL: string): AxiosInstance => {
  const client = axios.create({
    baseURL,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  // Add auth token to requests
  client.interceptors.request.use((config) => {
    const token = getAuthToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });

  // Handle errors
  client.interceptors.response.use(
    (response) => response,
    (error: AxiosError) => {
      if (error.response?.status === 401) {
        // Unauthorized - clear auth and redirect to login
        if (typeof window !== 'undefined') {
          localStorage.removeItem('auth_token');
          localStorage.removeItem('user_id');
          window.location.href = '/login';
        }
      }
      return Promise.reject(error);
    }
  );

  return client;
};

export const authClient = createApiClient(API_CONFIG.AUTH_SERVICE);
export const progressClient = createApiClient(API_CONFIG.PROGRESS_SERVICE);
export const analyticsClient = createApiClient(API_CONFIG.ANALYTICS_SERVICE);
export const recommendationClient = createApiClient(API_CONFIG.RECOMMENDATION_SERVICE);
export const learningClient = createApiClient(API_CONFIG.AUTH_SERVICE); // Learning paths are on auth service

