import { useState } from "react";
import { LogOut, Wifi, RefreshCw, Moon } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { useTheme } from "../context/ThemeContext";
import { useToast } from "../context/ToastContext";
import { useApiStatus } from "../hooks/useApiStatus";
import { API_BASE } from "../api/client";
import { adminApi, txApi } from "../api/endpoints";
import { Card, PageHeader, Switch, Button } from "../components/ui";
import { getDeviceId, resetDeviceId, clearSavedLocation, getSavedLocation } from "../utils/device";
import { initials, fullDateTime } from "../utils/format";

export default function SettingsPage() {
  const { user, logout } = useAuth();
  const { theme, toggle } = useTheme();
  const toast = useToast();
  const status = useApiStatus();
  const isAdmin = user.role === "ADMIN";
  const [device, setDevice] = useState(getDeviceId());
  const [ping, setPing] = useState(null);
  const [testing, setTesting] = useState(false);

  const test = async () => {
    setTesting(true);
    const t0 = performance.now();
    try { await (isAdmin ? adminApi.config() : txApi.mine()); setPing(Math.round(performance.now() - t0)); }
    catch (e) { setPing(null); toast.error(e.message); }
    finally { setTesting(false); }
  };

  return (
    <>
      <PageHeader title="Settings" subtitle="Your account, appearance and connection" />
      <div className="grid-2 even" style={{ alignItems: "start" }}>
        <Card title="Profile" pad>
          <div style={{ display: "flex", gap: 16, alignItems: "center", marginBottom: 20 }}>
            <div className="avatar-btn" style={{ width: 60, height: 60, fontSize: 20, display: "grid", placeItems: "center", cursor: "default" }}>{initials(user.name)}</div>
            <div><div style={{ fontWeight: 600, fontSize: 18 }}>{user.name}</div><div style={{ color: "var(--muted)" }}>{user.email}</div></div>
          </div>
          <div className="kv">
            <span>Role</span><b>{isAdmin ? "Administrator" : "User"}</b>
            <span>Session expires</span><b>{user.exp ? fullDateTime(user.exp) : "–"}</b>
          </div>
          <div className="form-actions" style={{ justifyContent: "flex-start" }}>
            <button className="btn danger" onClick={() => logout()}><LogOut size={17} /> Log out</button>
          </div>
        </Card>

        <div style={{ display: "grid", gap: 24 }}>
          <Card title="Appearance" icon={Moon} pad>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
              <div><b>Dark mode</b><div style={{ fontSize: 13, color: "var(--muted)" }}>Easier on the eyes at night</div></div>
              <Switch on={theme === "dark"} onChange={toggle} label="Dark mode" />
            </div>
          </Card>

          <Card title="Connection" icon={Wifi} pad>
            <div className="kv">
              <span>API URL</span><b>{API_BASE}</b>
              <span>Status</span><b><span className={`badge ${status ? "b-green" : status === false ? "b-red" : "b-gray"}`}>{status === null ? "Unknown" : status ? "Connected" : "Unreachable"}</span></b>
              <span>Latency</span><b>{ping != null ? `${ping} ms` : "–"}</b>
            </div>
            <div className="form-actions" style={{ justifyContent: "flex-start" }}>
              <Button className="btn ghost sm" loading={testing} onClick={test}>Test connection</Button>
            </div>
          </Card>

          {!isAdmin && (
            <Card title="This device" pad>
              <div className="kv">
                <span>Device ID</span><b>{device}</b>
                <span>Saved location</span><b>{getSavedLocation() || "–"}</b>
              </div>
              <p style={{ fontSize: 13, color: "var(--muted)", margin: "12px 0" }}>The backend raises risk when it sees a device or location it hasn't seen before. Use this to simulate that while testing.</p>
              <div className="form-actions" style={{ justifyContent: "flex-start", marginTop: 0 }}>
                <button className="btn ghost sm" onClick={() => { setDevice(resetDeviceId()); toast.success("New device ID generated."); }}><RefreshCw size={15} /> Simulate a new device</button>
                <button className="btn ghost sm" onClick={() => { clearSavedLocation(); toast.info("Saved location cleared."); }}>Clear location</button>
              </div>
            </Card>
          )}
        </div>
      </div>
    </>
  );
}
