package group2d.promo_graud.modules.campaigns.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;
import lombok.experimental.FieldDefaults;

import group2d.promo_graud.modules.campaigns.CampaignStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CampaignResponse {
    Integer id;
    String name;
    LocalDateTime startTime;
    LocalDateTime endTime;
    BigDecimal promotionBudget;
    CampaignStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    Boolean isDeleted;
}
