import { lazy, Suspense, type ReactNode } from 'react';
import { Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { useAuth } from './lib/auth';
import { supabaseConfigured } from './lib/supabase';
import { AppShell } from './components/AppShell';
import { Spinner } from './components/ui';
import { Welcome } from './pages/Welcome';
import { Login, Register, ForgotPassword, ResetPassword } from './pages/Auth';
import { Home } from './pages/Home';
import { Stories, StoryDetail } from './pages/NewGame';
import { Games, GameChapters } from './pages/Games';
import { HistoryList, HistoryDetail } from './pages/Historial';
import { Forum, NewsDetail } from './pages/Forum';
import { Resources } from './pages/Resources';
import { ProfilePage } from './pages/Profile';
import { Credits } from './pages/Credits';
import { NotFound } from './pages/NotFound';

const Play = lazy(() => import('./pages/Play'));
const Admin = lazy(() => import('./pages/Admin'));

function RequireAuth({ children }: { children: ReactNode }) {
  const { session, loading, recovering } = useAuth();
  const location = useLocation();
  if (loading) return <Spinner />;
  if (recovering) return <Navigate to="/nueva-contrasena" replace />;
  if (!session) return <Navigate to="/entrar" replace state={{ from: location.pathname }} />;
  return <>{children}</>;
}

function GuestOnly({ children }: { children: ReactNode }) {
  const { session, loading } = useAuth();
  if (loading) return <Spinner />;
  if (session) return <Navigate to="/inicio" replace />;
  return <>{children}</>;
}

export function App() {
  if (!supabaseConfigured) {
    return (
      <div className="page" style={{ paddingTop: 48 }}>
        <div className="alert alert-error">
          Falta la configuración de Supabase: define VITE_SUPABASE_URL y VITE_SUPABASE_ANON_KEY.
        </div>
      </div>
    );
  }
  return (
    <Suspense fallback={<Spinner />}>
      <Routes>
        <Route path="/" element={<GuestOnly><Welcome /></GuestOnly>} />
        <Route path="/entrar" element={<GuestOnly><Login /></GuestOnly>} />
        <Route path="/registro" element={<GuestOnly><Register /></GuestOnly>} />
        <Route path="/recuperar" element={<GuestOnly><ForgotPassword /></GuestOnly>} />
        <Route path="/nueva-contrasena" element={<ResetPassword />} />
        <Route path="/tutorial" element={<Play mode="tutorial" />} />
        <Route path="/jugar/:gameId" element={<RequireAuth><Play mode="game" /></RequireAuth>} />
        <Route path="/admin/*" element={<RequireAuth><Admin /></RequireAuth>} />

        {/* Recursos y créditos: también accesibles sin cuenta */}
        <Route element={<AppShell />}>
          <Route path="/recursos" element={<Resources />} />
          <Route path="/creditos" element={<Credits />} />
        </Route>

        <Route element={<RequireAuth><AppShell /></RequireAuth>}>
          <Route path="/inicio" element={<Home />} />
          <Route path="/nueva" element={<Stories />} />
          <Route path="/nueva/:historyId" element={<StoryDetail />} />
          <Route path="/partidas" element={<Games />} />
          <Route path="/partidas/:gameId" element={<GameChapters />} />
          <Route path="/historial" element={<HistoryList />} />
          <Route path="/historial/:gameId" element={<HistoryDetail />} />
          <Route path="/foro" element={<Forum />} />
          <Route path="/foro/:newsId" element={<NewsDetail />} />
          <Route path="/perfil" element={<ProfilePage />} />
        </Route>

        <Route path="*" element={<NotFound />} />
      </Routes>
    </Suspense>
  );
}
