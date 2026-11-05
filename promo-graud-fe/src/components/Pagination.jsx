import getPages from '../utils/getpages';

export default function Pagination({ meta, onPageChange }) {
  if (!meta || meta.last_page <= 1) return null;

  const { current_page, last_page } = meta;

  const pages = getPages(current_page, last_page);

  const baseBtn =
    'min-w-9 h-9 px-3 rounded-xl border text-sm font-semibold transition-all duration-200 cursor-pointer';
  const normalBtn =
    'bg-surface border-border text-text-secondary hover:bg-primary-light hover:border-primary hover:text-primary';
  const activeBtn = 'bg-primary border-primary text-white shadow-glow';
  const disabledBtn =
    'bg-surface-muted border-border text-text-muted opacity-60 cursor-not-allowed';

  return (
    <div className="flex flex-wrap justify-center items-center gap-2 pt-2">
      {/* Prev */}
      <button
        type="button"
        disabled={current_page === 1}
        onClick={() => onPageChange(current_page - 1)}
        className={`${baseBtn} ${current_page === 1 ? disabledBtn : normalBtn}`}
      >
        Trước
      </button>

      {pages.map((page, i) =>
        page === '...' ? (
          <span key={`ellipsis-${i}`} className="px-1 text-text-muted select-none">
            ...
          </span>
        ) : (
          <button
            key={page}
            type="button"
            aria-current={page === current_page ? 'page' : undefined}
            onClick={() => onPageChange(page)}
            className={`${baseBtn} ${page === current_page ? activeBtn : normalBtn}`}
          >
            {page}
          </button>
        ),
      )}

      {/* Next */}
      <button
        type="button"
        disabled={current_page === last_page}
        onClick={() => onPageChange(current_page + 1)}
        className={`${baseBtn} ${current_page === last_page ? disabledBtn : normalBtn}`}
      >
        Sau
      </button>
    </div>
  );
}
