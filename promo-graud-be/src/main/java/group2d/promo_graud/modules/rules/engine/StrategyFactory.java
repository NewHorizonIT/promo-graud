package group2d.promo_graud.modules.rules.engine;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import group2d.promo_graud.modules.rules.engine.strategy.RuleStrategy;
import group2d.promo_graud.modules.rules.enums.RuleCampaignErrorCode;
import group2d.promo_graud.modules.rules.enums.TypeOfRule;
import group2d.promo_graud.shared.exception.AppException;

@Component
public class StrategyFactory {
    private final Map<TypeOfRule, RuleStrategy> strategies;

    public StrategyFactory(List<RuleStrategy> strategyList) {
        this.strategies =
                strategyList.stream().collect(Collectors.toMap(s -> s.getRuleType(), s -> s));
    }

    public RuleStrategy resolveStrategy(TypeOfRule ruleType) {
        if (!this.strategies.containsKey(ruleType)) {
            throw new AppException(RuleCampaignErrorCode.UNSUPPORTED_RULE_TYPE);
        }
        return this.strategies.get(ruleType);
    }
}
