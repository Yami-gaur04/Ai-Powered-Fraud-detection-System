// Thin fetch wrapper around the Spring Boot API: adds the JWT, normalises errors,
// reports connection status and signals when the session has expired.
export const API_BASE = (import.meta.env.VITE_API_URL || "http://localhost:8080/api").replace(/\/$/, "");

const TOKEN_KEY = "fd_token";
export const tokenStore = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (t) => localStorage.setItem(TOKEN_KEY, t),
  clear: () => localStorage.removeItem(TOKEN_KEY),
};

/* connection status (null = unknown, true = reachable, false = unreachable) */
let status = null;
const listeners = new Set();
function setStatus(v) {
  if (status !== v) { status = v; listeners.forEach((l) => l()); }
}
export const connection = {
  subscribe: (l) => { listeners.add(l); return () => listeners.delete(l); },
  getSnapshot: () => status,
};

let unauthorizedHandler = null;
export const onUnauthorized = (fn) => { unauthorizedHandler = fn; };

export class ApiError extends Error {
  constructor(message, status) { super(message); this.name = "ApiError"; this.status = status; }
}

export async function request(path, { method = "GET", body, auth = true } = {}) {
  const headers = { "Content-Type": "application/json" };
  const token = tokenStore.get();
  if (auth && token) headers.Authorization = `Bearer ${token}`;

  let res;
  try {
    res = await fetch(API_BASE + path, { method, headers, body: body ? JSON.stringify(body) : undefined });
  } catch {
    setStatus(false);
    throw new ApiError(`Cannot reach the backend at ${API_BASE}. Is the Spring Boot server running?`, 0);
  }
  setStatus(true);

  const text = await res.text();
  let data = null;
  if (text) { try { data = JSON.parse(text); } catch { data = text; } }

  if (res.status === 401 && auth && token) {
    unauthorizedHandler?.();
    throw new ApiError("Your session has expired. Please sign in again.", 401);
  }
  if (res.status === 403) throw new ApiError("You don't have permission to do that.", 403);
  if (!res.ok) {
    let msg = data && typeof data === "object" ? data.message : null;
    if (msg && typeof msg === "object") msg = Object.values(msg).join(", ");
    throw new ApiError(msg || `Request failed (${res.status})`, res.status);
  }
  return data;
}
