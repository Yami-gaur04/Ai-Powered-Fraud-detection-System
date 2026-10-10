import { Modal, Badge, ScoreBar } from "../ui";
import { money, fullDateTime } from "../../utils/format";

export default function TransactionDetail({ tx, onClose }) {
  return (
    <Modal title={`Transaction #${tx.id}`} onClose={onClose} width={500}>
      <div className="kv">
        <span>Status</span><b><Badge status={tx.status} /></b>
        <span>Amount</span><b>{money(tx.amount)}</b>
        <span>Merchant</span><b>{tx.merchant}</b>
        <span>Location</span><b>{tx.location}</b>
        <span>Device</span><b>{tx.deviceId}</b>
        <span>Payment method</span><b>{tx.paymentMethod || "–"}</b>
        <span>Time</span><b>{fullDateTime(tx.transactionTime)}</b>
        <span>Risk score</span><div style={{ justifySelf: "end", minWidth: 180 }}><ScoreBar score={tx.fraudScore} /></div>
      </div>
    </Modal>
  );
}
