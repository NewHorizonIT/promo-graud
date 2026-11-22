package group2d.promo_graud.modules.notifications;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.notifications.component.NotificationPushEvent;
import group2d.promo_graud.modules.notifications.entity.Notification;
import group2d.promo_graud.modules.notifications.entity.NotificationRecipient;
import group2d.promo_graud.modules.notifications.repository.NotificationRecipientRepository;
import group2d.promo_graud.modules.notifications.repository.NotificationRepository;
import group2d.promo_graud.shared.exception.AppException;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationRecipientRepository recipientRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createForUsers(
            List<Integer> userIds,
            NotificationEnum type,
            String title,
            String message,
            Integer referenceId) {

        List<Integer> uniqueUserIds = userIds.stream().distinct().toList();

        if (uniqueUserIds.isEmpty()) {
            return;
        }

        Notification notification =
                notificationRepository.save(
                        Notification.builder()
                                .type(type)
                                .title(title)
                                .message(message)
                                .referenceId(referenceId)
                                .build());

        List<NotificationRecipient> recipients =
                uniqueUserIds.stream()
                        .map(
                                userId ->
                                        NotificationRecipient.builder()
                                                .notification(notification)
                                                .recipientId(userId)
                                                .isRead(false)
                                                .isDeleted(false)
                                                .createdAt(LocalDateTime.now())
                                                .build())
                        .toList();

        List<NotificationRecipient> saved = recipientRepository.saveAll(recipients);

        List<NotificationResponse> payloads = saved.stream().map(this::toResponse).toList();

        eventPublisher.publishEvent(new NotificationPushEvent(payloads));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(Integer userId) {

        return recipientRepository.findRecent(userId, PageRequest.of(0, 20)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countUnread(Integer userId) {
        return recipientRepository.countByRecipientIdAndIsReadFalseAndIsDeletedFalse(userId);
    }

    @Transactional
    public void markAsRead(Integer recipientRecordId, Integer userId) {

        NotificationRecipient recipient =
                recipientRepository
                        .findByIdAndRecipientIdAndIsDeletedFalse(recipientRecordId, userId)
                        .orElseThrow(
                                () ->
                                        new AppException(
                                                NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        recipient.setIsRead(true);
        recipient.setReadAt(LocalDateTime.now());

        recipientRepository.save(recipient);
    }

    @Transactional
    public void markAllAsRead(Integer userId) {
        recipientRepository.markAllAsRead(userId);
    }

    @Transactional
    public void delete(Integer recipientRecordId, Integer userId) {

        NotificationRecipient recipient =
                recipientRepository
                        .findByIdAndRecipientIdAndIsDeletedFalse(recipientRecordId, userId)
                        .orElseThrow(
                                () ->
                                        new AppException(
                                                NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        recipient.setIsDeleted(true);
        recipientRepository.save(recipient);
    }

    private NotificationResponse toResponse(NotificationRecipient recipient) {

        Notification notification = recipient.getNotification();

        return NotificationResponse.builder()
                .id(recipient.getId())
                .notificationId(notification.getId())
                .recipientId(recipient.getRecipientId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .referenceId(notification.getReferenceId())
                .isRead(recipient.getIsRead())
                .isDeleted(recipient.getIsDeleted())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
