package group2d.promo_graud.modules.voucher.component.worker;

import java.util.List;

import group2d.promo_graud.modules.voucher.component.distribute.DistributionPayload;

public record VoucherDistributionEvent(List<DistributionPayload> payloads) {}
