<div align="center">

<img src="public/img/ui/logo.webp" alt="Bella's Heart" width="320">

### Una novela visual sobre las relaciones de pareja y la violencia de género

**▶ Jugar ahora: [bellas-heart.vercel.app](https://bellas-heart.vercel.app)**

Se juega desde el navegador del móvil o del ordenador y se instala en la pantalla de inicio como una app, sin App Store ni Google Play.

<img src="docs/screenshots/juego-horizontal.jpg" alt="Escena del juego en un móvil en horizontal" width="760">

</div>

---

## Sobre el juego

**Bella's Heart** nació como Trabajo Fin de Grado en la Universidad de Sevilla. Bella y David acaban de mudarse juntos y todo parece perfecto… pero no es oro todo lo que reluce. Tú eres Bella: en cada conversación eliges qué responder o qué hacer, y esas decisiones van cambiando cómo se siente ella y cómo evoluciona la relación.

El objetivo es **ayudar a reconocer las señales de una relación tóxica** (control, celos, aislamiento, culpabilización…) y **dar a conocer los recursos de ayuda**, como el 016.

### Cómo se juega

- **Cada decisión cuenta.** Dos indicadores muestran en todo momento cómo está Bella (*Estable → Algo inquieta → Ansiosa → Angustiada*) y la tensión con David (*Calma aparente → Tensión leve → Tensión alta → Muy alta*).
  - Ceder, justificarle, culparse o aislarse **aumenta el malestar** de Bella.
  - Pedir ayuda, mantener a sus amistades, cuidarse o reconocer una señal de alarma **lo reduce**.
  - Poner límites o enfrentarse a David **sube la tensión**: la calma no siempre es buena señal.
- **Algunas respuestas solo aparecen** según el estado emocional de Bella, igual que en la vida real no siempre tenemos fuerzas para contestar de la misma manera.
- **La historia se ramifica.** Tras la introducción y los capítulos 1 a 5, tus decisiones eligen qué versión de los capítulos 6, 7 y 8 vivirás. Hay cuatro caminos:
  - Si Bella se protege y planta cara, la tensión crece y ella acaba tomando distancia.
  - Si cede para evitar conflictos, todo parece tranquilo mientras el control de David se va cerrando sobre ella.
- **Al terminar**, el historial te muestra qué decisiones aumentaron o redujeron el malestar y la tensión, para reflexionar sobre ellas.

## Capturas

<table>
  <tr>
    <td align="center"><img src="docs/screenshots/portada.jpg" width="230" alt="Portada"><br><sub>Portada: crear cuenta, entrar o probar el tutorial</sub></td>
    <td align="center"><img src="docs/screenshots/inicio.jpg" width="230" alt="Inicio"><br><sub>Inicio: seguir jugando donde lo dejaste</sub></td>
    <td align="center"><img src="docs/screenshots/juego-vertical.jpg" width="230" alt="Escena en vertical"><br><sub>Escena: diálogo y decisiones</sub></td>
  </tr>
  <tr>
    <td align="center"><img src="docs/screenshots/tutorial.jpg" width="230" alt="Tutorial"><br><sub>Tutorial (se puede jugar sin cuenta)</sub></td>
    <td align="center"><img src="docs/screenshots/nueva-partida.jpg" width="230" alt="Nueva partida"><br><sub>Nueva partida</sub></td>
    <td align="center"><img src="docs/screenshots/partidas.jpg" width="230" alt="Mis partidas"><br><sub>Mis partidas</sub></td>
  </tr>
  <tr>
    <td align="center"><img src="docs/screenshots/capitulos.jpg" width="230" alt="Camino de capítulos"><br><sub>Camino de capítulos de una partida</sub></td>
    <td align="center"><img src="docs/screenshots/final.jpg" width="230" alt="Final de la historia"><br><sub>Final de la historia</sub></td>
    <td align="center"><img src="docs/screenshots/historial.jpg" width="230" alt="Historial"><br><sub>Historial: señales de alerta</sub></td>
  </tr>
  <tr>
    <td align="center"><img src="docs/screenshots/recursos.jpg" width="230" alt="Recursos de ayuda"><br><sub>Recursos de ayuda (016, 112…)</sub></td>
    <td align="center"><img src="docs/screenshots/perfil.jpg" width="230" alt="Perfil"><br><sub>Perfil e instalación en el móvil</sub></td>
    <td align="center"><img src="docs/screenshots/admin.jpg" width="230" alt="Administración"><br><sub>Administración: noticias y jugadoras</sub></td>
  </tr>
</table>

## Funcionalidades

| | |
|---|---|
| 🎮 **Juego** | Historia completa del juego original (introducción, capítulos 1–5 y cuatro ramas en los capítulos 6–9), con sus personajes, fondos y música. Diálogos con efecto de escritura, opciones táctiles grandes y transiciones de capítulo. Funciona en vertical y en horizontal. |
| 💾 **Partidas** | Varias partidas por cuenta guardadas en la nube: sigues en el móvil donde lo dejaste en el ordenador. Camino de capítulos con los completados, el actual y los bloqueados. |
| 📖 **Historial** | Todas las decisiones de cada partida, con un filtro de *señales de alerta* (las que hicieron crecer el malestar de Bella o la tensión). |
| 🆘 **Ayuda** | Botón «Ayuda 016» siempre visible. Página de recursos con llamada directa al 016 y al 112, WhatsApp y email del 016, y enlaces a centros de atención y a la Fundación Ana Bella. |
| 📰 **Noticias** | Foro de noticias sobre violencia de género, gestionado desde administración. |
| 👤 **Cuenta** | Registro con email y contraseña, perfil con avatar, cambio de contraseña y borrado de la cuenta con todos sus datos. |
| 🛠️ **Administración** | Crear, editar y borrar noticias; ver jugadoras, sus partidas y decisiones; eliminar cuentas. |
| 📱 **PWA** | Se instala en la pantalla de inicio, se abre a pantalla completa y la app queda guardada en el dispositivo para abrir al instante. |

## Instalarla en el móvil

- **Android (Chrome):** abre [bellas-heart.vercel.app](https://bellas-heart.vercel.app) y toca **Añadir a la pantalla de inicio** en la portada o en el perfil (o menú ⋮ → *Instalar aplicación*).
- **iPhone / iPad (Safari):** botón **Compartir** → **Añadir a pantalla de inicio**.

## Cómo está hecha

```mermaid
flowchart LR
  A["📱 App React + Vite<br/>PWA en el navegador"] -->|"/api · JSON + cookie de sesión"| B["⚙️ Vercel Functions<br/>server/routes.ts"]
  B -->|"SQL · create_game, choose_option, get_story"| C[("🐘 Neon · PostgreSQL<br/>historia y partidas")]
```


- **Frontend:** [React](https://react.dev/) + [TypeScript](https://www.typescriptlang.org/) + [Vite](https://vite.dev/), con [vite-plugin-pwa](https://vite-pwa-org.netlify.app/) para el manifest y el *service worker*. La historia completa se descarga en una sola petición y se guarda en el dispositivo, así pasar de un diálogo a otro es instantáneo.
- **Servidor:** una función de [Vercel](https://vercel.com/) (`api/index.ts`, código en `server/`). Contraseñas con bcrypt, sesión en cookie `HttpOnly` firmada, protección CSRF, límite de intentos de inicio de sesión y control de acceso: cada jugadora solo ve y modifica sus propios datos.
- **Base de datos:** [Neon](https://neon.tech/) (PostgreSQL). La lógica de la partida (sumar puntos, avanzar escena, elegir la rama del capítulo siguiente…) vive en funciones SQL, así el progreso siempre es coherente aunque se juegue desde varios dispositivos.
- **Ilustraciones:** las originales del juego convertidas a WebP, de ~100 MB a ~4,5 MB sin pérdida visible.

```
src/            App React (src/pages/Play.tsx es la pantalla de juego)
server/         API: rutas, sesiones y acceso a la base de datos
api/index.ts    Función de Vercel que atiende /api/*
db/migrations/  001_schema.sql (tablas y lógica del juego) y 002_story.sql (la historia)
scripts/        migrate.mjs: aplica las migraciones pendientes en cada despliegue
public/         Ilustraciones (WebP), música e iconos
```

## Despliegue (Vercel + Neon)

1. En [vercel.com](https://vercel.com/): **Add New → Project** e importa este repositorio (detecta *Vite* solo).
2. En el proyecto: **Storage → Create Database → Neon**, conéctala con el prefijo `DATABASE` (crea la variable `DATABASE_URL`).
3. **Redeploy.** En cada despliegue, `npm run build` ejecuta `scripts/migrate.mjs`, que crea las tablas y carga la historia la primera vez y después solo aplica migraciones nuevas. No hace falta copiar claves ni ejecutar SQL a mano.

Para dar permisos de administración a una cuenta, en el **SQL Editor** de Neon:

```sql
update users set role = 'admin' where email = 'tu@email.com';
```

## Desarrollo en local

Requisitos: Node 20+ y Docker.

```bash
npm install
npm run db:start      # PostgreSQL local en Docker (puerto 55432)
npm run db:migrate    # crea las tablas y carga la historia
npm run dev           # http://localhost:5173 (la API /api funciona igual que en Vercel)
```

`.env.local` debe contener `DATABASE_URL=postgres://postgres:postgres@127.0.0.1:55432/bellas` (ver `.env.example`).

## Pendiente

- Recuperar la contraseña por email (necesita un servicio de correo como Resend).
- Editor visual de historias, capítulos y escenas en el panel de administración.

## ¿Necesitas ayuda?

Si estás sufriendo violencia de género, o conoces a alguien que la sufre, llama al **016**: gratuito, confidencial y disponible las 24 horas, en 53 idiomas. No deja rastro en la factura. También por WhatsApp en el **600 000 016** y por email en **016-online@igualdad.gob.es**. En caso de emergencia, **112**.

## Créditos

Desarrollada por **Ángela López Oliva** · Trabajo Fin de Grado · Grado en Ingeniería Informática – Ingeniería del Software, Universidad de Sevilla.
Tutora: María del Carmen Romero Ternero · Cotutora: Rosa María Casado Mejía.
