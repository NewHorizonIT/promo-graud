package group2d.promo_graud.modules.campaigns.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CampaignRequest {
    @NotBlank(message = "Tên chiến dịch không được để trống")
    @Size(
            min = 5,
            max = 100,
            message = "Tên chiến dịch phải ít nhất 5 ký tự và không quá 100 ký tự")
    String name;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime startTime;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime endTime;

    @Positive(message = "Ngân sách phải > 0")
    @NotNull(message = "Ngân sách không được để trống")
    BigDecimal promotionBudget;
}
