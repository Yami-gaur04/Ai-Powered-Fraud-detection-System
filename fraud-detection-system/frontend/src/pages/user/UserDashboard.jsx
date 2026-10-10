import { useState } from "react";
import { Link } from "react-router-dom";
import { CreditCard, CheckCircle2, ShieldAlert, Bell, ShieldCheck, Ban, Activity, Plus } from "lucide-react";
import { AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from "recharts";
import { useAuth } from "../../context/AuthContext";
import { useUserData } from "../../hooks/useUserData";
import { Card, PageHeader, StatCard, StatSkeletons, BlockSkeleton, ErrorState, Badge, ScoreBar, Empty } from "../../components/ui";
import { tooltipStyle } from "../../components/charts";
import QuickBar from "../../components/QuickBar";
import TransactionModal from "../../components/modals/TransactionModal";
import TransactionDetail from "../../components/modals/TransactionDetail";
import { money, dateTime, timeAgo, firstName, greeting, parseQuick, scorePct } from "../../utils/format";
import { getDeviceId } from "../../utils/device";

const tone = (s) => (s === "APPROVED" ? "green" : s === "FLAGGED" ? "amber" : "red");
const StatusIcon = ({ s }) => (s === "APPROVED" ? <CheckCircle2 size={19} /> : s === "FLAGGED" ? <ShieldAlert size={19} /> : <Ban size={19} />);

export default function UserDashboard() {
  const { user } = useAuth();
  const { data, loading, error, reload, refresh } = useUserData();
  const [modal, setModal] = useState(null);
  const [detail, setDetail] = useState(null);

  const quick = (text, clear) => { setModal(parseQuick(text)); clear(); };
  const header = <PageHeader title="Dashboard" subtitle={`${greeting()}, ${firstName(user.name)}. Here's your account security overview.`}
    actions={<button className="btn" onClick={() => setModal({})}><Plus size={17} /> New transaction</button>} />;

  if (error) return <>{header}<ErrorState error={error} onRetry={reload} /></>;

  const txs = data?.txs || [], alerts = data?.alerts || [];
  const count = (s) => txs.filter((t) => t.status === s).length;
  const active = alerts.filter((a) => a.caseStatus !== "FALSE_POSITIVE");
  const avg = txs.length ? txs.reduce((a, t) => a + (t.fraudScore || 0), 0) / txs.length : 0;
  const trend = [...txs].slice(0, 12).reverse().map((t) => ({ name: dateTime(t.transactionTime), risk: scorePct(t.fraudScore), amount: t.amount }));

  return (
    <>
      {header}
      <QuickBar placeholder="E.g., '2500 at Amazon' to start a new transaction" button="Start Transaction" onSubmit={quick} />

      {loading && !data ? <><StatSkeletons /><BlockSkeleton h={220} /></> : (
        <>
          <div className="grid-4">
            <StatCard label="Total transactions" value={txs.length} sub="All time" icon={CreditCard} />
            <StatCard label="Approved" value={count("APPROVED")} sub="Passed fraud checks" icon={CheckCircle2} tone="green" />
            <StatCard label="Flagged / Blocked" value={count("FLAGGED") + count("BLOCKED")} sub={`${count("FLAGGED")} flagged · ${count("BLOCKED")} blocked`} icon={ShieldAlert} tone="amber" />
            <StatCard label="Active alerts" value={active.length} sub={active.length ? "Needs attention" : "All clear"} icon={Bell} tone={active.length ? "red" : "green"} />
          </div>

          <div className="grid-2">
            <Card title="Risk score trend" icon={Activity}>
              {trend.length < 2 ? <Empty icon={Activity} title="Not enough data yet" text="Make a couple of transactions to see how your risk score moves." /> : (
                <div className="chart-box">
                  <ResponsiveContainer width="100%" height="100%">
                    <AreaChart data={trend} margin={{ left: 0, right: 16, top: 10 }}>
                      <defs><linearGradient id="rg" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor="#6366f1" stopOpacity={0.35} /><stop offset="100%" stopColor="#6366f1" stopOpacity={0} /></linearGradient></defs>
                      <CartesianGrid vertical={false} />
                      <XAxis dataKey="name" tickLine={false} axisLine={false} interval="preserveStartEnd" minTickGap={30} />
                      <YAxis tickLine={false} axisLine={false} domain={[0, 100]} unit="%" width={44} />
                      <Tooltip {...tooltipStyle} formatter={(v) => [`${v}%`, "Risk"]} />
                      <Area type="monotone" dataKey="risk" stroke="#6366f1" strokeWidth={2.5} fill="url(#rg)" />
                    </AreaChart>
                  </ResponsiveContainer>
                </div>
              )}
            </Card>
            <Card title="Account security" icon={ShieldCheck} pad>
              <div className="kv">
                <span>Status</span><b>{active.length ? `${active.length} need attention` : "No suspicious activity"}</b>
                <span>Average risk</span><div style={{ justifySelf: "end", minWidth: 170 }}><ScoreBar score={avg} /></div>
                <span>Last transaction</span><b>{txs[0] ? `${money(txs[0].amount)} · ${timeAgo(txs[0].transactionTime)}` : "None yet"}</b>
                <span>This device</span><b>{getDeviceId()}</b>
              </div>
            </Card>
          </div>

          <div className="grid-2 even">
            <Card title="Recent transactions" action={<Link className="link" to="/user/transactions">View all</Link>}>
              {txs.length === 0 ? <Empty icon={CreditCard} title="No transactions yet" text="Use the bar above to start your first one." /> :
                txs.slice(0, 5).map((t) => (
                  <div className="list-item click" key={t.id} onClick={() => setDetail(t)}>
                    <div className={`chip ${tone(t.status)}`}><StatusIcon s={t.status} /></div>
                    <div className="li-body"><div className="li-title">{t.merchant}</div><div className="li-text">{t.location} · {dateTime(t.transactionTime)}</div></div>
                    <div className="li-right"><b>{money(t.amount)}</b><Badge status={t.status} /></div>
                  </div>
                ))}
            </Card>
            <Card title="Latest alerts" action={<Link className="link" to="/user/alerts">View all</Link>}>
              {alerts.length === 0 ? <Empty icon={ShieldCheck} title="No alerts" text="Suspicious transactions will show up here." /> :
                alerts.slice(0, 4).map((a) => (
                  <div className="list-item" key={a.caseId}>
                    <div className={`chip ${tone(a.transactionStatus)}`}><StatusIcon s={a.transactionStatus} /></div>
                    <div className="li-body"><div className="li-title">{a.merchant} · {money(a.amount)}</div><div className="li-text">{a.reason}</div></div>
                    <div className="li-right"><Badge status={a.caseStatus === "OPEN" ? a.transactionStatus : a.caseStatus} /><span>{timeAgo(a.createdAt)}</span></div>
                  </div>
                ))}
            </Card>
          </div>
        </>
      )}

      {modal && <TransactionModal prefill={modal} onClose={() => setModal(null)} onDone={refresh} />}
      {detail && <TransactionDetail tx={detail} onClose={() => setDetail(null)} />}
    </>
  );
}
