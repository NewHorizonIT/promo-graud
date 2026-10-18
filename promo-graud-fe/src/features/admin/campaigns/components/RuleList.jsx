import { useState } from 'react';
import { Plus, Pencil, Trash2 } from 'lucide-react';
import RuleForm from './RuleForm';

const RULE_TYPE_LABELS = {
  giam_truc_tiep: 'Giảm trực tiếp',
  giam_phan_tram: 'Giảm %',
  mua_x_tang_y: 'Mua X tặng Y',
  tiered: 'Tiered',
};

export default function RuleList({ rules = [], onAdd, onEdit, onDelete }) {
  const [showForm, setShowForm] = useState(false);
  const [editTarget, setEditTarget] = useState(null);

  const handleAdd = (data) => {
    onAdd?.(data);
    setShowForm(false);
  };

  const handleEdit = (data) => {
    onEdit?.(editTarget.id, data);
    setEditTarget(null);
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-base font-semibold text-text-primary">Rules ({rules.length})</h3>
        <button
          type="button"
          onClick={() => setShowForm(true)}
          className="flex items-center gap-1.5 px-3 py-1.5 bg-primary hover:bg-primary-hover text-white rounded-xl text-xs font-semibold transition-all hover:shadow-glow cursor-pointer"
        >
          <Plus size={14} /> Thêm Rule
        </button>
      </div>

      {/* Add form */}
      {showForm && (
        <div className="bg-surface-muted border border-border rounded-xl p-4">
          <p className="text-sm font-semibold text-text-primary mb-3">Tạo Rule mới</p>
          <RuleForm onSubmit={handleAdd} onCancel={() => setShowForm(false)} />
        </div>
      )}

      {/* Rule rows */}
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
                <RuleForm
                  initial={rule}
                  onSubmit={handleEdit}
                  onCancel={() => setEditTarget(null)}
                />
              </div>
            ) : (
              <div className="flex items-center justify-between px-4 py-3 bg-surface border border-border rounded-xl hover:shadow-level-1 transition-shadow">
                <div className="space-y-0.5">
                  <p className="text-sm font-semibold text-text-primary">{rule.name}</p>
                  <div className="flex items-center gap-2">
                    <span className="text-xs text-primary bg-primary-light px-2 py-0.5 rounded-sm font-semibold">
                      {RULE_TYPE_LABELS[rule.type_of_rule] ?? rule.type_of_rule}
                    </span>
                    <span className="text-xs text-text-muted">Giá trị: {rule.value}</span>
                    {rule.min_order_value && (
                      <span className="text-xs text-text-muted">
                        Đơn tối thiểu: {rule.min_order_value.toLocaleString('vi-VN')}đ
                      </span>
                    )}
                  </div>
                </div>
                <div className="flex items-center gap-1">
                  <button
                    type="button"
                    onClick={() => setEditTarget(rule)}
                    className="p-1.5 rounded-lg text-text-muted hover:text-text-primary hover:bg-surface-muted transition-colors cursor-pointer"
                  >
                    <Pencil size={14} />
                  </button>
                  <button
                    type="button"
                    onClick={() => onDelete?.(rule.id)}
                    className="p-1.5 rounded-lg text-text-muted hover:text-error-text hover:bg-error-bg transition-colors cursor-pointer"
                  >
                    <Trash2 size={14} />
                  </button>
                </div>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
