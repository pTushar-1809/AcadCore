import { useAuth } from "../context/AuthContext";

export default function Dashboard() {
  const { user, logout } = useAuth();

  return (
    <div>
      <h1>Welcome to AcadCore</h1>

      <p>
        Logged in as: <strong>{user?.fullName}</strong>
      </p>

      <p>
        Role: <strong>{user?.role}</strong>
      </p>

      <button onClick={logout}>Logout</button>
    </div>
  );
}