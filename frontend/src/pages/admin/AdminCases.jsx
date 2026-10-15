import { useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { ShieldCheck, Download } from "lucide-react";
import { useAdminData } from "../../hooks/useAdminData";
import { useTable } from "../../hooks/useTable";
import { useSearch, matches } from "../../context/SearchContext";
import { Card, PageHeader, Chips, SortTh, Pagination, Badge, ScoreBar, Empty, ErrorState, BlockSkeleton } from "../../components/ui";
import ReviewModal from "../../components/modals/ReviewModal";
import { money, dateTime, splitReasons } from "../../utils/format";
import { downloadCSV } from "../../utils/csv";

export default function AdminCases() {
  const [params, setParams] = useSearchParams();
  const status = params.get("status") || "ALL";
  const { data, loading, error, reload, refresh } = useAdminData();
  const { query } = useSearch();
  const [review, setReview] = useState(null);

  const all = data?.cases || [];
  const rows = useMemo(() => all.filter((c) =>
    (status === "ALL" || c.caseStatus === status) &&
    matches(c, query, (c) => [c.caseId, c.userEmail, c.merchant, c.location, c.reason, c.amount, c.caseStatus])), [all, status, query]);
  const table = useTable(rows, { initialSort: { key: "createdAt", dir: "desc" } });
  const n = (s) => all.filter((c) => c.caseStatus === s).length;

  const exportCsv = () => downloadCSV("fraud-cases.csv", [
    { header: "Case", value: (c) => c.caseId }, { header: "Detected", value: (c) => c.createdAt }, { header: "User", value: (c) => c.userEmail },
    { header: "Merchant", value: (c) => c.merchant }, { header: "Amount", value: (c) => c.amount }, { header: "Location", value: (c) => c.location },
    { header: "Risk score", value: (c) => c.fraudScore }, { header: "Reason", value: (c) => c.reason }, { header: "Case status", value: (c) => c.caseStatus },
    { header: "Feedback", value: (c) => c.adminFeedback },
  ], table.sorted);

  const setStatus = (v) => setParams(v === "ALL" ? {} : { status: v });

  return (
    <>
      <PageHeader title="Fraud cases" subtitle="Monitor detected fraud and review cases to improve accuracy"
        actions={<button className="btn ghost" onClick={exportCsv} disabled={!rows.length}><Download size={16} /> Export CSV</button>} />
      {error ? <ErrorState error={error} onRetry={reload} /> : loading && !data ? <BlockSkeleton h={340} /> : (
        <>
          <Chips value={status} onChange={setStatus} options={[
            { value: "ALL", label: "All", count: all.length }, { value: "OPEN", label: "Open", count: n("OPEN") },
            { value: "CONFIRMED_FRAUD", label: "Confirmed fraud", count: n("CONFIRMED_FRAUD") }, { value: "FALSE_POSITIVE", label: "False positives", count: n("FALSE_POSITIVE") },
          ]} />
          <Card>
            {rows.length === 0 ? <Empty icon={ShieldCheck} title="No cases found" text={all.length ? "Try a different filter or search." : "No transaction has been flagged yet."} /> : (
              <>
                <div className="table-wrap"><table>
                  <thead><tr>
                    <SortTh label="Case" k="caseId" table={table} /><th>User</th><SortTh label="Transaction" k="amount" table={table} />
                    <th>Location</th><SortTh label="Risk" k="fraudScore" table={table} /><th>Why flagged</th><th>Status</th><th />
                  </tr></thead>
                  <tbody>{table.pageRows.map((c) => (
                    <tr key={c.caseId}>
                      <td>#{c.caseId}<span className="sub">{dateTime(c.createdAt)}</span></td>
                      <td>{c.userEmail}</td>
                      <td><b>{money(c.amount)}</b><span className="sub">{c.merchant}</span></td>
                      <td>{c.location}</td><td><ScoreBar score={c.fraudScore} /></td>
                      <td style={{ maxWidth: 300 }}><div className="reason-tags" style={{ marginTop: 0 }}>{splitReasons(c.reason).map((r) => <span className="tag" key={r}>{r}</span>)}</div></td>
                      <td><Badge status={c.caseStatus} /></td>
                      <td><button className={`btn sm ${c.caseStatus === "OPEN" ? "" : "ghost"}`} onClick={() => setReview(c)}>{c.caseStatus === "OPEN" ? "Review" : "Edit"}</button></td>
                    </tr>))}</tbody>
                </table></div>
                <Pagination table={table} />
              </>
            )}
          </Card>
        </>
      )}
      {review && <ReviewModal item={review} onClose={() => setReview(null)} onDone={refresh} />}
    </>
  );
}
