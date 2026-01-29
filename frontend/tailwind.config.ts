import type { Config } from 'tailwindcss'

const config: Config = {
  content: [
    './pages/**/*.{js,ts,jsx,tsx,mdx}',
    './components/**/*.{js,ts,jsx,tsx,mdx}',
    './app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        background: '#FAFAF8',
        primary: {
          50: '#F0F9F4',
          100: '#DCF2E3',
          200: '#B8E5C7',
          300: '#94D8AB',
          400: '#70CB8F',
          500: '#4CBE73',
          600: '#3D985C',
          700: '#2E7245',
          800: '#1F4C2E',
          900: '#102617',
        },
        accent: {
          50: '#F0F4F9',
          100: '#DCE6F2',
          200: '#B8CDE5',
          300: '#94B4D8',
          400: '#709BCB',
          500: '#4C82BE',
          600: '#3D6898',
          700: '#2E4E72',
          800: '#1F344C',
          900: '#101A26',
        },
        muted: {
          50: '#F9F9F7',
          100: '#F2F2ED',
          200: '#E5E5DB',
          300: '#D8D8C9',
          400: '#CBCBB7',
          500: '#BEBEA5',
        },
      },
      borderRadius: {
        'card': '16px',
        'button': '12px',
      },
      boxShadow: {
        'soft': '0 2px 8px rgba(0, 0, 0, 0.04)',
        'elevated': '0 4px 16px rgba(0, 0, 0, 0.08)',
      },
    },
  },
  plugins: [],
}
export default config

