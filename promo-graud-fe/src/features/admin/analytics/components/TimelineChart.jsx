import {
  ComposedChart,
  Bar,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { formatShortCurrency } from '../../../../utils/format';

function formatTimestamp(ts, interval) {
  if (!ts) return '';
  const d = new Date(ts);
  if (interval === 'hour') {
    return d.toLocaleString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  }
  return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' });
}

function CustomTooltip({ active, payload, label }) {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-surface border border-border rounded-xl p-3 shadow-level-2 text-sm">
      <p className="font-semibold text-text-primary mb-1">{label}</p>
      {payload.map((p) => (
        <p key={p.dataKey} style={{ color: p.color }}>
          {p.name}: {p.dataKey === 'revenue' ? formatShortCurrency(p.value) : p.value}
        </p>
      ))}
    </div>
  );
}

export default function TimelineChart({ data = [], interval = 'day' }) {
  if (!data.length) {
    return (
      <div className="flex flex-col items-center justify-center py-12 text-center bg-surface-muted rounded-xl">
        <p className="text-sm font-semibold text-text-primary">Chưa có dữ liệu</p>
        <p className="text-xs text-text-muted mt-1">
          Chưa có lượt redeem nào trong khoảng thời gian này
        </p>
      </div>
    );
  }

  const chartData = data.map((d) => ({
    time: formatTimestamp(d.timestamp, interval),
    redeemed: d.redeemed,
    revenue: Number(d.revenue),
  }));

  return (
    <ResponsiveContainer width="100%" height={260}>
      <ComposedChart data={chartData} margin={{ top: 4, right: 16, left: 0, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="var(--color-border, #e5e7eb)" />
        <XAxis dataKey="time" tick={{ fontSize: 11 }} />
        <YAxis yAxisId="left" tick={{ fontSize: 11 }} allowDecimals={false} />
        <YAxis
          yAxisId="right"
          orientation="right"
          tick={{ fontSize: 11 }}
          tickFormatter={formatShortCurrency}
        />
        <Tooltip content={<CustomTooltip />} />
        <Legend wrapperStyle={{ fontSize: 12 }} />
        <Bar
          yAxisId="left"
          dataKey="redeemed"
          name="Lượt redeem"
          fill="#6366f1"
          radius={[4, 4, 0, 0]}
        />
        <Line
          yAxisId="right"
          type="monotone"
          dataKey="revenue"
          name="Doanh thu"
          stroke="#10b981"
          strokeWidth={2}
          dot={false}
        />
      </ComposedChart>
    </ResponsiveContainer>
  );
}
