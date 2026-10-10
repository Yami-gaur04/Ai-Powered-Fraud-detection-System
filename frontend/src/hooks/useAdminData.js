import { useFetch } from "./useFetch";
import { adminApi } from "../api/endpoints";

/** Report + all cases (+ config on demand), refreshed every 30s. */
export function useAdminData({ withConfig = false } = {}) {
  return useFetch(async () => {
    const [report, cases, config] = await Promise.all([
      adminApi.reports(), adminApi.cases(), withConfig ? adminApi.config() : Promise.resolve(null),
    ]);
    return { report, cases, config };
  }, [withConfig], { interval: 30000 });
}

export const statusParts = (r) => [
  { label: "Approved", value: r.approved, color: "#22c55e" },
  { label: "Flagged", value: r.flagged, color: "#f59e0b" },
  { label: "Blocked", value: r.blocked, color: "#dc2626" },
];
