package group2d.promo_graud.modules.rules.engine.strategy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import group2d.promo_graud.modules.orders.entity.Order;
import group2d.promo_graud.modules.rules.RuleCampaign;
import group2d.promo_graud.modules.rules.enums.RuleCampaignErrorCode;
import group2d.promo_graud.modules.rules.enums.TypeOfRule;
import group2d.promo_graud.shared.exception.AppException;

@Component
public class TieredStrategy implements RuleStrategy {
    // Tính tiền giảm giá theo bậc, đơn càng lớn thì giảm càng nhiều
    // lấy danh sách tiers từ payload (rỗng thì giảm 0) -> lọc các bậc có min_order_value <= tổng
    // đơn -> lấy discount_value lớn nhất trong các bậc đạt
    @Override
    @SuppressWarnings("unchecked")
    public BigDecimal calculateDiscount(Order order, RuleCampaign rule) {
        List<Map<String, Object>> tiers =
                (List<Map<String, Object>>) rule.getPayload().get("tiers");

        if (tiers == null || tiers.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return tiers.stream()
                .filter(
                        tier -> {
                            BigDecimal minOrder = getRequiredBigDecimal(tier, "min_order_value");
                            return order.getTotalPrice().compareTo(minOrder) >= 0;
                        })
                .map(tier -> getRequiredBigDecimal(tier, "discount_value"))
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public TypeOfRule getRuleType() {
        return TypeOfRule.TIERED;
    }

    private BigDecimal getRequiredBigDecimal(Map<String, Object> data, String key) {
        Object value = data.get(key);

        if (value == null) {
            throw new AppException(RuleCampaignErrorCode.INVALID_PAYLOAD);
        }

        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            throw new AppException(RuleCampaignErrorCode.INVALID_PAYLOAD);
        }
    }
}
