import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { Bell, ShieldAlert, Ban } from "lucide-react";
import { useAuth } from "../../context/AuthContext";
import { useFetch } from "../../hooks/useFetch";
import { txApi, adminApi } from "../../api/endpoints";
import { money, timeAgo } from "../../utils/format";
import { Badge } from "../ui";

/** Bell with live count. Users see their alerts, admins see open fraud cases. Polls every 30s. */
export default function NotificationBell() {
  const { user } = useAuth();
  const isAdmin = user.role === "ADMIN";
  const seenKey = `fd_seen_${user.email}`;
  const [open, setOpen] = useState(false);
  const [seen, setSeen] = useState(() => Number(localStorage.getItem(seenKey) || 0));
  const ref = useRef(null);

  const { data } = useFetch(
    () => (isAdmin ? adminApi.cases("OPEN") : txApi.alerts().then((a) => a.filter((x) => x.caseStatus !== "FALSE_POSITIVE"))),
    [isAdmin], { interval: 30000 }
  );
  const items = data || [];
  const unread = isAdmin ? items.length : items.filter((a) => new Date(a.createdAt).getTime() > seen).length;
  const link = isAdmin ? "/admin/cases?status=OPEN" : "/user/alerts";

  useEffect(() => {
    if (!open) return;
    const h = (e) => { if (ref.current && !ref.current.contains(e.target)) setOpen(false); };
    document.addEventListener("mousedown", h);
    return () => document.removeEventListener("mousedown", h);
  }, [open]);

  const toggle = () => {
    if (!open && !isAdmin) { const now = Date.now(); localStorage.setItem(seenKey, String(now)); setSeen(now); }
    setOpen((o) => !o);
  };

  return (
    <div className="pop-wrap" ref={ref}>
      <button className="icon-btn" onClick={toggle} aria-label="Notifications"><Bell size={21} />{unread > 0 && <span className="dot">{unread > 99 ? "99+" : unread}</span>}</button>
      {open && (
        <div className="popover notif">
          <div className="notif-head"><span>{isAdmin ? "Open fraud cases" : "Recent alerts"}</span><Link to={link} className="link" onClick={() => setOpen(false)}>View all</Link></div>
          {items.length === 0 && <div className="empty" style={{ padding: 32 }}><b>You're all caught up</b></div>}
          {items.slice(0, 5).map((c) => (
            <Link key={c.caseId} to={link} className={`notif-item ${!isAdmin && new Date(c.createdAt).getTime() > seen ? "unread" : ""}`} onClick={() => setOpen(false)}>
              <div className={`chip ${c.transactionStatus === "BLOCKED" ? "red" : "amber"}`} style={{ width: 34, height: 34 }}>{c.transactionStatus === "BLOCKED" ? <Ban size={17} /> : <ShieldAlert size={17} />}</div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontWeight: 600 }}>{money(c.amount)} at {c.merchant}</div>
                <div style={{ color: "var(--muted)", fontSize: 12.5 }}>{isAdmin ? c.userEmail + " · " : ""}{timeAgo(c.createdAt)}</div>
              </div>
              <Badge status={c.transactionStatus} />
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
