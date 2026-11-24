import { ExternalLink, Phone, ShieldAlert, MapPin, HeartHandshake } from 'lucide-react';
import { Backdrop, PageHead } from '../components/ui';
import { BACKDROPS } from '../lib/assets';
import { useAuth } from '../lib/auth';

const LINKS = [
  {
    icon: <ShieldAlert size={22} />,
    title: 'Web del 016',
    text: 'Información, asesoramiento jurídico y atención psicosocial, en 53 idiomas.',
    href: 'https://violenciagenero.igualdad.gob.es/informacion-3/recursos/telefono016/',
  },
  {
    icon: <MapPin size={22} />,
    title: 'Dónde acudir',
    text: 'Centros y servicios de atención a víctimas de violencia de género en Andalucía.',
    href: 'https://www.juntadeandalucia.es/temas/familias-igualdad/mujeres/violencia-genero/paginas/donde-acudir.html',
  },
  {
    icon: <HeartHandshake size={22} />,
    title: 'Fundación Ana Bella',
    text: 'Red de mujeres supervivientes que ayudan a otras mujeres a salir de la violencia.',
    href: 'https://www.fundacionanabella.org/',
  },
];

export function Resources() {
  const { session } = useAuth();
  return (
    <div className="page">
      <Backdrop src={BACKDROPS.resources} />
      <PageHead title="Recursos de ayuda" back={session ? '/inicio' : '/'} />
      <section className="glass help-hero">
        <p>
          Si estás sufriendo violencia de género, o conoces a alguien que la sufre, puedes pedir ayuda especializada.
        </p>
        <a href="tel:016" className="btn btn-primary btn-block help-call">
          <Phone size={22} /> Llamar al 016
        </a>
        <p className="faint">
          Gratuito, confidencial y disponible las 24 horas. <strong>No deja rastro en la factura</strong>; si te preocupa
          que alguien revise tu móvil, borra la llamada del registro. También por WhatsApp en el 600 000 016 (solo
          mensajes) y por email en 016-online@igualdad.gob.es.
        </p>
        <a href="tel:112" className="btn btn-danger btn-block">
          <Phone size={20} /> Emergencias: 112
        </a>
      </section>
      <div className="stack">
        {LINKS.map((link) => (
          <a key={link.href} href={link.href} target="_blank" rel="noreferrer" className="card card-link resource-link">
            <span className="quick-icon">{link.icon}</span>
            <span className="stack" style={{ gap: 2 }}>
              <span className="title-md">{link.title}</span>
              <span className="muted">{link.text}</span>
            </span>
            <ExternalLink size={18} className="faint" />
          </a>
        ))}
      </div>
    </div>
  );
}
