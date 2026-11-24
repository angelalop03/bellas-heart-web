import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { VitePWA } from 'vite-plugin-pwa';

export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['icons/*.png'],
      manifest: {
        name: "Bella's Heart",
        short_name: "Bella's Heart",
        description: 'Novela visual sobre las relaciones de pareja y la violencia de género.',
        lang: 'es',
        start_url: '/',
        scope: '/',
        display: 'standalone',
        orientation: 'any',
        background_color: '#1a0b1f',
        theme_color: '#1a0b1f',
        categories: ['games', 'education'],
        icons: [
          { src: '/icons/icon-192.png', sizes: '192x192', type: 'image/png', purpose: 'any' },
          { src: '/icons/icon-512.png', sizes: '512x512', type: 'image/png', purpose: 'any' },
          { src: '/icons/icon-maskable-512.png', sizes: '512x512', type: 'image/png', purpose: 'maskable' },
        ],
      },
      workbox: {
        // La app (HTML, JS, CSS, fuentes) queda guardada para abrir al instante y sin conexión.
        globPatterns: ['**/*.{js,css,html,woff2,png,svg}'],
        navigateFallback: '/index.html',
        runtimeCaching: [
          {
            // Fondos, personajes y música: se guardan la primera vez que se usan.
            urlPattern: ({ url }) => url.pathname.startsWith('/img/') || url.pathname.startsWith('/music/'),
            handler: 'CacheFirst',
            options: {
              cacheName: 'bh-media',
              expiration: { maxEntries: 200, maxAgeSeconds: 60 * 60 * 24 * 90 },
              rangeRequests: true,
              cacheableResponse: { statuses: [0, 200] },
            },
          },
        ],
      },
    }),
  ],
});
