import { useState } from "react";
import { Modal, Button, ScoreBar } from "../ui";
import { adminApi } from "../../api/endpoints";
import { useToast } from "../../context/ToastContext";
import { money, fullDateTime, splitReasons } from "../../utils/format";

/** Admin verdict on a fraud case. This feedback is what "Update algorithm" learns from. */
export default function ReviewModal({ item, onClose, onDone }) {
  const toast = useToast();
  const [verdict, setVerdict] = useState(item.caseStatus === "OPEN" ? "" : item.caseStatus);
  const [feedback, setFeedback] = useState(item.adminFeedback || "");
  const [busy, setBusy] = useState(false);

  const save = async () => {
    if (!verdict) { toast.error("Choose confirm fraud or false positive."); return; }
    setBusy(true);
    try {
      await adminApi.review(item.caseId, { status: verdict, feedback: feedback.trim() });
      toast.success(`Case #${item.caseId} marked as ${verdict === "CONFIRMED_FRAUD" ? "confirmed fraud" : "a false positive"}.`);
      onClose(); onDone?.();
    } catch (e) { toast.error(e.message); setBusy(false); }
  };

  return (
    <Modal title={`Review case #${item.caseId}`} onClose={onClose}>
      <div className="kv" style={{ marginBottom: 14 }}>
        <span>User</span><b>{item.userEmail}</b>
        <span>Transaction</span><b>{money(item.amount)} at {item.merchant}</b>
        <span>Location</span><b>{item.location}</b>
        <span>Detected</span><b>{fullDateTime(item.createdAt)}</b>
        <span>Risk score</span><div style={{ justifySelf: "end", minWidth: 180 }}><ScoreBar score={item.fraudScore} /></div>
      </div>
      <div className="reason-tags" style={{ marginBottom: 18 }}>{splitReasons(item.reason).map((r) => <span className="tag" key={r}>{r}</span>)}</div>
      <div className="radio-row">
        <label className="radio-card"><input type="radio" name="v" checked={verdict === "CONFIRMED_FRAUD"} onChange={() => setVerdict("CONFIRMED_FRAUD")} /><div><b>Confirm fraud</b><span>Transaction stays blocked</span></div></label>
        <label className="radio-card"><input type="radio" name="v" checked={verdict === "FALSE_POSITIVE"} onChange={() => setVerdict("FALSE_POSITIVE")} /><div><b>False positive</b><span>Transaction is approved</span></div></label>
      </div>
      <div className="field" style={{ marginTop: 16 }}>
        <label>Feedback (used to improve the algorithm)</label>
        <textarea value={feedback} maxLength={480} onChange={(e) => setFeedback(e.target.value)} placeholder="e.g. Verified with the customer by phone" />
      </div>
      <div className="form-actions">
        <button className="btn ghost" onClick={onClose}>Cancel</button>
        <Button className="btn" loading={busy} onClick={save}>Save review</Button>
      </div>
    </Modal>
  );
}
