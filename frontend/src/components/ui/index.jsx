import { createPortal } from "react-dom";
import { useEffect } from "react";
import { X, Inbox, AlertTriangle, ArrowUpDown, ChevronUp, ChevronDown, ChevronLeft, ChevronRight, Loader2 } from "lucide-react";
import { STATUS_META } from "../../config/constants";
import { scorePct } from "../../utils/format";

export function Badge({ status }) {
  const m = STATUS_META[status] || { label: status || "–", tone: "gray" };
  return <span className={`badge b-${m.tone}`}>{m.label}</span>;
}

export function ScoreBar({ score }) {
  const p = scorePct(score);
  const color = p >= 70 ? "#dc2626" : p >= 40 ? "#f59e0b" : "#22c55e";
  return (
    <div className="score">
      <div className="bar"><i style={{ width: `${p}%`, background: color }} /></div>
      <span>{p}%</span>
    </div>
  );
}

export function Card({ title, icon: Icon, action, children, pad, style, className = "" }) {
  return (
    <div className={`card ${pad ? "pad" : ""} ${className}`} style={style}>
      {title && (
        <div className="card-head">
          <h3>{Icon && <Icon size={20} />}{title}</h3>
          {action}
        </div>
      )}
      {children}
    </div>
  );
}

export function PageHeader({ title, subtitle, actions }) {
  return (
    <div className="page-head">
      <div><h1 className="page-title">{title}</h1>{subtitle && <p className="page-sub">{subtitle}</p>}</div>
      {actions && <div className="page-actions">{actions}</div>}
    </div>
  );
}

export function StatCard({ label, value, sub, icon: Icon, tone = "" }) {
  return (
    <div className="card stat">
      <div className={`chip ${tone}`}><Icon size={19} /></div>
      <div className="label">{label}</div>
      <div className="value">{value}</div>
      <div className="sub">{sub}&nbsp;</div>
    </div>
  );
}

export const Skel = ({ h = 16, w = "100%" }) => <span className="skel" style={{ height: h, width: w }} />;

export function StatSkeletons({ n = 4 }) {
  return (
    <div className="grid-4">
      {Array.from({ length: n }, (_, i) => (
        <div className="skel-card" key={i}><Skel h={18} w="55%" /><Skel h={34} w="38%" /><Skel h={12} w="70%" /></div>
      ))}
    </div>
  );
}
export const BlockSkeleton = ({ h = 220 }) => <div className="card pad"><Skel h={h} /></div>;

export function Empty({ icon: Icon = Inbox, title, text }) {
  return (
    <div className="empty">
      <div className="big"><Icon size={26} /></div>
      <b>{title}</b>
      {text && <div style={{ marginTop: 4, fontSize: 13.5 }}>{text}</div>}
    </div>
  );
}

export function ErrorState({ error, onRetry }) {
  return (
    <div className="error-card">
      <div style={{ display: "flex", gap: 12, alignItems: "center" }}>
        <AlertTriangle size={22} />
        <div><b>Something went wrong</b><div style={{ fontSize: 14 }}>{error?.message || "Unexpected error"}</div></div>
      </div>
      {onRetry && <button className="btn ghost sm" onClick={onRetry}>Try again</button>}
    </div>
  );
}

export function Modal({ title, onClose, children, width = 560 }) {
  useEffect(() => {
    const onKey = (e) => { if (e.key === "Escape") onClose(); };
    document.addEventListener("keydown", onKey);
    document.body.style.overflow = "hidden";
    return () => { document.removeEventListener("keydown", onKey); document.body.style.overflow = ""; };
  }, [onClose]);
  return createPortal(
    <div className="overlay" onMouseDown={(e) => { if (e.target === e.currentTarget) onClose(); }}>
      <div className="modal" style={{ width: `min(${width}px,100%)` }} role="dialog" aria-modal="true" aria-label={title}>
        <div className="modal-head"><h3>{title}</h3><button className="x-btn" onClick={onClose} aria-label="Close"><X size={18} /></button></div>
        <div className="modal-body">{children}</div>
      </div>
    </div>,
    document.body
  );
}

export function Chips({ options, value, onChange }) {
  return (
    <div className="chips">
      {options.map((o) => (
        <button key={o.value} className={`chip-btn ${o.value === value ? "on" : ""}`} onClick={() => onChange(o.value)}>
          {o.label}{o.count != null && <em>{o.count}</em>}
        </button>
      ))}
    </div>
  );
}

export function SortTh({ label, k, table, className = "" }) {
  const on = table.sort.key === k;
  const Arrow = !on ? ArrowUpDown : table.sort.dir === "asc" ? ChevronUp : ChevronDown;
  return (
    <th className={`sortable ${className}`} onClick={() => table.toggle(k)}>
      {label}<Arrow size={13} className={`arr ${on ? "on" : ""}`} />
    </th>
  );
}

export function Pagination({ table }) {
  const { page, pages, total, pageSize, setPage } = table;
  if (total <= pageSize) return null;
  const from = (page - 1) * pageSize + 1, to = Math.min(total, page * pageSize);
  return (
    <div className="pager">
      <span>Showing {from}–{to} of {total}</span>
      <div className="pg">
        <button disabled={page === 1} onClick={() => setPage(page - 1)} aria-label="Previous"><ChevronLeft size={16} /></button>
        <span style={{ padding: "0 6px" }}>Page {page} of {pages}</span>
        <button disabled={page === pages} onClick={() => setPage(page + 1)} aria-label="Next"><ChevronRight size={16} /></button>
      </div>
    </div>
  );
}

export function Button({ loading, children, ...props }) {
  return (
    <button {...props} disabled={props.disabled || loading}>
      {loading && <Loader2 size={16} className="spin" />}{children}
    </button>
  );
}

export function Switch({ on, onChange, label }) {
  return <button type="button" role="switch" aria-checked={on} aria-label={label} className={`switch ${on ? "on" : ""}`} onClick={() => onChange(!on)} />;
}
