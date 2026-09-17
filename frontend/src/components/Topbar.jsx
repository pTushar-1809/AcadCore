import { Bell, Search } from "lucide-react";
import { useAuth } from "../context/AuthContext";

export default function Topbar() {
  const { user } = useAuth();

  return (
    <header className="topbar">
      <div>
        <h2>Dashboard</h2>
        <p>Welcome back, {user?.fullName || "Admin"}</p>
      </div>

      <div className="topbar-right">
        <div className="search-box">
          <Search size={18} />
          <input placeholder="Search..." />
        </div>

        <Bell size={21} />

        <div className="profile">
          <div className="avatar">
            {(user?.fullName || "A").charAt(0).toUpperCase()}
          </div>

          <div>
            <strong>{user?.fullName || "Admin"}</strong>
            <small>{user?.role || "ADMIN"}</small>
          </div>
        </div>
      </div>
    </header>
  );
}