import { Users } from 'lucide-react';
import Pagination from '../../../../components/Pagination';

/**
 * Mô hình chọn:
 *  - isSelectAll = true  : chọn TẤT CẢ user của bộ lọc hiện tại, trừ excludedUserIds
 *  - isSelectAll = false : chỉ chọn các user trong selectedUserIds (giữ qua các trang)
 *
 * typeTabs: [{ id: '', label: 'Tất cả' }, { id: 1, label: 'NORMAL' }, ...] (lấy từ API)
 */
export default function UserSelectionTable({
  users,
  totalPages,
  totalItems,
  isLoading,
  page,
  setPage,
  typeTabs,
  userTypeId,
  onChangeType,
  isSelectAll,
  selectedUserIds,
  excludedUserIds,
  selectedCount,
  onToggleAll,
  onToggleUser,
}) {
  const isRowChecked = (id) =>
    isSelectAll ? !excludedUserIds.includes(id) : selectedUserIds.includes(id);

  const allChecked = totalItems > 0 && selectedCount === totalItems;
  const someChecked = selectedCount > 0 && !allChecked;

  return (
    <div className="lg:col-span-2 space-y-4 bg-surface border border-border p-5 rounded-2xl flex flex-col">
      <div className="flex items-center gap-2 text-text-primary font-semibold">
        <Users size={18} className="text-primary" />
        <h3>
          Chọn người nhận{' '}
          <span className="text-sm font-normal text-text-muted">({totalItems} users)</span>
        </h3>
      </div>

      <div className="flex flex-wrap items-center gap-2 border-b border-border pb-4">
        {typeTabs.map((t) => (
          <button
            key={t.id}
            type="button"
            onClick={() => onChangeType(t.id)}
            className={`px-3.5 py-1.5 rounded-xl text-xs font-semibold transition-all cursor-pointer ${
              String(userTypeId) === String(t.id)
                ? 'bg-primary text-white shadow-glow'
                : 'bg-surface-muted text-text-muted hover:text-text-primary border border-transparent hover:border-border'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      <div className="overflow-x-auto flex-1">
        <table className="w-full text-left text-sm whitespace-nowrap">
          <thead className="text-text-muted border-b border-border">
            <tr>
              <th className="py-3 px-2 font-medium w-10">
                <div className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    checked={allChecked}
                    disabled={totalItems === 0}
                    ref={(el) => {
                      if (el) el.indeterminate = someChecked;
                    }}
                    onChange={onToggleAll}
                    className="rounded border-border text-primary focus:ring-primary w-4 h-4 cursor-pointer disabled:cursor-not-allowed"
                  />
                  {allChecked && (
                    <span className="text-xs text-primary font-semibold whitespace-nowrap hidden sm:inline-block">
                      Tất cả ({totalItems})
                    </span>
                  )}
                  {isSelectAll && excludedUserIds.length > 0 && (
                    <span className="text-xs text-text-muted font-semibold whitespace-nowrap hidden sm:inline-block">
                      Bỏ {excludedUserIds.length} người
                    </span>
                  )}
                </div>
              </th>
              <th className="py-3 px-4 font-medium">ID</th>
              <th className="py-3 px-4 font-medium">Email</th>
              <th className="py-3 px-4 font-medium">Loại người dùng</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {isLoading ? (
              <tr>
                <td colSpan="4" className="py-10 text-center text-text-muted">
                  Đang tải...
                </td>
              </tr>
            ) : users.length === 0 ? (
              <tr>
                <td colSpan="4" className="py-10 text-center text-text-muted">
                  Không tìm thấy người dùng.
                </td>
              </tr>
            ) : (
              users.map((user) => (
                <tr key={user.id} className="hover:bg-surface-muted/50 transition-colors">
                  <td className="py-3 px-2">
                    <input
                      type="checkbox"
                      checked={isRowChecked(user.id)}
                      onChange={() => onToggleUser(user.id)}
                      className="rounded border-border text-primary focus:ring-primary w-4 h-4 cursor-pointer"
                    />
                  </td>
                  <td className="py-3 px-4 text-text-secondary">{user.id}</td>
                  <td className="py-3 px-4 font-medium text-text-primary">{user.email}</td>
                  <td className="py-3 px-4">
                    <span className="px-2.5 py-1 bg-surface-muted text-text-secondary text-xs rounded-lg font-semibold border border-border uppercase">
                      {user.typeOfUserType || 'NORMAL'}
                    </span>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      <div className="flex flex-col sm:flex-row items-center justify-between pt-4 border-t border-border gap-4">
        <span className="text-sm font-medium text-primary bg-primary/10 px-3 py-1.5 rounded-lg border border-primary/20">
          Đã chọn: {selectedCount} người
        </span>
        <Pagination
          meta={{ current_page: page, last_page: totalPages }}
          onPageChange={(newPage) => setPage(newPage)}
        />
      </div>
    </div>
  );
}
