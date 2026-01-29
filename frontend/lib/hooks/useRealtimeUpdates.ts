'use client';

import { useEffect, useRef } from 'react';
import { API_CONFIG } from '@/lib/api/config';

/**
 * Optional SSE subscription for real-time feel. REST remains source of truth.
 * When progress or analytics are updated (after lesson completion etc.), refetch is called.
 * Never assume state is updated without backend confirmation (refetch from REST).
 */
export function useRealtimeUpdates(userId: string | null, refetch: () => void) {
  const refetchRef = useRef(refetch);
  refetchRef.current = refetch;

  useEffect(() => {
    if (!userId) return;

    const progressUrl = `${API_CONFIG.PROGRESS_SERVICE}/progress/stream?userId=${encodeURIComponent(userId)}`;
    const analyticsUrl = `${API_CONFIG.ANALYTICS_SERVICE}/analytics/stream?userId=${encodeURIComponent(userId)}`;

    const progressSource = new EventSource(progressUrl);
    const analyticsSource = new EventSource(analyticsUrl);

    const onUpdate = () => {
      refetchRef.current();
    };

    progressSource.addEventListener('progress_updated', onUpdate);
    analyticsSource.addEventListener('analytics_updated', onUpdate);

    return () => {
      progressSource.close();
      analyticsSource.close();
      progressSource.removeEventListener('progress_updated', onUpdate);
      analyticsSource.removeEventListener('analytics_updated', onUpdate);
    };
  }, [userId]);
}
