# Bella's Heart

**Bella's Heart** es una novela visual sobre las relaciones de pareja y la violencia de género, creada como Trabajo Fin de Grado (Universidad de Sevilla). Acompañas a Bella y David, que acaban de mudarse juntos: tus decisiones cambian el estado emocional de Bella y el de la relación, y de ello dependen los capítulos y el final que vivirás. El objetivo es ayudar a reconocer las señales de una relación tóxica y dar a conocer los recursos de ayuda (016).

Esta versión es una **PWA**: se juega desde el navegador del móvil o del ordenador y se puede **añadir a la pantalla de inicio** como una app, sin App Store ni Google Play.

## Qué incluye

- **Historia completa** del juego original: introducción, capítulos 1–5 y ramas 6–9 según cómo evolucione la relación (confianza, tensión leve, tensión alta, crítica), con sus personajes y fondos.
- **Pantalla de juego** pensada para el móvil, en vertical y en horizontal: diálogos con efecto de escritura, opciones táctiles grandes, indicadores de cómo está Bella y cómo está la relación, música y transiciones de capítulo.
- **Partidas** guardadas en la nube: varias por cuenta, camino de capítulos (completados, actual y bloqueados) y borrado.
- **Historial** de decisiones por partida, con un filtro para ver solo las que aumentaron el malestar de Bella o la tensión.
- **Tutorial** jugable sin cuenta, **noticias**, **recursos de ayuda** (016, 112, Fundación Ana Bella…) siempre a mano y **perfil** (avatar, datos, contraseña, borrar cuenta).
- **Administración básica**: crear, editar y borrar noticias; ver jugadoras, sus partidas y decisiones, y eliminar cuentas.
- Cuentas con email y contraseña (Supabase Auth), recuperación de contraseña y seguridad por filas (RLS): cada jugadora solo ve sus datos y la lógica del juego se ejecuta en la base de datos.

## Tecnología

- [React](https://react.dev/) + [TypeScript](https://www.typescriptlang.org/) + [Vite](https://vite.dev/), con [vite-plugin-pwa](https://vite-pwa-org.netlify.app/) (manifest, service worker y modo sin conexión para la app).
- [Supabase](https://supabase.com/): PostgreSQL, Auth y funciones SQL (`create_game`, `choose_option`, `get_story`…).
- Despliegue en [Vercel](https://vercel.com/).
- Las ilustraciones originales se han convertido a WebP: de ~100 MB a ~4,5 MB, sin pérdida visible.

```
src/
  pages/        Pantallas (Play.tsx es la pantalla de juego)
  components/   Interfaz común (navegación, medidores, diálogos…)
  lib/          Supabase, sesión, historia, estados y motor del tutorial
public/
  img/          Fondos, personajes, capítulos y avatares (WebP)
  music/        Música del juego
supabase/
  migrations/   Esquema, seguridad (RLS) y lógica de la partida
  seed.sql      Contenido de la historia (capítulos, escenas, diálogos y opciones)
```

## Puesta en marcha

### 1. Supabase

1. Crea un proyecto gratuito en [supabase.com](https://supabase.com/).
2. En **SQL Editor**, ejecuta el contenido de `supabase/migrations/20261124000000_init.sql` y después el de `supabase/seed.sql`.
3. En **Authentication → URL Configuration**, pon en *Site URL* la dirección de la web en Vercel (por ejemplo `https://bellas-heart.vercel.app`) y añádela también en *Redirect URLs*.
4. En **Authentication → Sign In / Providers → Email**: el servidor de correo que trae Supabase de serie solo envía emails a los miembros del equipo del proyecto. Para abrir el registro a cualquiera, desactiva *Confirm email* o configura un SMTP propio (por ejemplo Resend o Brevo, con plan gratuito), que también hace falta para recuperar contraseñas.
5. Copia la *Project URL* y la clave pública (*anon* / *publishable*) de **Project Settings → API**.

Para hacerte administradora, regístrate en la app y ejecuta en el SQL Editor:

```sql
update public.profiles set role = 'admin' where username = 'tu_usuario';
```

### 2. Vercel

1. En [vercel.com](https://vercel.com/), **Add New → Project** e importa este repositorio (el framework *Vite* se detecta solo).
2. Añade las variables de entorno `VITE_SUPABASE_URL` y `VITE_SUPABASE_ANON_KEY` con los valores del paso anterior.
3. **Deploy**. Cada push a `main` vuelve a publicar la web.

### 3. En el móvil

- **Android (Chrome):** abre la web y toca *Añadir a la pantalla de inicio* en el perfil o en la portada (o menú ⋮ → *Instalar aplicación*).
- **iPhone (Safari):** botón *Compartir* → *Añadir a pantalla de inicio*.

## Desarrollo en local

Requisitos: Node 20+ y Docker (para Supabase local).

```bash
npm install
npm run db:start      # Supabase local con el esquema y la historia
cp .env.example .env.local   # y pon la URL y la clave que muestra db:start
npm run dev           # http://localhost:5173
```

Otros comandos: `npm run build` (compila la web en `dist/`), `npm run db:reset` (recrea la base de datos local desde cero) y `npm run db:stop`.

## Créditos

Desarrollada por **Ángela López Oliva** · Trabajo Fin de Grado · Grado en Ingeniería Informática – Ingeniería del Software, Universidad de Sevilla. Tutora: María del Carmen Romero Ternero. Cotutora: Rosa María Casado Mejía.
