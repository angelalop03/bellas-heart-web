# Bella's Heart

**Bella's Heart** es una novela visual sobre las relaciones de pareja y la violencia de género, creada como Trabajo Fin de Grado (Universidad de Sevilla). Acompañas a Bella y David, que acaban de mudarse juntos: tus decisiones cambian el estado emocional de Bella y el de la relación, y de ello dependen los capítulos y el final que vivirás. El objetivo es ayudar a reconocer las señales de una relación tóxica y dar a conocer los recursos de ayuda (016).

Esta versión es una **PWA**: se juega desde el navegador del móvil o del ordenador y se puede **añadir a la pantalla de inicio** como una app, sin App Store ni Google Play.

### ▶ Jugar: **https://bellas-heart.vercel.app**

## Qué incluye

- **Historia completa** del juego original: introducción, capítulos 1–5 y ramas 6–9 según cómo evolucione la relación (confianza, tensión leve, tensión alta, crítica), con sus personajes y fondos.
- **Pantalla de juego** pensada para el móvil, en vertical y en horizontal: diálogos con efecto de escritura, opciones táctiles grandes, indicadores de cómo está Bella y cómo está la relación, música y transiciones de capítulo.
- **Partidas** guardadas en la nube: varias por cuenta, camino de capítulos (completados, actual y bloqueados) y borrado.
- **Historial** de decisiones por partida, con un filtro para ver solo las que aumentaron el malestar de Bella o la tensión.
- **Tutorial** jugable sin cuenta, **noticias**, **recursos de ayuda** (016, 112, Fundación Ana Bella…) siempre a mano y **perfil** (avatar, datos, contraseña, borrar cuenta).
- **Administración básica**: crear, editar y borrar noticias; ver jugadoras, sus partidas y decisiones, y eliminar cuentas.

## Tecnología

- [React](https://react.dev/) + [TypeScript](https://www.typescriptlang.org/) + [Vite](https://vite.dev/), con [vite-plugin-pwa](https://vite-pwa-org.netlify.app/) (manifest, service worker y la app disponible sin conexión).
- **Base de datos: [Neon](https://neon.tech/) (PostgreSQL)**. La lógica de la partida vive en funciones SQL (`create_game`, `choose_option`, `get_story`), así el progreso siempre es coherente.
- **Servidor: Vercel Functions** (`api/`, código en `server/`). Gestiona las cuentas (contraseñas con bcrypt, sesión en cookie `HttpOnly` firmada), protege contra CSRF, limita los intentos de inicio de sesión y comprueba que cada jugadora solo acceda a sus datos.
- Despliegue en [Vercel](https://vercel.com/). Las ilustraciones originales se han convertido a WebP: de ~100 MB a ~4,5 MB, sin pérdida visible.

```
src/            App React (src/pages/Play.tsx es la pantalla de juego)
server/         API: rutas, sesiones y acceso a la base de datos
api/index.ts    Función de Vercel que atiende /api/*
db/migrations/  001_schema.sql (tablas y lógica) y 002_story.sql (la historia)
scripts/        migrate.mjs: aplica las migraciones pendientes
public/         Imágenes (WebP), música e iconos
```

## Desplegar en Vercel + Neon

1. En [vercel.com](https://vercel.com/), **Add New → Project** e importa este repositorio (detecta *Vite* solo).
2. En el proyecto, **Storage → Create Database → Neon** y conéctala al proyecto. Vercel añade sola la variable `DATABASE_URL`.
3. **Redeploy**. En cada despliegue, `npm run build` ejecuta `scripts/migrate.mjs`, que crea las tablas y carga la historia la primera vez (y después solo aplica migraciones nuevas). No hace falta copiar claves ni ejecutar SQL a mano: la clave para firmar sesiones también se genera dentro de la base de datos.

Para hacerte administradora, regístrate en la web y ejecuta en el **SQL Editor** de Neon:

```sql
update users set role = 'admin' where email = 'tu@email.com';
```

### En el móvil

- **Android (Chrome):** abre la web y toca *Añadir a la pantalla de inicio* en la portada o en el perfil (o menú ⋮ → *Instalar aplicación*).
- **iPhone (Safari):** botón *Compartir* → *Añadir a pantalla de inicio*.

## Desarrollo en local

Requisitos: Node 20+ y Docker.

```bash
npm install
npm run db:start      # PostgreSQL local en Docker (puerto 55432)
npm run db:migrate    # crea las tablas y carga la historia
npm run dev           # http://localhost:5173 (la API /api funciona igual que en Vercel)
```

`.env.local` debe tener `DATABASE_URL=postgres://postgres:postgres@127.0.0.1:55432/bellas` (ver `.env.example`).

## Pendiente

- **Recuperar la contraseña por email**: necesita un servicio de correo (por ejemplo Resend). De momento se puede cambiar desde el perfil sabiendo la actual.

## Créditos

Desarrollada por **Ángela López Oliva** · Trabajo Fin de Grado · Grado en Ingeniería Informática – Ingeniería del Software, Universidad de Sevilla. Tutora: María del Carmen Romero Ternero. Cotutora: Rosa María Casado Mejía.
