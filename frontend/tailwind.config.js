/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Azul institucional (CNSC/DIAN) como color de marca.
        brand: {
          50:  '#eef5fc',
          100: '#d6e6f7',
          200: '#aecef0',
          300: '#7eaee4',
          400: '#4f8ad4',
          500: '#2e6ebd',
          600: '#1f5aa3',
          700: '#17487f',   // tono principal de acciones
          800: '#123a66',
          900: '#0d2b4d',
          950: '#081d35',
        },
        // Alias legado para no romper usos existentes de #003F7F.
        dian: {
          blue:  '#17487f',
          gold:  '#C8A951',
        },
      },
      fontSize: {
        // Base 16px; textos de apoyo nunca por debajo de 14px.
        base: ['1rem', { lineHeight: '1.6' }],
      },
      borderRadius: {
        xl: '0.875rem',
        '2xl': '1.125rem',
      },
      boxShadow: {
        card: '0 1px 3px rgba(16, 42, 67, 0.08), 0 1px 2px rgba(16, 42, 67, 0.04)',
        'card-hover': '0 10px 25px -5px rgba(16, 42, 67, 0.12), 0 4px 10px -4px rgba(16, 42, 67, 0.08)',
      },
      keyframes: {
        'fade-in': {
          '0%': { opacity: '0', transform: 'translateY(6px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
      },
      animation: {
        'fade-in': 'fade-in 0.3s ease-out both',
      },
    },
  },
  plugins: [],
}
