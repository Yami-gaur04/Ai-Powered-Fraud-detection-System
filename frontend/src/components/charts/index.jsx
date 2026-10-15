import { PieChart, Pie, Cell, ResponsiveContainer } from "recharts";

/** Donut with the total in the middle and a legend beside it. parts: [{label,value,color}] */
export function DonutWithLegend({ parts, centerLabel = "total" }) {
  const total = parts.reduce((a, p) => a + p.value, 0);
  const data = total === 0 ? [{ label: "none", value: 1, color: "var(--track)" }] : parts.filter((p) => p.value > 0);
  return (
    <div className="donut-wrap">
      <div className="donut">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie data={data} dataKey="value" innerRadius={62} outerRadius={88} paddingAngle={total ? 3 : 0} stroke="none" cornerRadius={4}>
              {data.map((d, i) => <Cell key={i} fill={d.color} />)}
            </Pie>
          </PieChart>
        </ResponsiveContainer>
        <div className="mid"><div><b>{total}</b><span>{centerLabel}</span></div></div>
      </div>
      <div className="legend">
        {parts.map((p) => (<div key={p.label}><i style={{ background: p.color }} />{p.label}<b>{p.value}</b></div>))}
      </div>
    </div>
  );
}

/** Half-circle gauge. value/max -> filled arc. */
export function Gauge({ value, max = 100, label, color = "#6366f1", caption }) {
  const len = Math.PI * 70;
  const p = Math.min(1, Math.max(0, (value ?? 0) / max));
  return (
    <div style={{ textAlign: "center" }}>
      <svg viewBox="0 0 180 110" width="100%" style={{ maxWidth: 240 }}>
        <path d="M20 90 A70 70 0 0 1 160 90" fill="none" stroke="var(--track)" strokeWidth="16" strokeLinecap="round" />
        <path d="M20 90 A70 70 0 0 1 160 90" fill="none" stroke={color} strokeWidth="16" strokeLinecap="round" strokeDasharray={`${p * len} ${len}`} style={{ transition: "stroke-dasharray .6s" }} />
        <text x="90" y="84" textAnchor="middle" fontSize="26" fontWeight="700" fill="var(--text)">{label}</text>
      </svg>
      {caption && <div style={{ fontSize: 13, color: "var(--muted)", marginTop: -6 }}>{caption}</div>}
    </div>
  );
}

export const tooltipStyle = {
  contentStyle: { background: "var(--card)", border: "1px solid var(--border)", borderRadius: 12, fontSize: 13, boxShadow: "var(--pop)" },
  labelStyle: { color: "var(--text)", fontWeight: 600 },
  itemStyle: { color: "var(--text)" },
  cursor: { fill: "rgba(128,128,140,.1)" },
};
