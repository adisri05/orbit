// API Configuration
export const API_CONFIG = {
  AUTH_SERVICE: process.env.NEXT_PUBLIC_AUTH_SERVICE_URL || 'http://localhost:8081',
  PROGRESS_SERVICE: process.env.NEXT_PUBLIC_PROGRESS_SERVICE_URL || 'http://localhost:8082',
  ANALYTICS_SERVICE: process.env.NEXT_PUBLIC_ANALYTICS_SERVICE_URL || 'http://localhost:8083',
  RECOMMENDATION_SERVICE: process.env.NEXT_PUBLIC_RECOMMENDATION_SERVICE_URL || 'http://localhost:8084',
};

export const getAuthToken = (): string | null => {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('auth_token');
};

export const setAuthToken = (token: string): void => {
  if (typeof window === 'undefined') return;
  localStorage.setItem('auth_token', token);
  // Also set as cookie for middleware
  document.cookie = `auth_token=${token}; path=/; max-age=86400`; // 24 hours
};

export const removeAuthToken = (): void => {
  if (typeof window === 'undefined') return;
  localStorage.removeItem('auth_token');
  document.cookie = 'auth_token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';
};

export const getUserId = (): string | null => {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('user_id');
};

export const setUserId = (userId: string): void => {
  if (typeof window === 'undefined') return;
  localStorage.setItem('user_id', userId);
};

export const removeUserId = (): void => {
  if (typeof window === 'undefined') return;
  localStorage.removeItem('user_id');
};

