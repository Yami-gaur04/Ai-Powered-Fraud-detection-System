import { useEffect, useMemo, useState } from "react";
import { FlaskConical, RotateCcw, Save } from "lucide-react";
import { useFetch } from "../../hooks/useFetch";
import { adminApi } from "../../api/endpoints";
import { useToast } from "../../context/ToastContext";
import { Card, PageHeader, ErrorState, BlockSkeleton, Button, Badge } from "../../components/ui";

const FIELDS = [
  { k: "flagThreshold", label: "Flag threshold", min: 0, max: 1, step: 0.01, slider: true, hint: "Score at or above this is FLAGGED" },
  { k: "blockThreshold", label: "Block threshold", min: 0, max: 1, step: 0.01, slider: true, hint: "Score at or above this is BLOCKED" },
  { k: "highAmountLimit", label: "High amount limit (INR)", min: 1, step: 1, hint: "Amounts near or above this add risk" },
  { k: "maxTxPerHour", label: "Max transactions per hour", min: 1, step: 1, hint: "Velocity rule limit" },
  { k: "amountWeight", label: "Amount weight", min: 0, max: 1, step: 0.05, slider: true },
  { k: "velocityWeight", label: "Velocity weight", min: 0, max: 1, step: 0.05, slider: true },
  { k: "locationWeight", label: "Location weight", min: 0, max: 1, step: 0.05, slider: true },
  { k: "deviceWeight", label: "Device weight", min: 0, max: 1, step: 0.05, slider: true },
];
const KEYS = FIELDS.map((f) => f.k);
const toForm = (c) => Object.fromEntries(KEYS.map((k) => [k, String(c[k])]));

