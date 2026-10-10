import { Navigate, Outlet, Route, Routes, useLocation } from "react-router-dom";
import { useAuth } from "./context/AuthContext";
import { homeFor } from "./config/constants";
import AppShell from "./components/layout/AppShell";
import Login from "./pages/Login";
import NotFound from "./pages/NotFound";
import SettingsPage from "./pages/SettingsPage";
import UserDashboard from "./pages/user/UserDashboard";
import UserTransactions from "./pages/user/UserTransactions";
import UserAlerts from "./pages/user/UserAlerts";
import AdminDashboard from "./pages/admin/AdminDashboard";
import AdminCases from "./pages/admin/AdminCases";
import AdminReports from "./pages/admin/AdminReports";
import AdminConfig from "./pages/admin/AdminConfig";
import AdminAlgorithms from "./pages/admin/AdminAlgorithms";

/** Only lets the given role in; everyone else is redirected. */
function RequireRole({ role }) {
  const { user } = useAuth();
  const location = useLocation();
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />;
  if (user.role !== role) return <Navigate to={homeFor(user.role)} replace />;
  return <AppShell role={role} />;
}

function Home() {
  const { user } = useAuth();
  return <Navigate to={user ? homeFor(user.role) : "/login"} replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/login" element={<Login />} />

      <Route path="/user" element={<RequireRole role="USER" />}>
        <Route index element={<UserDashboard />} />
        <Route path="transactions" element={<UserTransactions />} />
        <Route path="alerts" element={<UserAlerts />} />
        <Route path="settings" element={<SettingsPage />} />
      </Route>

      <Route path="/admin" element={<RequireRole role="ADMIN" />}>
        <Route index element={<AdminDashboard />} />
        <Route path="cases" element={<AdminCases />} />
        <Route path="reports" element={<AdminReports />} />
        <Route path="config" element={<AdminConfig />} />
        <Route path="algorithms" element={<AdminAlgorithms />} />
        <Route path="settings" element={<SettingsPage />} />
      </Route>

      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}
