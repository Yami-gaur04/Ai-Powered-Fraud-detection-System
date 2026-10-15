import { useMemo, useState } from "react";
import { CreditCard, ShieldAlert, Percent, Target, Download } from "lucide-react";
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, Cell } from "recharts";
import { useAdminData, statusParts } from "../../hooks/useAdminData";
import { Card, PageHeader, StatCard, StatSkeletons, BlockSkeleton, ErrorState, Chips } from "../../components/ui";
import { DonutWithLegend, Gauge, tooltipStyle } from "../../components/charts";
import { pct, casesPerDay, scorePct } from "../../utils/format";
import { downloadCSV } from "../../utils/csv";

export default function AdminReports() {
  const { data, loading, error, reload } = useAdminData();
  const [range, setRange] = useState(7);
  const r = data?.report, cases = data?.cases || [];

  const perDay = useMemo(() => casesPerDay(cases, range).map((d) => ({ name: d.label, cases: d.count })), [cases, range]);
  const buckets = useMemo(() => {
    const b = Array.from({ length: 10 }, (_, i) => ({ name: `${i * 10}–${i * 10 + 10}%`, cases: 0, from: i * 10 }));
    cases.forEach((c) => { b[Math.min(9, Math.floor(scorePct(c.fraudScore) / 10))].cases++; });
    return b;
  }, [cases]);

  const exportCsv = () => downloadCSV("detection-report.csv", [{ header: "Metric", value: (x) => x[0] }, { header: "Value", value: (x) => x[1] }], [
    ["Total transactions", r.totalTransactions], ["Approved", r.approved], ["Flagged", r.flagged], ["Blocked", r.blocked],
    ["Total cases", r.totalCases], ["Open cases", r.openCases], ["Confirmed fraud", r.confirmedFraud], ["False positives", r.falsePositives],
    ["Fraud rate %", r.fraudRatePercent], ["Precision %", r.detectionPrecisionPercent ?? ""], ["Active algorithm", r.activeAlgorithm],
  ]);

  return (
    <>
      <PageHeader title="Detection reports" subtitle="Graphical view of transactions and detected fraud cases"
        actions={<button className="btn ghost" onClick={exportCsv} disabled={!r}><Download size={16} /> Export summary</button>} />
      {error ? <ErrorState error={error} onRetry={reload} /> : loading && !data ? <><StatSkeletons /><BlockSkeleton /></> : (
        <>
          <div className="grid-4">
            <StatCard label="Total transactions" value={r.totalTransactions} icon={CreditCard} />
            <StatCard label="Total cases" value={r.totalCases} sub={`${r.openCases} still open`} icon={ShieldAlert} tone="amber" />
            <StatCard label="Fraud rate" value={pct(r.fraudRatePercent)} icon={Percent} tone="red" />
            <StatCard label="Precision" value={pct(r.detectionPrecisionPercent)} sub={`Algorithm ${r.activeAlgorithm}`} icon={Target} tone="blue" />
          </div>

          <div className="grid-3">
            <Card title="Transactions by outcome"><DonutWithLegend parts={statusParts(r)} centerLabel="transactions" /></Card>
            <Card title="Case review status">
              <div className="chart-box sm">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={[{ n: "Open", v: r.openCases, c: "#f59e0b" }, { n: "Confirmed", v: r.confirmedFraud, c: "#dc2626" }, { n: "False +", v: r.falsePositives, c: "#3b82f6" }]} margin={{ left: 0, right: 16, top: 10 }}>
                    <CartesianGrid vertical={false} /><XAxis dataKey="n" tickLine={false} axisLine={false} /><YAxis allowDecimals={false} tickLine={false} axisLine={false} width={30} />
                    <Tooltip {...tooltipStyle} formatter={(v) => [v, "Cases"]} />
                    <Bar dataKey="v" radius={[8, 8, 0, 0]} maxBarSize={56}>{["#f59e0b", "#dc2626", "#3b82f6"].map((c) => <Cell key={c} fill={c} />)}</Bar>
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </Card>
            <Card title="Detection precision" pad>
              <Gauge value={r.detectionPrecisionPercent ?? 0} label={pct(r.detectionPrecisionPercent)} color="#6366f1"
                caption={r.detectionPrecisionPercent == null ? "Review cases to measure precision" : "Confirmed fraud ÷ reviewed cases"} />
            </Card>
          </div>

          <div className="grid-2 even">
            <Card title="Detected cases over time" action={<Chips value={range} onChange={setRange} options={[{ value: 7, label: "7 days" }, { value: 14, label: "14 days" }, { value: 30, label: "30 days" }]} />}>
              <div className="chart-box">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={perDay} margin={{ left: 0, right: 16, top: 10 }}>
                    <CartesianGrid vertical={false} /><XAxis dataKey="name" tickLine={false} axisLine={false} interval="preserveStartEnd" minTickGap={14} /><YAxis allowDecimals={false} tickLine={false} axisLine={false} width={30} />
                    <Tooltip {...tooltipStyle} formatter={(v) => [v, "Cases"]} />
                    <Bar dataKey="cases" fill="#6366f1" radius={[7, 7, 0, 0]} maxBarSize={36} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </Card>
            <Card title="Risk score distribution of cases">
              <div className="chart-box">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={buckets} margin={{ left: 0, right: 16, top: 10 }}>
                    <CartesianGrid vertical={false} /><XAxis dataKey="name" tickLine={false} axisLine={false} interval={0} angle={-30} textAnchor="end" height={54} /><YAxis allowDecimals={false} tickLine={false} axisLine={false} width={30} />
                    <Tooltip {...tooltipStyle} formatter={(v) => [v, "Cases"]} />
                    <Bar dataKey="cases" radius={[7, 7, 0, 0]} maxBarSize={36}>{buckets.map((b) => <Cell key={b.name} fill={b.from >= 70 ? "#dc2626" : b.from >= 40 ? "#f59e0b" : "#22c55e"} />)}</Bar>
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </Card>
          </div>
        </>
      )}
    </>
  );
}
