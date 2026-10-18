import axiosInstance from '../../../../lib/axiosInstance';

export async function getCampaigns() {
  const { data } = await axiosInstance.get('/campaigns', {
    params: { status: 'ACTIVE' },
  });
  return data.result;
}

export async function getVouchersByCampaign(campaignId, size = 4) {
  const { data } = await axiosInstance.get('/vouchers', {
    params: { type: 'GENERIC', status: 'ACTIVE', campaignId, page: 1, pageSize: size },
  });
  return data;
}

export async function getVouchers({ campaignId, ruleId, search, page = 1, pageSize = 16 } = {}) {
  const params = { type: 'GENERIC', status: 'ACTIVE', page, pageSize };
  if (campaignId) params.campaignId = campaignId;
  if (ruleId) params.ruleId = ruleId;
  if (search) params.search = search;

  const { data } = await axiosInstance.get('/vouchers', { params });
  return data;
}

export async function claimVoucher(voucherId) {
  const { data } = await axiosInstance.post('/user-vouchers', { voucherId });
  return data.result;
}

export async function getMyVouchers({ page = 0, limit = 10 } = {}) {
  const { data } = await axiosInstance.get('/user-vouchers/me', { params: { page, limit } });
  return data.result;
}

// rules endpoint trả thẳng PaginatedResponse, không bọc trong ApiResponse
export async function getRules({ pageSize = 100 } = {}) {
  const { data } = await axiosInstance.get('/rules', {
    params: { status: 'ACTIVE', pageSize },
  });
  return data;
}
