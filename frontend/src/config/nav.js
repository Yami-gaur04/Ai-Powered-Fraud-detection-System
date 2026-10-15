import { LayoutDashboard, CreditCard, Bell, Settings, ShieldAlert, BarChart3, SlidersHorizontal, Cpu } from "lucide-react";

export const userNav = [
  { to: "/user", end: true, label: "Dashboard", icon: LayoutDashboard, key: "d" },
  { to: "/user/transactions", label: "Transactions", icon: CreditCard, key: "t" },
  { to: "/user/alerts", label: "Alerts", icon: Bell, key: "a" },
  { to: "/user/settings", label: "Settings", icon: Settings, key: "," },
];

export const adminNav = [
  { to: "/admin", end: true, label: "Dashboard", icon: LayoutDashboard, key: "d" },
  { to: "/admin/cases", label: "Fraud Cases", icon: ShieldAlert, key: "c" },
  { to: "/admin/reports", label: "Reports", icon: BarChart3, key: "r" },
  { to: "/admin/config", label: "Configuration", icon: SlidersHorizontal, key: "o" },
  { to: "/admin/algorithms", label: "Algorithms", icon: Cpu, key: "a" },
  { to: "/admin/settings", label: "Settings", icon: Settings, key: "," },
];
