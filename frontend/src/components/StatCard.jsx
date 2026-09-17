export default function StatCard({ title, value, icon: Icon, description }) {
  return (
    <div className="stat-card">
      <div className="stat-icon">
        <Icon size={24} />
      </div>

      <div>
        <p>{title}</p>
        <h3>{value}</h3>
        <span>{description}</span>
      </div>
    </div>
  );
}