package group2d.promo_graud.modules.campaigns.dto;

import jakarta.validation.constraints.NotNull;

import lombok.*;
import lombok.experimental.FieldDefaults;

import group2d.promo_graud.modules.campaigns.CampaignStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CampaignUpdateStatusRequest {
    @NotNull(message = "Trạng thái không được để trống")
    private CampaignStatus status;
}
