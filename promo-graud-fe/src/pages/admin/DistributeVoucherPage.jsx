import { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import { useUsers } from '../../features/admin/users/hooks/useUsers';
import { useTypeUser } from '../../features/admin/users/hooks/useTypeOfUser';
// GIẢ ĐỊNH: voucherService đã có getJobStatus(jobId) (dùng cho luồng tạo voucher UNIQUE).
// Nếu tên hàm khác thì sửa lại import này.
import { getCards, getJobStatus } from '../../features/admin/vouchers/services/voucherService';
import { distributeVoucher } from '../../features/admin/distribution/services/distributionService';
import UserSelectionTable from '../../features/admin/distribution/components/UserSelectionTable';
import Toast from '../../components/Toast';
import VoucherCardList from '../../features/admin/vouchers/components/VoucherCardList';

const PAGE_SIZE = 10;
const POLL_INTERVAL_MS = 2000;
const POLL_MAX_TRIES = 60; // ~2 phút
const JOB_FAILED_STATUSES = ['FAILED', 'STOPPED', 'ABANDONED'];

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

export default function DistributeVoucherPage() {
  const navigate = useNavigate();
  const [toast, setToast] = useState(null);
  const aliveRef = useRef(true);

  // ----- Bộ lọc & chọn người dùng -----
  const [page, setPage] = useState(1);
  const [userTypeId, setUserTypeId] = useState('');

  // isSelectAll = true  -> chọn tất cả user của bộ lọc, trừ excludedUserIds
  // isSelectAll = false -> chỉ chọn selectedUserIds
  const [isSelectAll, setIsSelectAll] = useState(false);
  const [selectedUserIds, setSelectedUserIds] = useState([]);
  const [excludedUserIds, setExcludedUserIds] = useState([]);

  // ----- Card chiến dịch -----
  const [cards, setCards] = useState([]);
  const [selectedCard, setSelectedCard] = useState(null);
  const [isLoadingCards, setIsLoadingCards] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { data: types = [] } = useTypeUser();
  const { data: usersData, isLoading: isLoadingUsers } = useUsers({
    page,
    size: PAGE_SIZE,
    ...(userTypeId ? { userTypeId } : {}),
  });

  const users = usersData?.data ?? [];
  const totalPages = usersData?.totalPages ?? 1;
  const totalItems = usersData?.totalItems ?? 0;

  // Hạng lấy từ API (id + tên enum) thay vì hard-code
  const typeTabs = [
    { id: '', label: 'Tất cả' },
    ...types.map((t) => ({ id: t.id, label: t.type })),
  ];
  const selectedType = types.find((t) => String(t.id) === String(userTypeId));

  const selectedCount = isSelectAll
    ? Math.max(totalItems - excludedUserIds.length, 0)
    : selectedUserIds.length;

  useEffect(() => {
    aliveRef.current = true;
    return () => {
      aliveRef.current = false;
    };
  }, []);

  const fetchCards = useCallback(async () => {
    setIsLoadingCards(true);
    try {
      const data = await getCards();
      setCards(data ?? []);
    } catch {
      setToast({ type: 'error', message: 'Không thể tải danh sách chiến dịch' });
    } finally {
      setIsLoadingCards(false);
    }
  }, []);

  useEffect(() => {
    fetchCards();
  }, [fetchCards]);

  // ----- Handlers chọn người dùng -----
  const resetSelection = (selectAll) => {
    setIsSelectAll(selectAll);
    setSelectedUserIds([]);
    setExcludedUserIds([]);
  };

  // Đổi hạng -> tự chọn tất cả user của hạng đó
  const handleChangeType = (id) => {
    setUserTypeId(id);
    setPage(1);
    resetSelection(false);
  };

  // Tick header: đang chọn đủ -> bỏ hết, còn lại -> chọn tất cả
  const handleToggleAll = () => {
    const allSelected = totalItems > 0 && selectedCount === totalItems;
    resetSelection(!allSelected);
  };

  const handleToggleUser = (id) => {
    const toggle = (list) => (list.includes(id) ? list.filter((x) => x !== id) : [...list, id]);
    if (isSelectAll) setExcludedUserIds(toggle);
    else setSelectedUserIds(toggle);
  };

  // ----- Theo dõi job chạy ngầm -----
  const pollJob = useCallback(
    async (jobId) => {
      for (let i = 0; i < POLL_MAX_TRIES; i++) {
        await sleep(POLL_INTERVAL_MS);
        if (!aliveRef.current) return;
        try {
          const res = await getJobStatus(jobId);
          const job = res?.result ?? res;
          if (job?.status === 'COMPLETED') {
            const done = job.processed ?? 0;
            const want = job.requested ?? 0;
            setToast({
              type: done < want ? 'warning' : 'success',
              message: `Phân phối xong: ${done}/${want} người nhận.`,
              duration: 5000,
            });
            fetchCards();
            return;
          }
          if (JOB_FAILED_STATUSES.includes(job?.status)) {
            setToast({
              type: 'error',
              message: `Phân phối thất bại (đã xử lý ${job.processed ?? 0}/${job.requested ?? 0}). Xem log backend để biết lý do.`,
              duration: 6000,
            });
            fetchCards();
            return;
          }
        } catch {
          // lỗi mạng tạm thời -> thử lại ở vòng sau
        }
      }
      setToast({
        type: 'warning',
        message: 'Job vẫn đang chạy, hãy xem kết quả ở trang Lịch sử phân phối.',
        duration: 5000,
      });
    },
    [fetchCards],
  );

  // ----- Submit -----
  const handleSubmit = async () => {
    if (selectedCount === 0) {
      return setToast({ type: 'warning', message: 'Vui lòng chọn ít nhất 1 người dùng' });
    }
    if (!selectedCard) {
      return setToast({ type: 'warning', message: 'Vui lòng chọn chiến dịch (Rule) phân phối' });
    }

    setIsSubmitting(true);
    try {
      const payload = {
        ruleId: selectedCard.ruleId,
        distributionChannel: selectedCard.channel,
        ...(isSelectAll
          ? {
              // BE tự lấy user theo hạng (null = tất cả user) rồi trừ excludedUserIds
              selectAll: true,
              targetUserType: selectedType?.type ?? null, // tên enum, KHÔNG gửi số
              userIds: [],
              excludedUserIds,
            }
          : {
              selectAll: false,
              targetUserType: null,
              userIds: selectedUserIds,
              excludedUserIds: [],
            }),
      };

      const res = await distributeVoucher(payload);
      const jobId = res?.result;

      setToast({ type: 'info', message: 'Đã khởi chạy phân phối, đang xử lý...' });
      resetSelection(false);
      setSelectedCard(null);

      if (jobId != null) pollJob(jobId);
      else fetchCards();
    } catch (error) {
      const errorMsg = error?.response?.data?.message || 'Phân phối thất bại!';
      setToast({ type: 'error', message: errorMsg });
    } finally {
      setIsSubmitting(false);
    }
  };
  const handleSelectCard = (card) => {
    setSelectedCard(card);
    if (!card?.targetType) return;

    if (card.targetType === 'ALL') {
      if (userTypeId !== '') {
        handleChangeType('');
      }
      return;
    }

    const matchedTab = types.find((t) => t.type === card.targetType);
    if (matchedTab && String(matchedTab.id) !== String(userTypeId)) {
      handleChangeType(matchedTab.id);
    }
  };
  return (
    <div className="space-y-6">
      <Toast toast={toast} onClose={() => setToast(null)} />
      <div className="flex items-center gap-4">
        <button
          onClick={() => navigate(-1)}
          className="p-2 bg-surface border border-border rounded-xl text-text-muted hover:text-text-primary hover:bg-surface-muted transition-all cursor-pointer"
        >
          <ArrowLeft size={18} />
        </button>
        <div>
          <h1 className="text-xl font-bold text-text-primary">Phân phối Voucher</h1>
          <p className="text-sm text-text-muted">
            Chọn danh sách nhận và chiến dịch để kích hoạt phân phối.
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 items-stretch">
        <UserSelectionTable
          users={users}
          totalPages={totalPages}
          totalItems={totalItems}
          isLoading={isLoadingUsers}
          page={page}
          setPage={setPage}
          typeTabs={typeTabs}
          userTypeId={userTypeId}
          onChangeType={handleChangeType}
          isSelectAll={isSelectAll}
          selectedUserIds={selectedUserIds}
          excludedUserIds={excludedUserIds}
          selectedCount={selectedCount}
          onToggleAll={handleToggleAll}
          onToggleUser={handleToggleUser}
        />

        <VoucherCardList
          cards={cards}
          isLoadingCards={isLoadingCards}
          selectedCard={selectedCard}
          setSelectedCard={handleSelectCard}
          handleSubmit={handleSubmit}
          isSubmitting={isSubmitting}
          selectedUserCount={selectedCount}
        />
      </div>
    </div>
  );
}
