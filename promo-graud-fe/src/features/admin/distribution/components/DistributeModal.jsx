import { useState } from "react";
import { X, Send } from "lucide-react";

const EMPTY = { channel: "email", recipients: "", scheduled_at: "" };

export default function DistributeModal({ voucher, onSubmit, onClose, loading = false }) {
  const [form, setForm] = useState({ ...EMPTY, channel: voucher?.distribution_channel ?? "email" });

  const set = (field, value) => setForm((prev) => ({ ...prev, [field]: value }));

  const handleSubmit = (e) => {
    e.preventDefault();
    const recipients = form.recipients
      .split(/[\n,]/)
      .map((r) => r.trim())
      .filter(Boolean);
    onSubmit?.({
      channel: form.channel,
      recipients,
      scheduled_at: form.scheduled_at || undefined,
    });
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center px-4">
      <div className="fixed inset-0 bg-black/40 backdrop-blur-xs" onClick={onClose} />
      <div className="relative w-full max-w-lg bg-surface rounded-2xl shadow-level-2 p-6 z-10">
        {/* Header */}
        <div className="flex items-center justify-between mb-5">
          <div>
            <h2 className="text-lg font-bold text-text-primary">Phân phối Voucher</h2>
            {voucher && (
              <p className="text-xs text-text-muted mt-0.5 font-mono">Mã: {voucher.code}</p>
            )}
          </div>
          <button type="button" onClick={onClose}
            className="p-1.5 rounded-lg text-text-muted hover:text-text-primary hover:bg-surface-muted transition-colors cursor-pointer">
            <X size={18} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Channel */}
          <div>
            <label className="block text-sm font-medium text-text-secondary mb-1.5">Kênh phân phối</label>
            <div className="flex gap-3">
              {["email", "webhook"].map((ch) => (
                <label key={ch} className={`flex-1 flex items-center gap-2 px-4 py-2.5 border rounded-xl cursor-pointer transition-all ${
                  form.channel === ch
                    ? "border-primary bg-primary-light text-primary"
                    : "border-border bg-surface-muted text-text-secondary hover:bg-surface-muted"
                }`}>
                  <input type="radio" name="dist_channel" value={ch} checked={form.channel === ch}
                    onChange={() => set("channel", ch)} className="hidden" />
                  <span className="text-sm font-semibold capitalize">{ch}</span>
                </label>
              ))}
            </div>
          </div>

          {/* Recipients */}
          <div>
            <label className="block text-sm font-medium text-text-secondary mb-1.5">
              {form.channel === "email" ? "Danh sách Email" : "Webhook URL"}
            </label>
            <textarea
              rows={4}
              value={form.recipients}
              onChange={(e) => set("recipients", e.target.value)}
              placeholder={form.channel === "email"
                ? "user1@example.com\nuser2@example.com"
                : "https://your-webhook.com/endpoint"}
              className="w-full px-4 py-3 rounded-xl border border-border bg-surface-muted text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all resize-none"
            />
            <p className="text-[11px] text-text-muted mt-1">
              {form.channel === "email" ? "Mỗi email trên một dòng hoặc cách nhau bằng dấu phẩy" : "Một URL duy nhất"}
            </p>
          </div>

          {/* Schedule */}
          <div>
            <label className="block text-sm font-medium text-text-secondary mb-1.5">
              Lịch gửi <span className="text-text-muted text-xs font-normal">(để trống = gửi ngay)</span>
            </label>
            <input type="datetime-local" value={form.scheduled_at} onChange={(e) => set("scheduled_at", e.target.value)}
              className="w-full h-11 px-4 rounded-xl border border-border bg-surface-muted text-sm text-text-primary focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition-all" />
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <button type="button" onClick={onClose}
              className="px-4 py-2.5 border border-border bg-surface-muted hover:bg-border text-sm font-semibold text-text-primary rounded-xl transition-colors cursor-pointer">
              Hủy
            </button>
            <button type="submit" disabled={loading}
              className="flex items-center gap-2 px-5 py-2.5 bg-primary hover:bg-primary-hover text-white rounded-xl text-sm font-semibold transition-all hover:shadow-glow disabled:opacity-60 cursor-pointer">
              <Send size={15} />
              {loading ? "Đang gửi..." : "Phân phối"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
