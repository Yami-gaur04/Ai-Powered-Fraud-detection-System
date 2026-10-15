import { useSyncExternalStore } from "react";
import { connection } from "../api/client";

export const useApiStatus = () => useSyncExternalStore(connection.subscribe, connection.getSnapshot);
