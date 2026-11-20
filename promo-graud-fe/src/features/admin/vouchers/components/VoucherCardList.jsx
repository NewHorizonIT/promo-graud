import { Ticket, CheckCircle2, Send } from 'lucide-react';

export default function VoucherCardList({
  cards,
  isLoadingCards,
  selectedCard,
  setSelectedCard,
  handleSubmit,
  isSubmitting,
  selectedUserCount,
}) {
  const getStatusBadgeStyle = (status) => {
    if (status === 'ACTIVE') {
      return 'bg-emerald-500/10 text-emerald-600 border border-emerald-500/20';
    }
    if (status === 'INACTIVE') {
      return 'bg-red-500/10 text-red-600 border border-red-500/20';
    }
    return 'bg-slate-500/10 text-slate-600 border border-slate-500/20';
  };
  return (
    <div className="flex flex-col gap-4 h-full">
      <div className="bg-surface border border-border p-5 rounded-2xl flex-1 flex flex-col min-h-[400px]">
        <div className="flex items-center gap-2 text-text-primary font-semibold mb-1">
          <Ticket size={18} className="text-primary" />
          <h3>Thông tin phân phối</h3>
        </div>
        <p className="text-xs text-text-muted mb-4">Click chọn 1 thẻ để áp dụng Rule phân phối.</p>

        <div className="space-y-3 overflow-y-auto flex-1 max-h-[500px] pr-1.5 custom-scrollbar">
          {isLoadingCards ? (
            <div className="text-center text-sm text-text-muted py-8">Đang tải chiến dịch...</div>
          ) : cards.length === 0 ? (
            <div className="text-center text-sm text-text-muted py-8">Chưa có Rule active nào.</div>
          ) : (
            cards.map((card) => {
              const isSelected =
                selectedCard?.ruleId === card.ruleId && selectedCard?.channel === card.channel;

              return (
                <div
                  key={`${card.ruleId}-${card.channel}-${card.typeVoucher}`}
                  onClick={() => setSelectedCard(card)}
                  className={`p-4 rounded-xl border-2 transition-all cursor-pointer relative overflow-hidden ${
                    isSelected
                      ? 'border-primary bg-primary/5 shadow-level-1'
                      : 'border-border bg-surface hover:border-border-hover'
                  }`}
                >
                  {isSelected && (
                    <div className="absolute top-3 right-3 text-primary">
                      <CheckCircle2 size={20} />
                    </div>
                  )}

                  <h4 className="text-sm font-bold text-text-primary pr-6 line-clamp-1">
                    {card.campaignName || card.ruleName}
                  </h4>

                  <div className="grid grid-cols-2 gap-2 mt-3 text-xs">
                    <div>
                      <p className="text-text-muted">Rule ID</p>
                      <p className="font-semibold text-text-primary">{card.ruleId}</p>
                    </div>
                    <div>
                      <p className="text-text-muted">Loại người áp dụng</p>
                      <p className="font-semibold text-primary">
                        {card.targetType || 'Không xác định'}
                      </p>
                    </div>
                    <div className="flex flex-col items-start">
                      <p className="text-text-muted">Rule Status</p>
                      <span
                        className={`mt-0.5 px-2 py-0.5 rounded text-[10px] font-bold uppercase ${getStatusBadgeStyle(
                          card.ruleStatus,
                        )}`}
                      >
                        {card.ruleStatus}
                      </span>
                    </div>
                    <div>
                      <p className="text-text-muted">Tổng Voucher</p>
                      <p className="font-semibold text-primary">
                        {card.totalUniqueVouchers?.toLocaleString() || 0}
                      </p>
                    </div>
                    <div>
                      <p className="text-text-muted">Tên rule</p>
                      <p className="font-medium text-text-secondary">{card.ruleName}</p>
                    </div>
                    <div className="flex flex-col items-start">
                      <p className="text-text-muted">Kênh phân phối</p>
                      <span
                        className={`mt-0.5 px-2 py-0.5 rounded text-[10px] font-bold uppercase ${
                          card.channel === 'WEBHOOK'
                            ? 'bg-emerald-500/10 text-emerald-600 border border-emerald-500/20'
                            : 'bg-blue-500/10 text-blue-600 border border-blue-500/20'
                        }`}
                      >
                        {card.channel}
                      </span>
                    </div>
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>

      <button
        type="button"
        onClick={handleSubmit}
        disabled={isSubmitting || selectedCard?.ruleStatus === 'INACTIVE'}
        className="w-full flex items-center justify-center gap-2 py-3.5 bg-primary hover:bg-primary-hover text-white rounded-2xl text-sm font-bold transition-all shadow-glow disabled:opacity-60 cursor-pointer"
      >
        {isSubmitting ? (
          'Đang xử lý...'
        ) : (
          <>
            <Send size={18} />
            Xác nhận phân phối {selectedUserCount > 0 ? `(${selectedUserCount})` : ''}
          </>
        )}
      </button>
    </div>
  );
}
