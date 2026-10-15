export const STATUS_META = {
  APPROVED: { label: "Approved", tone: "green" },
  FLAGGED: { label: "Flagged", tone: "amber" },
  BLOCKED: { label: "Blocked", tone: "red" },
  OPEN: { label: "Open", tone: "amber" },
  CONFIRMED_FRAUD: { label: "Confirmed fraud", tone: "red" },
  FALSE_POSITIVE: { label: "False positive", tone: "blue" },
};
export const COLORS = { green: "#22c55e", amber: "#f59e0b", red: "#dc2626", blue: "#3b82f6", ink: "#6366f1" };
export const homeFor = (role) => (role === "ADMIN" ? "/admin" : "/user");
export const PAYMENT_METHODS = ["UPI", "CARD", "NET_BANKING", "WALLET"];
