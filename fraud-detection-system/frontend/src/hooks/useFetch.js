import { useCallback, useEffect, useRef, useState } from "react";

/**
 * Runs an async function on mount (and when deps change), optionally polling.
 * Returns { data, loading, error, reload, refresh }.
 *   reload  = refetch and show the loading state
 *   refresh = silent refetch in the background
 */
export function useFetch(fn, deps = [], { interval } = {}) {
  const [state, setState] = useState({ data: null, loading: true, error: null });
  const fnRef = useRef(fn);
  fnRef.current = fn;
  const reqId = useRef(0);

  const load = useCallback(async (silent = false) => {
    const id = ++reqId.current;
    if (!silent) setState((s) => ({ ...s, loading: true, error: null }));
    try {
      const data = await fnRef.current();
      if (id === reqId.current) setState({ data, loading: false, error: null });
    } catch (error) {
      if (id === reqId.current) setState((s) => ({ data: silent ? s.data : null, loading: false, error: silent ? s.error : error }));
    }
  }, []);

  useEffect(() => { load(false); return () => { reqId.current++; }; }, deps); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    if (!interval) return;
    const t = setInterval(() => load(true), interval);
    return () => clearInterval(t);
  }, [interval, load]);

  return { ...state, reload: () => load(false), refresh: () => load(true) };
}
