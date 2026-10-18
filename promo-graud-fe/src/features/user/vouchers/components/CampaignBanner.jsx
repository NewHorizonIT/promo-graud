import { useRef } from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import CampaignCard from './CampaignCard';
import { useCampaigns, useAllCampaignVouchers } from '../hooks/useVouchers';

function CampaignCardSkeleton() {
  return (
    <div className="flex-shrink-0 w-72 bg-surface border border-border rounded-2xl overflow-hidden">
      <div className="p-4 bg-surface-muted/50 border-b border-border">
        <div className="h-4 bg-surface-muted animate-pulse rounded w-3/4 mb-2" />
        <div className="h-3 bg-surface-muted animate-pulse rounded w-1/2" />
      </div>
      <div className="p-3 grid grid-cols-2 gap-2">
        {[1, 2, 3, 4].map((i) => (
          <div key={i} className="h-16 bg-surface-muted animate-pulse rounded-lg" />
        ))}
      </div>
    </div>
  );
}

export default function CampaignBanner() {
  const { data: campaignData, isLoading: loadingCampaigns } = useCampaigns();
  const campaigns = campaignData?.content ?? [];

  const campaignIds = campaigns.map((c) => c.id);
  const { data: allVouchers, isLoading: loadingVouchers } = useAllCampaignVouchers(campaignIds);

  const loading = loadingCampaigns || loadingVouchers;
  const scrollRef = useRef(null);

  const scroll = (dir) => {
    if (!scrollRef.current) return;
    scrollRef.current.scrollBy({ left: dir * 300, behavior: 'smooth' });
  };

  if (!loading && campaigns.length === 0) return null;

  return (
    <section className="w-full">
      <div className="flex items-center justify-between mb-4">
        <div>
          <h2 className="text-2xl font-bold text-text-primary">Chiến dịch đang diễn ra</h2>
          <p className="text-sm text-text-muted mt-0.5">Lưu mã ngay — số lượng có hạn</p>
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => scroll(-1)}
            className="p-2 rounded-full border border-border bg-surface hover:bg-surface-muted transition-all cursor-pointer"
            aria-label="Cuộn trái"
          >
            <ChevronLeft size={18} />
          </button>
          <button
            type="button"
            onClick={() => scroll(1)}
            className="p-2 rounded-full border border-border bg-surface hover:bg-surface-muted transition-all cursor-pointer"
            aria-label="Cuộn phải"
          >
            <ChevronRight size={18} />
          </button>
        </div>
      </div>

      <div ref={scrollRef} className="flex gap-4 overflow-x-auto pb-2 scroll-smooth scrollbar-hide">
        {loading
          ? [1, 2, 3].map((i) => <CampaignCardSkeleton key={i} />)
          : campaigns.map((campaign, idx) => (
              <CampaignCard
                key={campaign.id}
                campaign={campaign}
                vouchers={allVouchers?.[idx]?.data ?? []}
              />
            ))}
      </div>
    </section>
  );
}
