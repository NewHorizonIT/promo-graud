package group2d.promo_graud.modules.rules.engine;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.orders.entity.Order;
import group2d.promo_graud.modules.rules.RuleCampaign;
import group2d.promo_graud.modules.rules.engine.strategy.RuleStrategy;
import group2d.promo_graud.modules.rules.enums.RuleCampaignErrorCode;
import group2d.promo_graud.modules.user.entity.User;
import group2d.promo_graud.shared.exception.AppException;

@Service
@RequiredArgsConstructor
public class RuleEngine {

    private final StrategyFactory strategyFactory;
    private final EligibilityChecker eligibilityChecker;

    // Áp dụng rule khuyến mãi lên đơn hàng và trả về số tiền giảm (điểm khởi đầu)
    // kiểm tra đơn/user có đủ điều kiện không (không đủ thì ném RULE_NOT_ELIGIBLE) -> chọn strategy
    // theo loại rule -> gọi calculateDiscount
    public BigDecimal applyRules(Order order, RuleCampaign rule, User user) {
        if (!eligibilityChecker.isEligible(order, rule, user)) {
            throw new AppException(RuleCampaignErrorCode.RULE_NOT_ELIGIBLE);
        }

        RuleStrategy strategy = strategyFactory.resolveStrategy(rule.getTypeOfRule());
        return strategy.calculateDiscount(order, rule);
    }
}
