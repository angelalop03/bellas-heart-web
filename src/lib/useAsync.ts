import { useCallback, useEffect, useRef, useState, type DependencyList } from 'react';
import { friendlyError } from './supabase';

/** Carga datos asíncronos con estado de carga y error, y permite recargarlos. */
export function useAsync<T>(fn: () => Promise<T>, deps: DependencyList) {
  const [data, setData] = useState<T | undefined>(undefined);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const counter = useRef(0);

  // eslint-disable-next-line react-hooks/exhaustive-deps
  const run = useCallback(fn, deps);

  const reload = useCallback(async () => {
    const id = ++counter.current;
    setLoading(true);
    setError(null);
    try {
      const result = await run();
      if (id === counter.current) setData(result);
    } catch (e) {
      if (id === counter.current) setError(friendlyError(e));
    } finally {
      if (id === counter.current) setLoading(false);
    }
  }, [run]);

  useEffect(() => {
    reload();
  }, [reload]);

  return { data, error, loading, reload, setData };
}

/** Lanza el error de Supabase si lo hay y devuelve los datos. */
export function unwrap<T>(result: { data: T | null; error: unknown }): T {
  if (result.error) throw result.error;
  return result.data as T;
}
