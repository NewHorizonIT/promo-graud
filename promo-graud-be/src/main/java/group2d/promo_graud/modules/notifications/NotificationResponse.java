package group2d.promo_graud.modules.notifications;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationResponse {
    private Integer id;

    // ID nội dung notification
    private Integer notificationId;

    private Integer recipientId;

    private NotificationEnum type;
    private String title;
    private String message;
    private Integer referenceId;

    private Boolean isRead;
    private Boolean isDeleted;

    private LocalDateTime createdAt;
}
