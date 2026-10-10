import { useState } from "react";
import { CheckCircle2, ShieldAlert, Ban, Sparkles } from "lucide-react";
import { Modal, Button } from "../ui";
import { txApi } from "../../api/endpoints";
import { useToast } from "../../context/ToastContext";
import { PAYMENT_METHODS } from "../../config/constants";
import { getDeviceId, getSavedLocation, saveLocation, randomDeviceId } from "../../utils/device";
import { money, scorePct, splitReasons } from "../../utils/format";

const RESULT = {
  APPROVED: { tone: "green", icon: CheckCircle2, title: "Transaction approved" },
  FLAGGED: { tone: "amber", icon: ShieldAlert, title: "Transaction flagged for review" },
  BLOCKED: { tone: "red", icon: Ban, title: "Transaction blocked" },
};

/** Create a transaction; the backend scores it instantly and we show the verdict. */
export default function TransactionModal({ prefill = {}, onClose, onDone }) {
  const toast = useToast();
  const [form, setForm] = useState({
    amount: prefill.amount ?? "", merchant: prefill.merchant ?? "",
    location: getSavedLocation(), deviceId: getDeviceId(), paymentMethod: "UPI",
  });
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);
  const [result, setResult] = useState(null);
  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }));

  const validate = () => {
    const e = {};
    if (!(parseFloat(form.amount) > 0)) e.amount = "Enter an amount greater than 0";
    if (!form.merchant.trim()) e.merchant = "Merchant is required";
    if (!form.location.trim()) e.location = "Location is required";
    if (!form.deviceId.trim()) e.deviceId = "Device ID is required";
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const submit = async () => {
    if (!validate()) return;
    setBusy(true);
    try {
      const r = await txApi.create({
        amount: parseFloat(form.amount), merchant: form.merchant.trim(), location: form.location.trim(),
        deviceId: form.deviceId.trim(), paymentMethod: form.paymentMethod,
      });
      saveLocation(form.location.trim());
      setResult(r);
    } catch (e) { toast.error(e.message); }
    finally { setBusy(false); }
  };

  const finish = () => { onClose(); onDone?.(); };

  if (result) {
    const m = RESULT[result.status], Icon = m.icon;
    const reasons = result.status === "APPROVED" ? [] : splitReasons(result.message.split("Reason:")[1] || "");
    return (
      <Modal title="Result" onClose={finish} width={480}>
        <div className="result">
          <div className={`ring ${m.tone}`}><Icon size={40} /></div>
          <h4>{m.title}</h4>
          <p>{money(result.amount)} at {result.merchant} · risk score <b>{scorePct(result.fraudScore)}%</b></p>
          {reasons.length > 0 && <div className="reasons"><b>Why:</b><ul>{reasons.map((r) => <li key={r}>{r}</li>)}</ul></div>}
          <div className="form-actions" style={{ justifyContent: "center" }}><button className="btn" onClick={finish}>Done</button></div>
        </div>
      </Modal>
    );
  }

  const scenario = (kind) => setForm((f) => kind === "normal"
    ? { ...f, amount: "500", merchant: "Amazon", location: getSavedLocation() || "Delhi", deviceId: getDeviceId() }
    : { ...f, amount: "95000", merchant: "Unknown Store", location: "Dubai", deviceId: randomDeviceId() });

  // a plain render function (not a component) so inputs keep focus while typing
  const field = (k, label, props = {}, hint, full) => (
    <div className={`field ${errors[k] ? "bad" : ""} ${full ? "full" : ""}`} key={k}>
      <label>{label}</label><input value={form[k]} onChange={set(k)} {...props} />
      {errors[k] ? <span className="err">{errors[k]}</span> : hint && <span className="hint">{hint}</span>}
    </div>
  );

  return (
    <Modal title="New transaction" onClose={onClose}>
      <div className="chips" style={{ marginBottom: 16 }}>
        <span style={{ fontSize: 13, color: "var(--muted)", display: "inline-flex", gap: 6, alignItems: "center" }}><Sparkles size={15} /> Try a scenario:</span>
        <button className="chip-btn" onClick={() => scenario("normal")}>Everyday purchase</button>
        <button className="chip-btn" onClick={() => scenario("fraud")}>Suspicious purchase</button>
      </div>
      <div className="form-grid">
        {field("amount", "Amount (INR)", { type: "number", min: "0.01", step: "0.01", placeholder: "2500" })}
        {field("merchant", "Merchant", { placeholder: "Amazon" })}
        {field("location", "Location", { placeholder: "Delhi" })}
        <div className="field"><label>Payment method</label>
          <select value={form.paymentMethod} onChange={set("paymentMethod")}>{PAYMENT_METHODS.map((p) => <option key={p}>{p}</option>)}</select></div>
        {field("deviceId", "Device ID", {}, "A new location or device that we haven't seen before raises the risk score.", true)}
      </div>
      <div className="form-actions">
        <button className="btn ghost" onClick={onClose}>Cancel</button>
        <Button className="btn" loading={busy} onClick={submit}>Submit transaction</Button>
      </div>
    </Modal>
  );
}
