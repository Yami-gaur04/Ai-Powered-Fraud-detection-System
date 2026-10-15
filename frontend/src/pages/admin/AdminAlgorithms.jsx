import { useState } from "react";
import { Cpu, RefreshCw, Info } from "lucide-react";
import { useFetch } from "../../hooks/useFetch";
import { adminApi } from "../../api/endpoints";
import { useToast } from "../../context/ToastContext";
import { Card, PageHeader, ErrorState, BlockSkeleton, Modal, Button, Badge } from "../../components/ui";
import { fullDateTime, pct } from "../../utils/format";

export default function AdminAlgorithms() {
  const toast = useToast();
  const { data, loading, error, reload } = useFetch(async () => {
    const [versions, report] = await Promise.all([adminApi.algorithms(), adminApi.reports()]);
    return { versions, report };
  }, []);
  const [open, setOpen] = useState(false);
  const [name, setName] = useState("");
  const [notes, setNotes] = useState("");
  const [busy, setBusy] = useState(false);

  const header = (disabled) => <PageHeader title="Algorithm updates" subtitle="Refine detection from reviewed cases and track every version"
    actions={<button className="btn" disabled={disabled} onClick={() => setOpen(true)}><RefreshCw size={16} /> Update algorithm</button>} />;
  if (error) return <>{header(true)}<ErrorState error={error} onRetry={reload} /></>;
  if (loading && !data) return <>{header(true)}<BlockSkeleton h={320} /></>;

  const { versions, report } = data;
  const reviewed = report.confirmedFraud + report.falsePositives;
  const active = versions.find((v) => v.active);

  const run = async () => {
    setBusy(true);
    try {
      const v = await adminApi.updateAlgorithm({ versionName: name.trim(), description: notes.trim() });
      toast.success(`Now running ${v.versionName}`);
      setOpen(false); setName(""); setNotes(""); reload();
    } catch (e) { toast.error(e.message); }
    finally { setBusy(false); }
  };

  return (
    <>
      {header(false)}
      <div className="grid-2" style={{ alignItems: "start" }}>
        <Card title="Version history" icon={Cpu}>
          <div className="timeline">
            {versions.map((v) => (
              <div className={`tl-item ${v.active ? "active" : ""}`} key={v.id}>
                <div style={{ display: "flex", justifyContent: "space-between", gap: 12, flexWrap: "wrap" }}>
                  <b>{v.versionName}</b><span className={`badge ${v.active ? "b-green" : "b-gray"}`}>{v.active ? "Active" : "Previous"}</span>
                </div>
                <div style={{ fontSize: 13.5, color: "var(--muted)", marginTop: 4 }}>{v.description}</div>
                <div style={{ fontSize: 12.5, color: "var(--faint)", marginTop: 6 }}>{fullDateTime(v.createdAt)}{v.accuracy != null && ` · precision ${pct(v.accuracy)}`}</div>
              </div>
            ))}
          </div>
        </Card>
        <div style={{ display: "grid", gap: 24 }}>
          <Card title="Performance data" pad>
            <div className="kv">
              <span>Active version</span><b>{active ? active.versionName : "–"}</b>
              <span>Reviewed cases</span><b>{reviewed}</b>
              <span>Confirmed fraud</span><b>{report.confirmedFraud}</b>
              <span>False positives</span><b>{report.falsePositives}</b>
              <span>Precision</span><b>{pct(report.detectionPrecisionPercent)}</b>
            </div>
          </Card>
          <div className="info-box"><Info size={18} />
            <div><b style={{ color: "var(--text)" }}>How an update works</b><div style={{ marginTop: 4 }}>The system measures precision from the cases you reviewed. Below 50% it raises the flag threshold to cut false alarms; above 80% it lowers it slightly to catch more fraud. A new version is saved and activated.</div>
              {reviewed === 0 && <div style={{ marginTop: 8, color: "var(--amber)" }}>Review some fraud cases first so there is feedback to learn from.</div>}</div></div>
        </div>
      </div>

      {open && (
        <Modal title="Update algorithm" onClose={() => setOpen(false)}>
          <p style={{ fontSize: 14, color: "var(--muted)", marginBottom: 16 }}>Uses {reviewed} reviewed case{reviewed === 1 ? "" : "s"} (precision {pct(report.detectionPrecisionPercent)}) to tune the detection thresholds.</p>
          <div className="field"><label>Version name (optional)</label><input value={name} onChange={(e) => setName(e.target.value)} placeholder="v2.0-tuned" /></div>
          <div className="field" style={{ marginTop: 14 }}><label>Notes (optional)</label><textarea value={notes} onChange={(e) => setNotes(e.target.value)} /></div>
          <div className="form-actions"><button className="btn ghost" onClick={() => setOpen(false)}>Cancel</button><Button className="btn" loading={busy} onClick={run}>Run update</Button></div>
        </Modal>
      )}
    </>
  );
}
