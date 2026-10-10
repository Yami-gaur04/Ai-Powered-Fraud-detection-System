import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { tokenStore, onUnauthorized } from "../api/client";
import { useToast } from "./ToastContext";

const AuthContext = createContext(null);

function decode(token) {
  try {
    const payload = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
    return JSON.parse(atob(payload));
  } catch { return null; }
}

function loadSession() {
  const token = tokenStore.get();
  const raw = localStorage.getItem("fd_user");
  if (!token || !raw) return null;
  try {
    const user = JSON.parse(raw);
    const claims = decode(token);
    const exp = claims?.exp ? claims.exp * 1000 : null;
    if (exp && exp < Date.now()) return null;
    return { ...user, exp };
  } catch { return null; }
}

export function AuthProvider({ children }) {
  const [session, setSession] = useState(loadSession);
  const toast = useToast();

  const logout = useCallback((message) => {
    tokenStore.clear();
    localStorage.removeItem("fd_user");
    setSession(null);
    if (typeof message === "string") toast.info(message);
  }, [toast]);

  const signIn = useCallback((res) => {
    tokenStore.set(res.token);
    localStorage.setItem("fd_user", JSON.stringify({ name: res.name, email: res.email, role: res.role }));
    const s = loadSession();
    setSession(s);
    return s;
  }, []);

  // any 401 from the API logs the user out
  useEffect(() => { onUnauthorized(() => logout("Session expired. Please sign in again.")); }, [logout]);

  // log out automatically when the JWT expires
  useEffect(() => {
    if (!session?.exp) return;
    const ms = session.exp - Date.now();
    if (ms <= 0) { logout("Session expired. Please sign in again."); return; }
    const t = setTimeout(() => logout("Session expired. Please sign in again."), Math.min(ms, 2 ** 31 - 1));
    return () => clearTimeout(t);
  }, [session, logout]);

  const value = useMemo(() => ({ user: session, signIn, logout }), [session, signIn, logout]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
export const useAuth = () => useContext(AuthContext);
