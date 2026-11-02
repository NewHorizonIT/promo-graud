import { useState } from 'react';
import { Plus, Pencil, Trash2, Loader2, Lock } from 'lucide-react';
import RuleForm from './RuleForm';

const RULE_TYPE_LABELS = {
  FIXED_AMOUNT: 'Giảm trực tiếp',
  PERCENTAGE: 'Giảm %',
  FLASH_SALE: 'Flash Sale',
  BUY_X_GET_Y: 'Mua X tặng Y',
  TIERED: 'Tiered',
};

const RULE_STATUS_STYLES = {
  ACTIVE: 'bg-success-bg text-success-text',
  INACTIVE: 'bg-surface-muted text-text-muted',
};

const PAYLOAD_TYPES = new Set(['TIERED', 'BUY_X_GET_Y']);

function ruleValueDisplay(rule) {
  if (PAYLOAD_TYPES.has(rule.typeOfRule)) return '(JSON)';
  if (rule.value == null) return '—';
  const suffix = rule.typeOfRule === 'FIXED_AMOUNT' ? 'đ' : '%';
  return `${Number(rule.value).toLocaleString('vi-VN')}${suffix}`;
}

function formatDateShort(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

function toFormState(rule) {
  return {
    name: rule.name ?? '',
    type_of_rule: rule.typeOfRule ?? 'PERCENTAGE',
    value: String(rule.value ?? ''),
    max_discount_value: String(rule.maxDiscountValue ?? ''),
    min_order_value: String(rule.minOrderValue ?? ''),
    type_of_user: rule.typeOfUser ?? 'ALL',
    start_time: rule.startTime?.slice(0, 16) ?? '',
    end_time: rule.endTime?.slice(0, 16) ?? '',
    payload: rule.payload ? JSON.stringify(rule.payload, null, 2) : '',
  };
}

export default function RuleList({
  rules = [],
  campaignStatus,
  onAdd,
  onEdit,
  onDelete,
  addLoading = false,
  editLoading = false,
  deletingId = null,
}) {
  const [showForm, setShowForm] = useState(false);
  const [editTarget, setEditTarget] = useState(null);

  const isEnded = campaignStatus === 'ENDED';

  // Đóng form chỉ khi mutation thành công (onAdd phải là async)
  const handleAdd = async (data) => {
    try {
      await onAdd?.(data);
      setShowForm(false);
    } catch {
      // form ở lại, parent đã hiển thị lỗi
    }
  };

  const handleEdit = async (data) => {
    try {
      await onEdit?.(editTarget.id, data);
      setEditTarget(null);
    } catch {
      // form ở lại
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-base font-semibold text-text-primary">Rules ({rules.length})</h3>
        {!isEnded && (
          <button
            type="button"
            onClick={() => setShowForm(true)}
            disabled={showForm}
            className="flex items-center gap-1.5 px-3 py-1.5 bg-primary hover:bg-primary-hover text-white rounded-xl text-xs font-semibold transition-all hover:shadow-glow disabled:opacity-50 cursor-pointer"
          >
            <Plus size={14} /> Thêm Rule
          </button>
        )}
      </div>

      {/* Add form */}
      {showForm && (
        <div className="bg-surface-muted border border-border rounded-xl p-4">
          <p className="text-sm font-semibold text-text-primary mb-3">Tạo Rule mới</p>
          <RuleForm
            onSubmit={handleAdd}
            onCancel={() => setShowForm(false)}
            loading={addLoading}
          />
        </div>
      )}

      {rules.length === 0 && !showForm && (
        <p className="text-sm text-text-muted py-4 text-center">
          Chưa có rule nào cho campaign này.
        </p>
      )}

      <div className="space-y-2">
        {rules.map((rule) => (
          <div key={rule.id}>
            {editTarget?.id === rule.id ? (
              <div className="bg-surface-muted border border-border rounded-xl p-4">
                <p className="text-sm font-semibold text-text-primary mb-3">Sửa Rule</p>
                {/* key={rule.id} ensures form remounts fresh when target changes — no useEffect needed */}
                <RuleForm
                  key={rule.id}
                  initial={toFormState(rule)}
                  isEdit
                  onSubmit={handleEdit}
                  onCancel={() => setEditTarget(null)}
                  loading={editLoading}
                />
              </div>
            ) : (
              <div className="flex items-center justify-between px-4 py-3 bg-surface border border-border rounded-xl hover:shadow-level-1 transition-shadow">
                <div className="space-y-0.5 min-w-0">
                  <p className="text-sm font-semibold text-text-primary truncate">{rule.name}</p>
                  <div className="flex items-center gap-2 flex-wrap">
                    <span className="text-xs text-primary bg-primary-light px-2 py-0.5 rounded-sm font-semibold">
                      {RULE_TYPE_LABELS[rule.typeOfRule] ?? rule.typeOfRule}
                    </span>
                    {rule.status && (
                      <span
                        className={`text-xs px-2 py-0.5 rounded-sm font-semibold ${RULE_STATUS_STYLES[rule.status] ?? 'bg-surface-muted text-text-muted'}`}
                      >
                        {rule.status}
                      </span>
                    )}
                    <span className="text-xs text-text-muted">
                      Giá trị: {ruleValueDisplay(rule)}
                    </span>
                    {rule.minOrderValue && (
                      <span className="text-xs text-text-muted">
                        Tối thiểu: {Number(rule.minOrderValue).toLocaleString('vi-VN')}đ
                      </span>
                    )}
                    <span className="text-xs text-text-muted">
                      {formatDateShort(rule.startTime)} → {formatDateShort(rule.endTime)}
                    </span>
                  </div>
                </div>
                <div className="flex items-center gap-1 ml-3 shrink-0">
                  {isEnded ? (
                    <span
                      className="p-1.5 rounded-lg text-text-muted/40 cursor-not-allowed"
                      title="Campaign đã kết thúc — không thể chỉnh sửa"
                    >
                      <Lock size={14} />
                    </span>
                  ) : (
                    <>
                      <button
                        type="button"
                        onClick={() => setEditTarget(rule)}
                        className="p-1.5 rounded-lg text-text-muted hover:text-text-primary hover:bg-surface-muted transition-colors cursor-pointer"
                        title="Sửa rule"
                      >
                        <Pencil size={14} />
                      </button>
                      <button
                        type="button"
                        onClick={() => onDelete?.(rule.id)}
                        disabled={deletingId === rule.id}
                        className="p-1.5 rounded-lg text-text-muted hover:text-error-text hover:bg-error-bg transition-colors cursor-pointer disabled:opacity-50"
                        title="Xóa rule"
                      >
                        {deletingId === rule.id ? (
                          <Loader2 size={14} className="animate-spin" />
                        ) : (
                          <Trash2 size={14} />
                        )}
                      </button>
                    </>
                  )}
                </div>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
