export default function UserTypeBadge({ name }) {
  if (!name) return <span className="text-text-muted">—</span>;

  return (
    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold border bg-primary/10 text-primary border-primary/20">
      {name}
    </span>
  );
}
