package group2d.promo_graud.modules.campaigns.dto;

import group2d.promo_graud.modules.campaigns.CampaignStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CampaignUpdateStatusRequest {
  @NotNull(message = "Trạng thái không được để trống")
  private CampaignStatus status;
}
