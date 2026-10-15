import { useFetch } from "./useFetch";
import { txApi } from "../api/endpoints";

/** The signed-in user's transactions + alerts, refreshed every 30s. */
export function useUserData() {
  return useFetch(async () => {
    const [txs, alerts] = await Promise.all([txApi.mine(), txApi.alerts()]);
    return { txs, alerts };
  }, [], { interval: 30000 });
}
