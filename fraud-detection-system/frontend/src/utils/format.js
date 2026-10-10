export const money = (n) =>
  new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 2 }).format(Number(n) || 0);

export const moneyShort = (n) => {
  n = Number(n) || 0;
  if (n >= 1e7) return `₹${(n / 1e7).toFixed(1)}Cr`;
  if (n >= 1e5) return `₹${(n / 1e5).toFixed(1)}L`;
  if (n >= 1e3) return `₹${(n / 1e3).toFixed(1)}K`;
  return `₹${n}`;
};

export const dateTime = (s) =>
  s ? new Date(s).toLocaleString("en-IN", { day: "2-digit", month: "short", hour: "2-digit", minute: "2-digit" }) : "–";

export const fullDateTime = (s) =>
  s ? new Date(s).toLocaleString("en-IN", { dateStyle: "medium", timeStyle: "medium" }) : "–";

export function timeAgo(s) {
  if (!s) return "";
  const sec = Math.max(1, Math.floor((Date.now() - new Date(s).getTime()) / 1000));
  if (sec < 60) return "just now";
  const min = Math.floor(sec / 60);
  if (min < 60) return `${min}m ago`;
  const h = Math.floor(min / 60);
  if (h < 24) return `${h}h ago`;
  const d = Math.floor(h / 24);
  return d < 7 ? `${d}d ago` : dateTime(s);
}

export const pct = (n, d = 1) => (n == null ? "–" : `${Math.round(n * 10 ** d) / 10 ** d}%`);
export const scorePct = (s) => Math.round((s || 0) * 100);
export const initials = (name) => (name || "?").split(/\s+/).map((w) => w[0]).slice(0, 2).join("").toUpperCase();
export const firstName = (name) => (name || "").split(/\s+/)[0];

export const greeting = () => {
  const h = new Date().getHours();
  return h < 12 ? "Good morning" : h < 18 ? "Good afternoon" : "Good evening";
};

/** "2500 at Amazon" -> { amount: "2500", merchant: "Amazon" } */
export function parseQuick(text) {
  const m = text.match(/^\s*[₹$]?\s*([\d,]+(?:\.\d+)?)\s*(?:rs\.?|inr|rupees)?\s*(?:at|to|@|for)?\s*(.*)$/i);
  return m ? { amount: m[1].replace(/,/g, ""), merchant: m[2].trim() } : {};
}

export const splitReasons = (reason) => (reason ? reason.split(/;\s*/).filter(Boolean) : []);

/** cases grouped per day for the last N days */
export function casesPerDay(cases, days = 7) {
  const out = [];
  for (let i = days - 1; i >= 0; i--) {
    const d = new Date(); d.setHours(0, 0, 0, 0); d.setDate(d.getDate() - i);
    out.push({ date: d, label: d.toLocaleDateString("en-IN", { day: "2-digit", month: "short" }), count: 0 });
  }
  cases.forEach((c) => {
    const t = new Date(c.createdAt); t.setHours(0, 0, 0, 0);
    const slot = out.find((o) => o.date.getTime() === t.getTime());
    if (slot) slot.count++;
  });
  return out;
}
