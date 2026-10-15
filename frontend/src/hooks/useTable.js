import { useEffect, useMemo, useState } from "react";

/** Client-side sorting + pagination. getters maps a sort key to a value extractor. */
export function useTable(rows, { pageSize = 10, initialSort = { key: null, dir: "desc" }, getters = {} } = {}) {
  const [sort, setSort] = useState(initialSort);
  const [page, setPage] = useState(1);

  const sorted = useMemo(() => {
    if (!sort.key) return rows;
    const get = getters[sort.key] || ((r) => r[sort.key]);
    const arr = [...rows].sort((a, b) => {
      const x = get(a), y = get(b);
      if (x == null) return 1;
      if (y == null) return -1;
      return x > y ? 1 : x < y ? -1 : 0;
    });
    return sort.dir === "asc" ? arr : arr.reverse();
  }, [rows, sort]); // eslint-disable-line react-hooks/exhaustive-deps

  const pages = Math.max(1, Math.ceil(sorted.length / pageSize));
  useEffect(() => { if (page > pages) setPage(pages); }, [pages, page]);
  useEffect(() => { setPage(1); }, [rows.length]);

  const toggle = (key) => setSort((s) => (s.key === key ? { key, dir: s.dir === "asc" ? "desc" : "asc" } : { key, dir: "desc" }));
  return { pageRows: sorted.slice((page - 1) * pageSize, page * pageSize), sorted, page, setPage, pages, total: sorted.length, pageSize, sort, toggle };
}
