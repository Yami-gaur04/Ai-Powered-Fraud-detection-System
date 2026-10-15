// One function per backend endpoint, grouped by controller.
import { request } from "./client";

export const authApi = {
  login: (body) => request("/auth/login", { method: "POST", body, auth: false }),
  register: (body) => request("/auth/register", { method: "POST", body, auth: false }),
};

export const txApi = {
  create: (body) => request("/transactions", { method: "POST", body }),
  mine: () => request("/transactions"),
  alerts: () => request("/transactions/alerts"),
};

export const adminApi = {
  config: () => request("/admin/config"),
  updateConfig: (body) => request("/admin/config", { method: "PUT", body }),
  cases: (status) => request("/admin/fraud-cases" + (status ? `?status=${encodeURIComponent(status)}` : "")),
  caseById: (id) => request(`/admin/fraud-cases/${id}`),
  review: (id, body) => request(`/admin/fraud-cases/${id}/review`, { method: "PUT", body }),
  reports: () => request("/admin/reports"),
  algorithms: () => request("/admin/algorithms"),
  updateAlgorithm: (body) => request("/admin/algorithms/update", { method: "POST", body: body || {} }),
};
