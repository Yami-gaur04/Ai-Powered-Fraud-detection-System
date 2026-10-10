import { useMemo, useState } from "react";
import { CreditCard, Download, Plus } from "lucide-react";
import { useUserData } from "../../hooks/useUserData";
import { useTable } from "../../hooks/useTable";
import { useSearch, matches } from "../../context/SearchContext";
import { Card, PageHeader, Chips, SortTh, Pagination, Badge, ScoreBar, Empty, ErrorState, BlockSkeleton } from "../../components/ui";
import TransactionModal from "../../components/modals/TransactionModal";
import TransactionDetail from "../../components/modals/TransactionDetail";
import { money, dateTime } from "../../utils/format";
import { downloadCSV } from "../../utils/csv";

export default function UserTransactions() {
  const { data, loading, error, reload, refresh } = useUserData();
  const { query } = useSearch();
  const [filter, setFilter] = useState("ALL");
  const [modal, setModal] = useState(false);
  const [detail, setDetail] = useState(null);

  const all = data?.txs || [];
  const rows = useMemo(
    () => all.filter((t) => (filter === "ALL" || t.status === filter) && matches(t, query, (t) => [t.merchant, t.location, t.deviceId, t.paymentMethod, t.status, t.amount])),
    [all, filter, query]
  );
  const table = useTable(rows, { initialSort: { key: "transactionTime", dir: "desc" }, getters: { risk: (t) => t.fraudScore } });
  const n = (s) => all.filter((t) => t.status === s).length;

  const exportCsv = () => downloadCSV("my-transactions.csv", [
    { header: "ID", value: (t) => t.id }, { header: "Time", value: (t) => t.transactionTime }, { header: "Merchant", value: (t) => t.merchant },
    { header: "Amount", value: (t) => t.amount }, { header: "Location", value: (t) => t.location }, { header: "Device", value: (t) => t.deviceId },
    { header: "Payment method", value: (t) => t.paymentMethod }, { header: "Risk score", value: (t) => t.fraudScore }, { header: "Status", value: (t) => t.status },
  ], table.sorted);

  return (
    <>
      <PageHeader title="Transactions" subtitle="Every transaction and the fraud risk we calculated for it"
        actions={<><button className="btn ghost" onClick={exportCsv} disabled={!rows.length}><Download size={16} /> Export CSV</button><button className="btn" onClick={() => setModal(true)}><Plus size={17} /> New transaction</button></>} />
      {error ? <ErrorState error={error} onRetry={reload} /> : loading && !data ? <BlockSkeleton h={320} /> : (
        <>
          <Chips value={filter} onChange={setFilter} options={[
            { value: "ALL", label: "All", count: all.length }, { value: "APPROVED", label: "Approved", count: n("APPROVED") },
            { value: "FLAGGED", label: "Flagged", count: n("FLAGGED") }, { value: "BLOCKED", label: "Blocked", count: n("BLOCKED") },
          ]} />
          <Card>
            {rows.length === 0 ? <Empty icon={CreditCard} title={all.length ? "No matching transactions" : "No transactions yet"} text={all.length ? "Try a different filter or search." : "Create one to see fraud detection in action."} /> : (
              <>
                <div className="table-wrap"><table>
                  <thead><tr>
                    <SortTh label="Merchant" k="merchant" table={table} /><SortTh label="Amount" k="amount" table={table} className="num" />
                    <th>Location</th><th>Device</th><SortTh label="Time" k="transactionTime" table={table} />
                    <SortTh label="Risk" k="risk" table={table} /><th>Status</th>
                  </tr></thead>
                  <tbody>{table.pageRows.map((t) => (
                    <tr key={t.id} className="click" onClick={() => setDetail(t)}>
                      <td><b>{t.merchant}</b><span className="sub">{t.paymentMethod}</span></td>
                      <td className="num"><b>{money(t.amount)}</b></td><td>{t.location}</td><td>{t.deviceId}</td>
                      <td>{dateTime(t.transactionTime)}</td><td><ScoreBar score={t.fraudScore} /></td><td><Badge status={t.status} /></td>
                    </tr>))}</tbody>
                </table></div>
                <Pagination table={table} />
              </>
            )}
          </Card>
        </>
      )}
      {modal && <TransactionModal onClose={() => setModal(false)} onDone={refresh} />}
      {detail && <TransactionDetail tx={detail} onClose={() => setDetail(null)} />}
    </>
  );
}
