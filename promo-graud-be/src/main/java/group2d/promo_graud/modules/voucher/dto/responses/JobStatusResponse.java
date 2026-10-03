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
public class JobStatusResponse {
   Long jobId;
  String status;     // STARTING, STARTED, COMPLETED, FAILED, STOPPED
  long processed;      // số item đã ghi
  long requested;      // số yêu cầu
  String errorMessage;
}
