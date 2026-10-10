import { useMemo, useState } from "react";
import { Ban, ShieldAlert, ShieldCheck, CheckCircle2 } from "lucide-react";
import { useUserData } from "../../hooks/useUserData";
import { useSearch, matches } from "../../context/SearchContext";
import { Card, PageHeader, Chips, Badge, Empty, ErrorState, BlockSkeleton } from "../../components/ui";
import { money, timeAgo, splitReasons, scorePct } from "../../utils/format";

const EXPLAIN = {
  OPEN: "Under review by our fraud team.",
  CONFIRMED_FRAUD: "Our team confirmed this was fraud. The transaction stays blocked.",
  FALSE_POSITIVE: "Reviewed and cleared. This was a legitimate transaction and has been approved.",
};

export default function UserAlerts() {
  const { data, loading, error, reload } = useUserData();
  const { query } = useSearch();
  const [filter, setFilter] = useState("ALL");
  const all = data?.alerts || [];
  const rows = useMemo(() => all.filter((a) =>
    (filter === "ALL" || (filter === "OPEN" ? a.caseStatus === "OPEN" : a.caseStatus !== "OPEN")) &&
    matches(a, query, (a) => [a.merchant, a.location, a.reason, a.caseStatus, a.amount])), [all, filter, query]);

  return (
    <>
      <PageHeader title="Alerts" subtitle="Suspicious activity detected on your transactions" />
      {error ? <ErrorState error={error} onRetry={reload} /> : loading && !data ? <BlockSkeleton h={260} /> : (
        <>
          <Chips value={filter} onChange={setFilter} options={[
            { value: "ALL", label: "All", count: all.length },
            { value: "OPEN", label: "Under review", count: all.filter((a) => a.caseStatus === "OPEN").length },
            { value: "DONE", label: "Resolved", count: all.filter((a) => a.caseStatus !== "OPEN").length },
          ]} />
          {rows.length === 0 ? <Card><Empty icon={ShieldCheck} title="No alerts" text="Everything looks good. We'll let you know here if something looks suspicious." /></Card> : (
            <div style={{ display: "grid", gap: 16 }}>
              {rows.map((a) => {
                const blocked = a.transactionStatus === "BLOCKED", cleared = a.caseStatus === "FALSE_POSITIVE";
                const Icon = cleared ? CheckCircle2 : blocked ? Ban : ShieldAlert;
                return (
                  <Card key={a.caseId} pad>
                    <div style={{ display: "flex", gap: 16, alignItems: "flex-start", flexWrap: "wrap" }}>
                      <div className={`chip ${cleared ? "blue" : blocked ? "red" : "amber"}`} style={{ width: 46, height: 46 }}><Icon size={22} /></div>
                      <div style={{ flex: 1, minWidth: 220 }}>
                        <div style={{ display: "flex", justifyContent: "space-between", gap: 12, flexWrap: "wrap" }}>
                          <div><b style={{ fontSize: 17 }}>{money(a.amount)}</b> <span style={{ color: "var(--muted)" }}>at {a.merchant} · {a.location}</span></div>
                          <div style={{ display: "flex", gap: 8, alignItems: "center" }}><Badge status={a.transactionStatus} /><Badge status={a.caseStatus} /></div>
                        </div>
                        <div className="reason-tags">{splitReasons(a.reason).map((r) => <span className="tag" key={r}>{r}</span>)}</div>
                        <div style={{ fontSize: 13, color: "var(--muted)", marginTop: 12 }}>{EXPLAIN[a.caseStatus]} · Risk score {scorePct(a.fraudScore)}% · {timeAgo(a.createdAt)}</div>
                      </div>
                    </div>
                  </Card>
                );
              })}
            </div>
          )}
        </>
      )}
    </>
  );
}
