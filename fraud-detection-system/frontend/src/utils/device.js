// A per-browser "device id" sent with every transaction (the backend flags unseen devices).
const KEY = "fd_device", LOC = "fd_loc";
const rand = () => "web-" + Math.random().toString(36).slice(2, 8);

export function getDeviceId() {
  let d = localStorage.getItem(KEY);
  if (!d) { d = rand(); localStorage.setItem(KEY, d); }
  return d;
}
export function resetDeviceId() { const d = rand(); localStorage.setItem(KEY, d); return d; }
export const randomDeviceId = rand;
export const getSavedLocation = () => localStorage.getItem(LOC) || "";
export const saveLocation = (l) => localStorage.setItem(LOC, l);
export const clearSavedLocation = () => localStorage.removeItem(LOC);
