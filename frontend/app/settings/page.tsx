'use client';

import { motion } from 'framer-motion';
import MainLayout from '@/components/layout/MainLayout';
import { getUserId } from '@/lib/api/config';

export default function SettingsPage() {
  const userId = getUserId();

  return (
    <MainLayout>
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        className="space-y-8"
      >
        <div>
          <h1 className="text-3xl font-bold text-gray-800 mb-2">Settings</h1>
          <p className="text-gray-600">Manage your account preferences</p>
        </div>

        <div className="card">
          <h2 className="text-xl font-semibold text-gray-800 mb-4">Account Information</h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                User ID
              </label>
              <input
                type="text"
                value={userId || ''}
                disabled
                className="input-field bg-gray-50"
              />
            </div>
            <p className="text-sm text-gray-500">
              Settings functionality will be implemented as needed.
            </p>
          </div>
        </div>
      </motion.div>
    </MainLayout>
  );
}

