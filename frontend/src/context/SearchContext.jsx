import { createContext, useContext, useEffect, useState } from "react";
import { useLocation } from "react-router-dom";

const SearchContext = createContext({ query: "", setQuery: () => {} });

/** The top-bar search box. Pages read `query` and filter their own lists. */
export function SearchProvider({ children }) {
  const [query, setQuery] = useState("");
  const { pathname } = useLocation();
  useEffect(() => { setQuery(""); }, [pathname]);
  return <SearchContext.Provider value={{ query, setQuery }}>{children}</SearchContext.Provider>;
}
export const useSearch = () => useContext(SearchContext);

export const matches = (row, q, pick) => {
  if (!q) return true;
  const hay = (pick ? pick(row) : Object.values(row)).join(" ").toLowerCase();
  return hay.includes(q.trim().toLowerCase());
};
