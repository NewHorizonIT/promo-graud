package group2d.promo_graud.modules.orders.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import group2d.promo_graud.modules.orders.OrderStatus;
import group2d.promo_graud.modules.orders.entity.Order;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
  public Integer countByUserIdAndVoucherIdAndStatus(Integer userId, Integer voucherId, OrderStatus status);

  public List<Order> findByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndIsDeletedFalse(LocalDateTime start, LocalDateTime end);
}
