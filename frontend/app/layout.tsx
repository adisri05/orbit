import type { Metadata } from 'next'
import './globals.css'

export const metadata: Metadata = {
  title: 'ORBIT - Learning Platform',
  description: 'A calm, intelligent learning platform focused on your growth',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  )
}

