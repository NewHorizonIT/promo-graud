package group2d.promo_graud.modules.orders.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import group2d.promo_graud.modules.orders.OrderPaymentEnum;
import group2d.promo_graud.modules.orders.OrderPaymentStatusEnum;
import group2d.promo_graud.modules.orders.OrderStatus;
import group2d.promo_graud.modules.orders.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    @Query(
            "SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.voucher.id = :voucherId "
                    + "AND o.status IN (:statuses)")
    long countByUserIdAndVoucherIdAndStatusIn(
            @Param("userId") Integer userId,
            @Param("voucherId") Integer voucherId,
            @Param("statuses") List<OrderStatus> statuses);

    @EntityGraph(attributePaths = {"items", "items.product"})
    public List<Order> findByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndIsDeletedFalse(
            LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"items", "items.product"})
    Page<Order> findByIsDeletedFalse(Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.product"})
    Page<Order> findByUserId(Integer userId, Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.product"})
    Page<Order> findByStatusAndIsDeletedFalse(OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.product"})
    Page<Order> findByUserUsernameContainingIgnoreCaseAndIsDeletedFalse(
            String username, Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.product"})
    Page<Order> findByStatusAndUserUsernameContainingIgnoreCaseAndIsDeletedFalse(
            OrderStatus status, String username, Pageable pageable);

    @EntityGraph(attributePaths = {"items", "items.product", "user", "voucher"})
    Optional<Order> findByUserIdAndIdempotencyKey(Integer userId, String idempotencyKey);

    @Query(
            "SELECT COALESCE(SUM(o.finalPrice), 0) FROM Order o"
                    + " WHERE o.user.id = :userId AND o.isDeleted = false")
    public BigDecimal sumFinalPriceByUserId(@Param("userId") Integer userId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            """
            UPDATE Order o
            SET o.status = :newStatus, o.paymentStatus = :newPay, o.transactionNo = :txnNo
            WHERE o.id = :id AND o.status = :curStatus AND o.paymentStatus = :curPay
            """)
    int transition(
            @Param("id") Integer id,
            @Param("curStatus") OrderStatus curStatus,
            @Param("curPay") OrderPaymentStatusEnum curPay,
            @Param("newStatus") OrderStatus newStatus,
            @Param("newPay") OrderPaymentStatusEnum newPay,
            @Param("txnNo") String txnNo);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(
            """
            UPDATE Order o
            SET o.status = :newStatus,
                o.paymentStatus = :newPay
            WHERE o.id = :id
              AND o.status = :curStatus
              AND o.paymentStatus = :curPay
            """)
    int transitionStatus(
            Integer id,
            OrderStatus curStatus,
            OrderPaymentStatusEnum curPay,
            OrderStatus newStatus,
            OrderPaymentStatusEnum newPay);

    // Tìm đơn VNPay kẹt PENDING + UNPAID tạo trước thời điểm chỉ định
    List<Order> findByPaymentMethodAndStatusAndPaymentStatusAndCreatedAtBefore(
            OrderPaymentEnum paymentMethod,
            OrderStatus status,
            OrderPaymentStatusEnum paymentStatus,
            LocalDateTime dateTime);
}
