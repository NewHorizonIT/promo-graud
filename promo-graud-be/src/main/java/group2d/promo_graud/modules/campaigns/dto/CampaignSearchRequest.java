package group2d.promo_graud.modules.campaigns.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

import group2d.promo_graud.modules.campaigns.CampaignStatus;

@Getter
@Setter
public class CampaignSearchRequest {

    private String name;

    private CampaignStatus status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private int page = 1;

    private int size = 10;

    private String sortBy = "id";

    private String sortDir = "desc";
}
