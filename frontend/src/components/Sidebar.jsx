import {
  LayoutDashboard,
  GraduationCap,
  BookOpen,
  Users,
  ClipboardList,
  LogOut,
} from "lucide-react";
import { useAuth } from "../context/AuthContext";

export default function Sidebar() {
  const { logout } = useAuth();

  return (
    <aside className="sidebar">
      <div className="logo">
        <GraduationCap size={28} />
        <span>AcadCore</span>
      </div>

      <nav>
        <a href="/admin">
          <LayoutDashboard size={20} />
          Dashboard
        </a>

        <a href="/admin/classes">
          <BookOpen size={20} />
          Classes
        </a>

        <a href="/admin/subjects">
          <BookOpen size={20} />
          Subjects
        </a>

        <a href="/admin/faculty">
          <Users size={20} />
          Faculty
        </a>

        <a href="/admin/students">
          <GraduationCap size={20} />
          Students
        </a>

        <a href="/admin/assessments">
          <ClipboardList size={20} />
          Assessments
        </a>
      </nav>

      <button className="logout-btn" onClick={logout}>
        <LogOut size={20} />
        Logout
      </button>
    </aside>
  );
}