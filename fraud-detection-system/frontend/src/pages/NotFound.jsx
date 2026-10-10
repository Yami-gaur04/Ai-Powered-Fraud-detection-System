import { Link } from "react-router-dom";
export default function NotFound() {
  return (
    <div className="not-found"><div>
      <h1>404</h1><p style={{ color: "var(--muted)", margin: "6px 0 20px" }}>We couldn't find that page.</p>
      <Link className="btn" to="/">Go home</Link>
    </div></div>
  );
}
