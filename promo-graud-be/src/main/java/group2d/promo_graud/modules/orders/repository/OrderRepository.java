package group2d.promo_graud.modules.orders.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import group2d.promo_graud.modules.orders.OrderStatus;
import group2d.promo_graud.modules.orders.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    public Integer countByUserIdAndVoucherIdAndStatus(
            Integer userId, Integer voucherId, OrderStatus status);

    public List<Order> findByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndIsDeletedFalse(
            LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"items", "items.product"})
    public Page<Order> findByUserId(Integer userId, Pageable pageable);

    @Query(
            "SELECT COALESCE(SUM(o.finalPrice), 0) FROM Order o"
                    + " WHERE o.user.id = :userId AND o.isDeleted = false")
    public BigDecimal sumFinalPriceByUserId(@Param("userId") Integer userId);
}
