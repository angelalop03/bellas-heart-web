import { NavLink, Link, Outlet } from 'react-router-dom';
import { BookHeart, Gamepad2, House, Newspaper, UserRound, LifeBuoy, ShieldCheck } from 'lucide-react';
import { useAuth } from '../lib/auth';

const NAV = [
  { to: '/inicio', label: 'Inicio', icon: House },
  { to: '/partidas', label: 'Partidas', icon: Gamepad2 },
  { to: '/historial', label: 'Historial', icon: BookHeart },
  { to: '/foro', label: 'Noticias', icon: Newspaper },
  { to: '/perfil', label: 'Perfil', icon: UserRound },
];

export function Brand() {
  const { user } = useAuth();
  return (
    <Link to={user ? '/inicio' : '/'} className="brand" aria-label="Bella's Heart, inicio">
      Bella's <span className="brand-heart">Heart</span>
    </Link>
  );
}

/** Cabecera + navegación inferior (arriba en pantallas grandes) para la zona de jugadora. */
export function AppShell() {
  const { user } = useAuth();
  return (
    <div className={`app-shell ${user ? '' : 'no-nav'}`}>
      <header className="topbar">
        <Brand />
        <div className="topbar-spacer" />
        {user?.role === 'admin' && (
          <Link to="/admin" className="btn btn-sm btn-ghost" aria-label="Administración">
            <ShieldCheck size={18} /> <span className="hide-sm">Admin</span>
          </Link>
        )}
        <Link to="/recursos" className="help-chip">
          <LifeBuoy size={16} /> Ayuda 016
        </Link>
      </header>
      <main>
        <Outlet />
      </main>
      {user && (
      <nav className="bottom-nav" aria-label="Navegación principal">
        {NAV.map(({ to, label, icon: Icon }) => (
          <NavLink key={to} to={to}>
            <Icon size={22} strokeWidth={2.1} />
            <span>{label}</span>
          </NavLink>
        ))}
      </nav>
      )}
    </div>
  );
}
