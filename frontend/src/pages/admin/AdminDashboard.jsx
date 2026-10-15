import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { CreditCard, ShieldAlert, Percent, Target, Activity, Cpu, Ban, CheckCircle2 } from "lucide-react";
import { AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from "recharts";
import { useAuth } from "../../context/AuthContext";
import { useToast } from "../../context/ToastContext";
import { useAdminData, statusParts } from "../../hooks/useAdminData";
import { Card, PageHeader, StatCard, StatSkeletons, BlockSkeleton, ErrorState, Badge, Empty } from "../../components/ui";
import { DonutWithLegend, tooltipStyle } from "../../components/charts";
import QuickBar from "../../components/QuickBar";
import ReviewModal from "../../components/modals/ReviewModal";
import { money, pct, timeAgo, casesPerDay, firstName, greeting } from "../../utils/format";

const tone = (s) => (s === "OPEN" ? "amber" : s === "CONFIRMED_FRAUD" ? "red" : "blue");

export default function AdminDashboard() {
  const { user } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const { data, loading, error, reload, refresh } = useAdminData({ withConfig: true });
  const [review, setReview] = useState(null);

  const go = (text, clear) => {
    const q = text.toLowerCase();
    let to = null;
    if (/config|threshold|setting|weight/.test(q)) to = "/admin/config";
    else if (/algorithm|model|retrain|refine/.test(q)) to = "/admin/algorithms";
    else if (/report|chart|graph/.test(q)) to = "/admin/reports";
    else if (/false/.test(q)) to = "/admin/cases?status=FALSE_POSITIVE";
    else if (/confirm/.test(q)) to = "/admin/cases?status=CONFIRMED_FRAUD";
    else if (/open|pending/.test(q)) to = "/admin/cases?status=OPEN";
    else if (/case|fraud/.test(q)) to = "/admin/cases";
    if (to) { navigate(to); clear(); } else toast.info("Try: 'show open fraud cases', 'view reports', 'update thresholds', 'update algorithm'");
  };

  const header = <PageHeader title="Dashboard" subtitle={`${greeting()}, ${firstName(user.name)}. Overview of fraud detection and algorithm performance.`} />;
  if (error) return <>{header}<ErrorState error={error} onRetry={reload} /></>;

  const r = data?.report, cases = data?.cases || [], cfg = data?.config;
  const chart = casesPerDay(cases, 7).map((d) => ({ name: d.label, cases: d.count }));

  return (
    <>
      {header}
      <QuickBar placeholder="E.g., 'Show open fraud cases' or 'Update thresholds'" button="Go" onSubmit={go} />
      {loading && !data ? <><StatSkeletons /><div className="grid-2 even"><BlockSkeleton /><BlockSkeleton /></div></> : (
        <>
          <div className="grid-4">
            <StatCard label="Total transactions" value={r.totalTransactions} sub={`${r.approved} approved`} icon={CreditCard} />
            <StatCard label="Open fraud cases" value={r.openCases} sub={`${r.totalCases} cases in total`} icon={ShieldAlert} tone={r.openCases ? "amber" : "green"} />
            <StatCard label="Fraud rate" value={pct(r.fraudRatePercent)} sub="Flagged or blocked" icon={Percent} tone="red" />
            <StatCard label="Detection precision" value={pct(r.detectionPrecisionPercent)} sub={r.detectionPrecisionPercent == null ? "Review cases to measure" : `${r.confirmedFraud} confirmed · ${r.falsePositives} false`} icon={Target} tone="blue" />
          </div>

          <div className="grid-2 even">
            <Card title="Detection overview" icon={Activity} action={<Link className="link" to="/admin/reports">Full reports</Link>}>
              <DonutWithLegend parts={statusParts(r)} centerLabel="transactions" />
            </Card>
            <Card title="Active configuration" icon={Cpu} action={<Link className="link" to="/admin/config">Edit</Link>} pad>
              <div className="kv">
                <span>Algorithm</span><b>{r.activeAlgorithm}</b>
                <span>Flag threshold</span><b>{cfg.flagThreshold}</b>
                <span>Block threshold</span><b>{cfg.blockThreshold}</b>
                <span>High amount limit</span><b>{money(cfg.highAmountLimit)}</b>
                <span>Max transactions / hour</span><b>{cfg.maxTxPerHour}</b>
                <span>Weights (amt / vel / loc / dev)</span><b>{cfg.amountWeight} / {cfg.velocityWeight} / {cfg.locationWeight} / {cfg.deviceWeight}</b>
              </div>
            </Card>
          </div>

          <div className="grid-2">
            <Card title="Detected cases, last 7 days" icon={Activity}>
              <div className="chart-box sm">
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={chart} margin={{ left: 0, right: 16, top: 10 }}>
                    <defs><linearGradient id="cg" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor="#dc2626" stopOpacity={0.3} /><stop offset="100%" stopColor="#dc2626" stopOpacity={0} /></linearGradient></defs>
                    <CartesianGrid vertical={false} />
                    <XAxis dataKey="name" tickLine={false} axisLine={false} />
                    <YAxis tickLine={false} axisLine={false} allowDecimals={false} width={30} />
                    <Tooltip {...tooltipStyle} />
                    <Area type="monotone" dataKey="cases" stroke="#dc2626" strokeWidth={2.5} fill="url(#cg)" />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            </Card>
            <Card title="Recent fraud cases" action={<Link className="link" to="/admin/cases">View all</Link>}>
              {cases.length === 0 ? <Empty icon={CheckCircle2} title="No fraud cases yet" text="Cases appear when a transaction is flagged or blocked." /> :
                cases.slice(0, 5).map((c) => (
                  <div className="list-item click" key={c.caseId} onClick={() => setReview(c)}>
                    <div className={`chip ${tone(c.caseStatus)}`}>{c.transactionStatus === "BLOCKED" ? <Ban size={18} /> : <ShieldAlert size={18} />}</div>
                    <div className="li-body"><div className="li-title">{c.merchant} · {money(c.amount)}</div><div className="li-text">{c.userEmail}</div></div>
                    <div className="li-right"><Badge status={c.caseStatus} /><span>{timeAgo(c.createdAt)}</span></div>
                  </div>
                ))}
            </Card>
          </div>
        </>
      )}
      {review && <ReviewModal item={review} onClose={() => setReview(null)} onDone={refresh} />}
    </>
  );
}
