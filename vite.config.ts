import { defineConfig, loadEnv, type Connect, type Plugin, type ViteDevServer } from 'vite';
import react from '@vitejs/plugin-react';
import { VitePWA } from 'vite-plugin-pwa';
import type { IncomingMessage, ServerResponse } from 'node:http';

/**
 * En desarrollo, sirve /api con el mismo código que la función de Vercel (server/routes.ts),
 * conectado al Postgres local de .env.local.
 */
function localApi(): Plugin {
  const middleware =
    (load: () => Promise<{ handle: (request: Request) => Promise<Response> }>): Connect.NextHandleFunction =>
    async (req: IncomingMessage, res: ServerResponse, next) => {
      if (!req.url || !(req.url === '/api' || req.url.startsWith('/api/') || req.url.startsWith('/api?'))) return next();
      try {
        const { handle } = await load();
        const chunks: Buffer[] = [];
        for await (const chunk of req) chunks.push(chunk as Buffer);
        const headers = new Headers();
        for (const [key, value] of Object.entries(req.headers)) {
          if (Array.isArray(value)) value.forEach((v) => headers.append(key, v));
          else if (value != null) headers.set(key, value);
        }
        const hasBody = req.method !== 'GET' && req.method !== 'HEAD';
        const response = await handle(
          new Request(`http://${req.headers.host}${req.url}`, { method: req.method, headers, body: hasBody ? Buffer.concat(chunks) : undefined }),
        );
        res.statusCode = response.status;
        response.headers.forEach((value, key) => res.setHeader(key, value));
        res.end(Buffer.from(await response.arrayBuffer()));
      } catch (error) {
        next(error as Error);
      }
    };
  return {
    name: 'bellas-local-api',
    configureServer(server: ViteDevServer) {
      const env = loadEnv('development', process.cwd(), '');
      process.env.DATABASE_URL ??= env.DATABASE_URL;
      server.middlewares.use(middleware(() => server.ssrLoadModule('/server/routes.ts') as never));
    },
  };
}

export default defineConfig({
  plugins: [
    localApi(),
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
        navigateFallbackDenylist: [/^\/api/],
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