export default function AdminConfig() {
  const toast = useToast();
  const { data, loading, error, reload } = useFetch(() => adminApi.config(), []);
  const [form, setForm] = useState(null);
  const [saved, setSaved] = useState(null);
  const [busy, setBusy] = useState(false);
  const [sim, setSim] = useState({ amount: "60000", txs: "2", newLoc: true, newDev: false });

  useEffect(() => { if (data) { setForm(toForm(data)); setSaved(toForm(data)); } }, [data]);

  const n = (k) => parseFloat(form?.[k]);
  const dirty = form && saved && KEYS.some((k) => form[k] !== saved[k]);

  const errors = useMemo(() => {
    if (!form) return {};
    const e = {};
    FIELDS.forEach((f) => {
      const v = parseFloat(form[f.k]);
      if (Number.isNaN(v)) e[f.k] = "Enter a number";
      else if (f.min != null && v < f.min) e[f.k] = `Must be at least ${f.min}`;
      else if (f.max != null && v > f.max) e[f.k] = `Must be at most ${f.max}`;
    });
    if (!e.flagThreshold && !e.blockThreshold && n("flagThreshold") >= n("blockThreshold")) e.flagThreshold = "Must be lower than the block threshold";
    if (!KEYS.slice(4).some((k) => e[k]) && KEYS.slice(4).reduce((a, k) => a + n(k), 0) <= 0) e.amountWeight = "At least one weight must be above 0";
    return e;
  }, [form]); // eslint-disable-line react-hooks/exhaustive-deps

  const valid = Object.keys(errors).length === 0;
  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));

  const save = async () => {
    setBusy(true);
    try {
      const body = Object.fromEntries(KEYS.map((k) => [k, n(k)]));
      body.maxTxPerHour = Math.round(body.maxTxPerHour);
      const res = await adminApi.updateConfig(body);
      setSaved(toForm(res)); setForm(toForm(res));
      toast.success("Configuration updated.");
    } catch (e) { toast.error(e.message); }
    finally { setBusy(false); }
  };

  /* what-if simulator (mirrors the backend scoring, ignoring the "3x your average" rule) */
  const simResult = useMemo(() => {
    if (!form || !valid) return null;
    const w = { a: n("amountWeight"), v: n("velocityWeight"), l: n("locationWeight"), d: n("deviceWeight") };
    const total = w.a + w.v + w.l + w.d;
    const amt = Math.min(1, (parseFloat(sim.amount) || 0) / n("highAmountLimit"));
    const vel = Math.min(1, (parseFloat(sim.txs) || 0) / n("maxTxPerHour"));
    const score = total ? (amt * w.a + vel * w.v + (sim.newLoc ? 1 : 0) * w.l + (sim.newDev ? 1 : 0) * w.d) / total : 0;
    const status = score >= n("blockThreshold") ? "BLOCKED" : score >= n("flagThreshold") ? "FLAGGED" : "APPROVED";
    return { score, status };
  }, [form, sim, valid]); // eslint-disable-line react-hooks/exhaustive-deps

  const header = <PageHeader title="System configuration" subtitle="Tune detection thresholds and risk weights" />;
  if (error) return <>{header}<ErrorState error={error} onRetry={reload} /></>;
  if (!form) return <>{header}<BlockSkeleton h={380} /></>;

  const flag = Math.min(1, Math.max(0, n("flagThreshold") || 0)), block = Math.min(1, Math.max(flag, n("blockThreshold") || 0));
  const wsum = KEYS.slice(4).reduce((a, k) => a + (n(k) || 0), 0);

  const input = (f) => (
    <div className={`field ${errors[f.k] ? "bad" : ""}`} key={f.k}>
      <label htmlFor={f.k}>{f.label}</label>
      {f.slider ? (
        <div className="slider-row">
          <input type="range" min={f.min} max={f.max} step={f.step} value={Number.isNaN(n(f.k)) ? 0 : n(f.k)} onChange={(e) => set(f.k, e.target.value)} />
          <input id={f.k} type="number" min={f.min} max={f.max} step={f.step} value={form[f.k]} onChange={(e) => set(f.k, e.target.value)} />
        </div>
      ) : <input id={f.k} type="number" min={f.min} step={f.step} value={form[f.k]} onChange={(e) => set(f.k, e.target.value)} />}
      {errors[f.k] ? <span className="err">{errors[f.k]}</span> : f.hint && <span className="hint">{f.hint}</span>}
    </div>
  );

  return (
    <>
      {header}
      <div className="grid-2" style={{ alignItems: "start" }}>
        <div style={{ display: "grid", gap: 24 }}>
          <Card title="Detection thresholds" pad>
            <div className="zones" style={{ marginBottom: 6 }}>
              <div style={{ flex: flag || 0.0001, background: "#22c55e" }}>{flag > 0.12 && "Approve"}</div>
              <div style={{ flex: block - flag || 0.0001, background: "#f59e0b" }}>{block - flag > 0.12 && "Flag"}</div>
              <div style={{ flex: 1 - block || 0.0001, background: "#dc2626" }}>{1 - block > 0.12 && "Block"}</div>
            </div>
            <div className="zone-axis"><span>0</span><span>{flag}</span><span>{block}</span><span>1</span></div>
            <div className="form-grid" style={{ marginTop: 22 }}>{input(FIELDS[0])}{input(FIELDS[1])}{input(FIELDS[2])}{input(FIELDS[3])}</div>
          </Card>
          <Card title="Risk weights" pad>
            <div className="form-grid">{FIELDS.slice(4).map(input)}</div>
            <div className="contrib" style={{ marginTop: 22 }}>
              {FIELDS.slice(4).map((f) => (
                <div className="row" key={f.k}><span>{f.label.replace(" weight", "")}</span>
                  <div className="track"><i style={{ width: `${wsum ? ((n(f.k) || 0) / wsum) * 100 : 0}%` }} /></div>
                  <b>{wsum ? Math.round(((n(f.k) || 0) / wsum) * 100) : 0}%</b></div>
              ))}
            </div>
            <div className="hint" style={{ marginTop: 10, fontSize: 12 }}>Weights are relative: the score is the weighted average of each risk factor.</div>
          </Card>
        </div>

        <Card title="Try your settings" icon={FlaskConical} pad>
          <p style={{ fontSize: 13.5, color: "var(--muted)", marginBottom: 16 }}>See how a transaction would be judged with the values in the form, before you save them.</p>
          <div style={{ display: "grid", gap: 14 }}>
            <div className="field"><label>Amount (INR)</label><input type="number" value={sim.amount} onChange={(e) => setSim({ ...sim, amount: e.target.value })} /></div>
            <div className="field"><label>Transactions in the last hour (including this one)</label><input type="number" min="1" value={sim.txs} onChange={(e) => setSim({ ...sim, txs: e.target.value })} /></div>
            <label className="check"><input type="checkbox" checked={sim.newLoc} onChange={(e) => setSim({ ...sim, newLoc: e.target.checked })} /> New location</label>
            <label className="check"><input type="checkbox" checked={sim.newDev} onChange={(e) => setSim({ ...sim, newDev: e.target.checked })} /> New device</label>
          </div>
          <div className="info-box" style={{ marginTop: 20, justifyContent: "space-between", alignItems: "center" }}>
            {simResult ? <><div><div style={{ fontSize: 12.5 }}>Predicted risk score</div><b style={{ fontSize: 26, color: "var(--text)" }}>{Math.round(simResult.score * 100)}%</b></div><Badge status={simResult.status} /></> : <span>Fix the errors in the form to run a simulation.</span>}
          </div>
        </Card>
      </div>

      {dirty && (
        <div className="sticky-save">
          <span style={{ fontSize: 14 }}>You have unsaved changes</span>
          <div style={{ display: "flex", gap: 10 }}>
            <button className="btn ghost" onClick={() => setForm(saved)}><RotateCcw size={16} /> Discard</button>
            <Button className="btn" loading={busy} disabled={!valid} onClick={save}><Save size={16} /> Save configuration</Button>
          </div>
        </div>
      )}
    </>
  );
}
