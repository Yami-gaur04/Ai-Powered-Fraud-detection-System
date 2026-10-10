import { Fragment, useEffect, useRef, useState } from "react";
import { NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { ShieldCheck, ShieldAlert, Search, ChevronLeft, LogOut, Menu, Moon, Sun, User, Keyboard } from "lucide-react";
import { useAuth } from "../../context/AuthContext";
import { useTheme } from "../../context/ThemeContext";
import { SearchProvider, useSearch } from "../../context/SearchContext";
import { useApiStatus } from "../../hooks/useApiStatus";
import { adminNav, userNav } from "../../config/nav";
import { initials } from "../../utils/format";
import { Modal } from "../ui";
import NotificationBell from "./NotificationBell";

function Shell({ role }) {
  const { user, logout } = useAuth();
  const { theme, toggle } = useTheme();
  const nav = role === "ADMIN" ? adminNav : userNav;
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const { query, setQuery } = useSearch();
  const status = useApiStatus();
  const searchRef = useRef(null);
  const menuRef = useRef(null);
  const [collapsed, setCollapsed] = useState(() => localStorage.getItem("fd_collapsed") === "1");
  const [drawer, setDrawer] = useState(false);
  const [menu, setMenu] = useState(false);
  const [help, setHelp] = useState(false);
  const isMac = /Mac|iPhone|iPad/.test(navigator.platform);

  useEffect(() => { setDrawer(false); }, [pathname]);
  useEffect(() => { localStorage.setItem("fd_collapsed", collapsed ? "1" : "0"); }, [collapsed]);

  useEffect(() => {
    if (!menu) return;
    const h = (e) => { if (menuRef.current && !menuRef.current.contains(e.target)) setMenu(false); };
    document.addEventListener("mousedown", h);
    return () => document.removeEventListener("mousedown", h);
  }, [menu]);

  // keyboard shortcuts: Ctrl/Cmd+K, "?", and "G then <key>"
  useEffect(() => {
    let armed = false, timer;
    const onKey = (e) => {
      if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === "k") { e.preventDefault(); searchRef.current?.focus(); return; }
      const typing = /INPUT|TEXTAREA|SELECT/.test(e.target.tagName) || e.target.isContentEditable;
      if (typing) { if (e.key === "Escape") e.target.blur(); return; }
      if (e.metaKey || e.ctrlKey || e.altKey) return;
      if (e.key === "?") { setHelp(true); return; }
      const k = e.key.toLowerCase();
      if (armed) {
        armed = false; clearTimeout(timer);
        const item = nav.find((n) => n.key === k);
        if (item) { e.preventDefault(); navigate(item.to); }
        return;
      }
      if (k === "g") { armed = true; timer = setTimeout(() => { armed = false; }, 1200); }
    };
    window.addEventListener("keydown", onKey);
    return () => { window.removeEventListener("keydown", onKey); clearTimeout(timer); };
  }, [nav, navigate]);

  const ok = status === true;
  const statusText = status === null ? "Checking API…" : ok ? "Backend connected" : "API disconnected";
  const statusCls = status === null ? "wait" : ok ? "ok" : "";

  return (
    <>
      <header className="topbar">
        <button className="icon-btn menu-toggle" onClick={() => setDrawer((d) => !d)} aria-label="Menu"><Menu size={22} /></button>
        <div className="brand"><div className="logo"><ShieldCheck size={22} /></div><span>{role === "ADMIN" ? "FraudShield Admin" : "FraudShield AI"}</span></div>
        <label className="search">
          <Search size={19} />
          <input ref={searchRef} value={query} onChange={(e) => setQuery(e.target.value)} placeholder={role === "ADMIN" ? "Search cases, users, merchants…" : "Search transactions, alerts…"} />
          <kbd>{isMac ? "⌘ K" : "Ctrl K"}</kbd>
        </label>
        <div className="top-actions">
          <button className="icon-btn" onClick={toggle} aria-label="Toggle theme" title="Toggle theme">{theme === "dark" ? <Sun size={20} /> : <Moon size={20} />}</button>
          <button className="icon-btn" onClick={() => setHelp(true)} aria-label="Keyboard shortcuts" title="Keyboard shortcuts (?)"><Keyboard size={20} /></button>
          <NotificationBell />
          <div className="pop-wrap" ref={menuRef}>
            <button className="avatar-btn" onClick={() => setMenu((m) => !m)} aria-label="Account menu">{initials(user.name)}</button>
            {menu && (
              <div className="popover menu">
                <div className="who"><b>{user.name}</b><span>{user.email}</span><br /><span>{user.role === "ADMIN" ? "Administrator" : "User"}</span></div>
                <button onClick={() => { setMenu(false); navigate(`${role === "ADMIN" ? "/admin" : "/user"}/settings`); }}><User size={18} /> Account settings</button>
                <button onClick={() => logout()}><LogOut size={18} /> Log out</button>
              </div>
            )}
          </div>
        </div>
      </header>

      <div className={`layout ${collapsed ? "collapsed" : ""} ${drawer ? "open" : ""}`}>
        <aside className="sidebar">
          <div className="side-head"><span>MENU</span><button onClick={() => setCollapsed((c) => !c)} aria-label="Collapse sidebar"><ChevronLeft size={18} /></button></div>
          <nav>
            {nav.map((n) => (
              <NavLink key={n.to} to={n.to} end={n.end} className={({ isActive }) => "nav-item" + (isActive ? " active" : "")} title={n.label}>
                <n.icon size={22} /><span className="label">{n.label}</span>
                <span className="hint">G then {n.key === "," ? "," : n.key.toUpperCase()}</span>
              </NavLink>
            ))}
          </nav>
          <div className="side-foot">
            <div className="user-card">
              <div className={`u-avatar ${statusCls}`}><User size={20} /><span className="pulse" /></div>
              <div className="u-meta">
                <div className="u-name">{user.name.split(" ")[0]} {ok ? <ShieldCheck size={14} color="var(--green)" /> : <ShieldAlert size={14} color="var(--red)" />}</div>
                <div className={`u-status ${statusCls}`}>{statusText}</div>
              </div>
            </div>
          </div>
        </aside>
        <main className="content"><div className="page-fade" key={pathname}><Outlet /></div></main>
      </div>

      {help && (
        <Modal title="Keyboard shortcuts" onClose={() => setHelp(false)} width={440}>
          <div className="shortcut-list">
            <span>Focus search</span><kbd>{isMac ? "⌘ K" : "Ctrl K"}</kbd>
            {nav.map((n) => (<Fragment key={n.to}><span>Go to {n.label}</span><kbd>G then {n.key.toUpperCase()}</kbd></Fragment>))}
            <span>Show this help</span><kbd>?</kbd>
          </div>
        </Modal>
      )}
    </>
  );
}

export default function AppShell({ role }) {
  return <SearchProvider><Shell role={role} /></SearchProvider>;
}
