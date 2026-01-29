# ORBIT Frontend

A calm, intelligent learning platform frontend built with Next.js, React, and TypeScript.

## Core Principles

- **Pure Renderer**: Frontend performs no business logic, calculations, or recommendations
- **Backend-Driven**: All data comes from backend APIs
- **Thin Client**: Frontend only fetches, renders, and triggers APIs

## Tech Stack

- **Next.js 14** (App Router)
- **React 18**
- **TypeScript**
- **Tailwind CSS**
- **Framer Motion** (subtle animations)
- **Axios** (REST API calls)

## Design Philosophy

- Light, calm, minimal UI
- Soft off-white background
- Muted greens, blues, and pastels
- Rounded cards with subtle shadows
- Smooth fade-in and slide-up transitions
- Hover elevation effects on cards
- Zero harsh contrast

## Getting Started

### Prerequisites

- Node.js 18+ 
- npm or yarn
- Backend services running (see backend READMEs)

### Installation

```bash
cd frontend
npm install
```

### Environment Variables

Create a `.env.local` file in the frontend directory:

```env
NEXT_PUBLIC_AUTH_SERVICE_URL=http://localhost:8081
NEXT_PUBLIC_PROGRESS_SERVICE_URL=http://localhost:8082
NEXT_PUBLIC_ANALYTICS_SERVICE_URL=http://localhost:8083
NEXT_PUBLIC_RECOMMENDATION_SERVICE_URL=http://localhost:8084
```

### Development

```bash
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) in your browser.

### Build

```bash
npm run build
npm start
```

## Project Structure

```
frontend/
├── app/                    # Next.js App Router pages
│   ├── login/             # Login page
│   ├── dashboard/         # Dashboard with progress and recommendations
│   ├── paths/             # Learning paths listing
│   │   └── [pathId]/     # Path detail page
│   ├── progress/          # Progress & Analytics page
│   ├── activity/          # Activity feed
│   └── settings/          # Settings page
├── components/            # React components
│   └── layout/           # Layout components (Sidebar, Header)
├── lib/                   # Utilities and API clients
│   ├── api/              # API client layer
│   │   ├── auth.ts       # Authentication API
│   │   ├── learning.ts   # Learning paths API
│   │   ├── progress.ts   # Progress API
│   │   ├── analytics.ts  # Analytics API
│   │   └── recommendation.ts # Recommendation API
│   └── types.ts          # TypeScript types
└── middleware.ts         # Route protection middleware
```

## API Integration

The frontend integrates with the following backend services:

1. **Auth Service** (port 8081)
   - Login: `POST /api/auth/login`
   - Register: `POST /api/auth/register`

2. **Learning Path Service** (port 8081)
   - Get all paths: `GET /paths`
   - Get courses by path: `GET /paths/{pathId}/courses`

3. **Progress Service** (port 8082)
   - Get course progress: `GET /progress/users/{userId}/courses/{courseId}`
   - Get path progress: `GET /progress/users/{userId}/paths/{pathId}`

4. **Analytics Service** (port 8083)
   - Get user analytics: `GET /analytics/users/{userId}`
   - Get course analytics: `GET /analytics/courses/{courseId}`
   - Get platform overview: `GET /analytics/platform/overview`

5. **Recommendation Service** (port 8084)
   - Get next recommendation: `GET /recommendations/users/{userId}/next`
   - Get all recommendations: `GET /recommendations/users/{userId}`

## Authentication

- JWT tokens are stored in `localStorage` and cookies
- Protected routes are handled by Next.js middleware
- Token is automatically included in API requests via Axios interceptors
- Unauthorized responses (401) automatically redirect to login

## Pages

### Login (`/login`)
- Email and password authentication
- Redirects to dashboard on success

### Dashboard (`/dashboard`)
- Overall progress statistics
- Current streak
- Lessons completed
- Learning time
- Today's focus (recommendation from backend)

### Learning Paths (`/paths`)
- List of all available learning paths
- Progress bars per path
- Resume/Start buttons

### Path Detail (`/paths/[pathId]`)
- Path metadata and description
- Path progress ring visualization
- List of courses in the path
- Course-level progress bars

### Progress & Analytics (`/progress`)
- Learning velocity
- Weekly consistency
- Current streak
- Skill mastery rings
- All data from backend (no frontend calculations)

### Activity Feed (`/activity`)
- Event-based activity feed
- Lesson completions
- Course completions
- Streak milestones
- Data from Analytics Service

## Constraints

✅ **No business logic** - All logic is in backend  
✅ **No progress calculations** - Frontend only displays backend data  
✅ **No recommendation inference** - Recommendations come from backend  
✅ **No fake analytics** - All data from backend APIs  
✅ **No optimistic updates** - State refetched after actions  

## Styling

- Tailwind CSS with custom color palette
- Soft shadows and rounded corners
- Subtle hover effects
- Smooth transitions with Framer Motion
- Responsive design

## License

Part of the ORBIT learning platform.

