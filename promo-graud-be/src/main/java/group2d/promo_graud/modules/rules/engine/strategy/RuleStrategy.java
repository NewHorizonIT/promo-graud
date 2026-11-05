package group2d.promo_graud.modules.rules.engine.strategy;

import java.math.BigDecimal;

import group2d.promo_graud.modules.orders.entity.Order;
import group2d.promo_graud.modules.rules.RuleCampaign;
import group2d.promo_graud.modules.rules.enums.TypeOfRule;

public interface RuleStrategy {
    BigDecimal calculateDiscount(Order order, RuleCampaign rule);

    TypeOfRule getRuleType();
}
