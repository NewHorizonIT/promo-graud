export default function ProductSkeleton({ count = 8 }) {
  return (
    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-5">
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} className="bg-surface border border-border rounded-xl overflow-hidden animate-pulse">
          <div className="aspect-[4/5] bg-surface-muted" />
          <div className="p-4 space-y-2.5">
            <div className="h-3 bg-border rounded w-1/3" />
            <div className="h-4 bg-border rounded w-3/4" />
            <div className="h-4 bg-border rounded w-1/2" />
            <div className="h-9 bg-border rounded-lg mt-3" />
          </div>
        </div>
      ))}
    </div>
  );
}
