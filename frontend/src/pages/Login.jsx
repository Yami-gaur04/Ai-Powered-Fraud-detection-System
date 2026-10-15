import { useState } from "react";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import { ShieldCheck, Eye, EyeOff, Zap, LineChart, SlidersHorizontal } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { authApi } from "../api/endpoints";
import { Button } from "../components/ui";
import { homeFor } from "../config/constants";

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export default function Login() {
  const { user, signIn } = useAuth();
  const navigate = useNavigate();
  const from = useLocation().state?.from;
  const [mode, setMode] = useState("login");
  const [form, setForm] = useState({ name: "", email: "", password: "" });
  const [show, setShow] = useState(false);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const reg = mode === "register";
  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }));

  if (user) return <Navigate to={homeFor(user.role)} replace />;

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    if (reg && !form.name.trim()) return setError("Please enter your name.");
    if (!EMAIL_RE.test(form.email.trim())) return setError("Please enter a valid email address.");
    if (reg && form.password.length < 6) return setError("Password must be at least 6 characters.");
    if (!form.password) return setError("Please enter your password.");
    setBusy(true);
    try {
      const res = reg
        ? await authApi.register({ name: form.name.trim(), email: form.email.trim(), password: form.password })
        : await authApi.login({ email: form.email.trim(), password: form.password });
      const s = signIn(res);
      const target = from && from.startsWith(homeFor(s.role)) ? from : homeFor(s.role);
      navigate(target, { replace: true });
    } catch (err) { setError(err.message); setBusy(false); }
  };

  return (
    <div className="login">
      <aside className="login-brand">
        <div className="brand"><div className="logo"><ShieldCheck size={22} /></div><span>FraudShield AI</span></div>
        <div>
          <h2>Catch fraud before it costs you.</h2>
          <p>Every transaction is scored in real time against amount, frequency, location and device patterns, and reviewed by your team.</p>
          <div className="feat">
            <div><span className="chip"><Zap size={18} /></span>Instant risk scoring on every transaction</div>
            <div><span className="chip"><LineChart size={18} /></span>Live reports and detection analytics</div>
            <div><span className="chip"><SlidersHorizontal size={18} /></span>Tunable thresholds and self-improving algorithm</div>
          </div>
        </div>
        <small style={{ color: "#6e6e7a" }}>© 2026 FraudShield AI</small>
      </aside>

      <section className="login-form">
        <form className="login-card" onSubmit={submit} noValidate>
          <h1>{reg ? "Create your account" : "Welcome back"}</h1>
          <p className="sub">{reg ? "Start monitoring your transactions for fraud." : "Sign in to your dashboard."}</p>
          <div className="tabs">
            <button type="button" className={!reg ? "on" : ""} onClick={() => { setMode("login"); setError(""); }}>Sign in</button>
            <button type="button" className={reg ? "on" : ""} onClick={() => { setMode("register"); setError(""); }}>Create account</button>
          </div>
          {error && <div className="auth-err" role="alert">{error}</div>}
          {reg && (
            <div className="field"><label htmlFor="name">Full name</label>
              <div className="input-wrap"><input id="name" value={form.name} onChange={set("name")} placeholder="Yamini Sharma" autoComplete="name" /></div></div>
          )}
          <div className="field"><label htmlFor="email">Email</label>
            <input id="email" type="email" value={form.email} onChange={set("email")} placeholder="you@example.com" autoComplete="email" autoFocus /></div>
          <div className="field"><label htmlFor="pw">Password</label>
            <div className="input-wrap">
              <input id="pw" type={show ? "text" : "password"} value={form.password} onChange={set("password")} placeholder={reg ? "At least 6 characters" : "Your password"} autoComplete={reg ? "new-password" : "current-password"} />
              <button type="button" className="eye" onClick={() => setShow((s) => !s)} aria-label={show ? "Hide password" : "Show password"}>{show ? <EyeOff size={18} /> : <Eye size={18} />}</button>
            </div></div>
          <Button className="btn" type="submit" loading={busy}>{reg ? "Create account" : "Sign in"}</Button>
          {import.meta.env.DEV && !reg && (
            <p className="dev-tip">Development: <button type="button" onClick={() => setForm((f) => ({ ...f, email: "admin@fraud.com", password: "Admin@123" }))}>fill default admin login</button></p>
          )}
        </form>
      </section>
    </div>
  );
}
