package group2d.promo_graud.modules.orders.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import group2d.promo_graud.modules.orders.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {}
