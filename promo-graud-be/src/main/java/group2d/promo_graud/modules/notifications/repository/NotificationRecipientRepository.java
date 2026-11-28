package group2d.promo_graud.modules.notifications.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import group2d.promo_graud.modules.notifications.entity.NotificationRecipient;

public interface NotificationRecipientRepository
        extends JpaRepository<NotificationRecipient, Integer> {

    @Query(
            """
                SELECT nr
                FROM NotificationRecipient nr
                JOIN FETCH nr.notification n
                WHERE nr.recipientId = :userId
                  AND nr.isDeleted = false
                ORDER BY nr.createdAt DESC
            """)
    List<NotificationRecipient> findRecent(@Param("userId") Integer userId, Pageable pageable);

    long countByRecipientIdAndIsReadFalseAndIsDeletedFalse(Integer recipientId);

    Optional<NotificationRecipient> findByIdAndRecipientIdAndIsDeletedFalse(
            Integer id, Integer recipientId);

    @Modifying
    @Query(
            """
                UPDATE NotificationRecipient nr
                SET nr.isRead = true,
                    nr.readAt = CURRENT_TIMESTAMP
                WHERE nr.recipientId = :userId
                  AND nr.isRead = false
                  AND nr.isDeleted = false
            """)
    void markAllAsRead(@Param("userId") Integer userId);
}
