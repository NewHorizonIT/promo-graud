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

    // Khởi tạo factory, gom tất cả strategy do Spring inject vào một map theo loại rule
    // nhận danh sách RuleStrategy -> lấy getRuleType() làm key -> gom thành Map<TypeOfRule,
    // RuleStrategy>
    public StrategyFactory(List<RuleStrategy> strategyList) {
        this.strategies =
                strategyList.stream().collect(Collectors.toMap(s -> s.getRuleType(), s -> s));
    }

    // Lấy strategy tương ứng với loại rule
    // kiểm tra map có chứa ruleType không (không có thì ném UNSUPPORTED_RULE_TYPE) -> trả về
    // strategy
    public RuleStrategy resolveStrategy(TypeOfRule ruleType) {
        if (!this.strategies.containsKey(ruleType)) {
            throw new AppException(RuleCampaignErrorCode.UNSUPPORTED_RULE_TYPE);
        }
        return this.strategies.get(ruleType);
    }
}
