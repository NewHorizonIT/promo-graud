package group2d.promo_graud.modules.notifications.component;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import group2d.promo_graud.modules.notifications.NotificationResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationWebSocketListener {

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationPush(NotificationPushEvent event) {

        for (NotificationResponse recipient : event.recipients()) {

            try {
                messagingTemplate.convertAndSendToUser(
                        String.valueOf(recipient.getRecipientId()),
                        "/queue/notifications",
                        recipient);
            } catch (Exception ex) {
                log.error("Cannot push notification to user {}", recipient.getRecipientId(), ex);
            }
        }
    }
}
