import { Link, useParams } from 'react-router-dom';
import { Newspaper } from 'lucide-react';
import { supabase } from '../lib/supabase';
import { unwrap, useAsync } from '../lib/useAsync';
import { Backdrop, Empty, PageHead, Spinner, formatDate } from '../components/ui';
import { BACKDROPS } from '../lib/assets';
import type { News } from '../lib/types';

export function Forum() {
  const { data: news, loading, error } = useAsync(
    async () => unwrap(await supabase.from('news').select('*').order('created_at', { ascending: false }).order('id', { ascending: false })) as News[],
    [],
  );
  return (
    <div className="page">
      <Backdrop src={BACKDROPS.forum} />
      <PageHead title="Noticias" />
      <p className="muted">Actualidad sobre violencia de género para entender que lo que cuenta el juego también pasa fuera de él.</p>
      {loading && <Spinner />}
      {error && <div className="alert alert-error">{error}</div>}
      {news?.length === 0 && <Empty icon={<Newspaper size={36} color="var(--pink-400)" />} title="No hay noticias publicadas" />}
      <div className="stack">
        {news?.map((item) => (
          <Link key={item.id} to={`/foro/${item.id}`} className="card card-link stack" style={{ gap: 8 }}>
            <span className="faint">{formatDate(item.created_at)}</span>
            <h2 className="title-md">{item.title}</h2>
            <p className="muted clamp-3">{item.body}</p>
          </Link>
        ))}
      </div>
    </div>
  );
}

export function NewsDetail() {
  const { newsId } = useParams();
  const { data: item, loading, error } = useAsync(
    async () => unwrap(await supabase.from('news').select('*').eq('id', Number(newsId)).maybeSingle()) as News | null,
    [newsId],
  );
  return (
    <div className="page">
      <Backdrop src={BACKDROPS.forum} />
      <PageHead title="Noticia" back="/foro" />
      {loading && <Spinner />}
      {error && <div className="alert alert-error">{error}</div>}
      {!loading && !item && !error && <Empty title="Esta noticia ya no existe" />}
      {item && (
        <article className="glass article">
          <span className="faint">{formatDate(item.created_at)}</span>
          <h2 className="article-title">{item.title}</h2>
          {item.body.split(/\n+/).map((para, i) => (
            <p key={i}>{para}</p>
          ))}
        </article>
      )}
    </div>
  );
}
