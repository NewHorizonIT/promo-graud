package group2d.promo_graud.modules.voucher.dto.responses;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DistributionJobStatus {
    Long jobId;
    String status; // STARTING, STARTED, COMPLETED, FAILED, STOPPED
    int targetUserCount;
}
