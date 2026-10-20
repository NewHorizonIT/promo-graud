package group2d.promo_graud.modules.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TimelinePointResponse {
    private LocalDateTime timestamp;
    private long redeemed;
    private BigDecimal revenue;
}
