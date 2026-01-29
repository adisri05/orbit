'use client';

import { getUserId } from '@/lib/api/config';
import { removeAuthToken, removeUserId } from '@/lib/api/config';
import { useRouter } from 'next/navigation';

export default function Header() {
  const router = useRouter();
  const userId = getUserId();

  const handleLogout = () => {
    removeAuthToken();
    removeUserId();
    router.push('/login');
  };

  return (
    <header className="fixed top-0 left-64 right-0 h-16 bg-white border-b border-gray-100 z-30">
      <div className="flex items-center justify-between h-full px-6">
        <div className="flex items-center gap-4">
          <h2 className="text-lg font-semibold text-gray-800">Welcome back</h2>
        </div>
        <div className="flex items-center gap-4">
          {userId && (
            <span className="text-sm text-gray-600">User ID: {userId}</span>
          )}
          <button
            onClick={handleLogout}
            className="text-sm text-gray-600 hover:text-gray-800 transition-colors"
          >
            Logout
          </button>
        </div>
      </div>
    </header>
  );
}

