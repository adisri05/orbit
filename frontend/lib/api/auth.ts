import { authClient } from './client';
import { LoginRequest, LoginResponse } from '../types';
import { setAuthToken, setUserId } from './config';

const loginUser = async (credentials: LoginRequest): Promise<LoginResponse> => {
  const response = await authClient.post<LoginResponse>('/api/auth/login', credentials);
  const data = response.data;
  
  // Store token and user ID
  setAuthToken(data.token);
  setUserId(data.userId);
  
  return data;
};

export const authApi = {
  login: loginUser,

  register: async (credentials: { email: string; password: string; name: string; role?: string }): Promise<LoginResponse> => {
    // First register the user
    await authClient.post('/api/auth/register', {
      ...credentials,
      role: credentials.role || 'USER',
    });
    
    // Then automatically log them in
    return loginUser({
      email: credentials.email,
      password: credentials.password,
    });
  },
};

