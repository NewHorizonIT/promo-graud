package group2d.promo_graud.modules.orders.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import group2d.promo_graud.modules.voucher.enums.VoucherType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.orders.OrderErrorCode;
import group2d.promo_graud.modules.orders.OrderStatus;
import group2d.promo_graud.modules.orders.dto.request.OrderItemRequest;
import group2d.promo_graud.modules.orders.dto.request.OrderRequest;
import group2d.promo_graud.modules.orders.dto.response.OrderResponse;
import group2d.promo_graud.modules.orders.entity.Order;
import group2d.promo_graud.modules.orders.entity.OrderItem;
import group2d.promo_graud.modules.orders.repository.OrderItemRepository;
import group2d.promo_graud.modules.orders.repository.OrderRepository;
import group2d.promo_graud.modules.products.entity.Product;
import group2d.promo_graud.modules.products.repository.ProductRepository;
import group2d.promo_graud.modules.rules.engine.RuleEngine;
import group2d.promo_graud.modules.user.entity.User;
import group2d.promo_graud.modules.user.entity.UserVoucher;
import group2d.promo_graud.modules.user.enums.UserVoucherStatus;
import group2d.promo_graud.modules.user.error.UserErrorCode;
import group2d.promo_graud.modules.user.error.UserVoucherErrorCode;
import group2d.promo_graud.modules.user.repository.UserRepository;
import group2d.promo_graud.modules.user.repository.UserVoucherRepository;
import group2d.promo_graud.modules.voucher.Voucher;
import group2d.promo_graud.modules.voucher.VoucherRepository;
import group2d.promo_graud.modules.voucher.enums.VoucherErrorCode;
import group2d.promo_graud.modules.voucher.enums.VoucherStatus;
import group2d.promo_graud.shared.exception.AppException;

// Redis Lock
//    ↓
// BEGIN TRANSACTION
//    ↓
// Check voucher, userVoucher
//    ↓
// Create Order
//    ↓
// Decrease quantity
//    ↓
// COMMIT
//    ↓
// Unlock
@Service
@RequiredArgsConstructor
public class OrderService {
  private final OrderRepository orderRepository;
  private final UserRepository userRepository;
  private final ProductRepository productRepository;
  private final OrderItemRepository orderItemRepository;
  private final VoucherRepository voucherRepository;
  private final DistributedLockService distributedLockService;
  private final TransactionTemplate transactionTemplate;
  private final UserVoucherRepository userVoucherRepository;
  private final RuleEngine ruleEngine;

  public OrderResponse createOrder(OrderRequest request) {
    if (request.getVoucherId() == null) {
      return createOrderWithOutVoucher(request);
    }
    String lockKey = "lock:voucher:" + request.getVoucherId();
    String lockValue = UUID.randomUUID().toString();
    boolean isLocked = distributedLockService.tryLock(lockKey, lockValue, 10);
    if (!isLocked) {
      throw new AppException(VoucherErrorCode.VOUCHER_BUSY);
    }
    try {
      return createOrderVoucher(request);
    } finally {
      distributedLockService.unLock(lockKey, lockValue);
    }
  }

  private OrderResponse createOrderWithOutVoucher(OrderRequest request) {
    return transactionTemplate.execute(status -> {
      User user = getCurrentUser();
      Order order = orderRepository.save(Order.builder()
        .user(user)
        .totalPrice(BigDecimal.ZERO)
        .finalPrice(BigDecimal.ZERO)
        .voucher(null)
        .build());
      BigDecimal totalPrice = createOrderItemAndCalculateTotal(order, request.getItems());
      order.setTotalPrice(totalPrice);
      order.setFinalPrice(totalPrice);
      return mapToOrderResponse(order);
    });
  }

  private OrderResponse createOrderVoucher(OrderRequest request) {
    return transactionTemplate.execute(status -> {
      User user = getCurrentUser();
      Voucher currentVoucher = voucherRepository.findById(request.getVoucherId()).orElseThrow(() -> new AppException(VoucherErrorCode.NOT_EXIST));
      UserVoucher userVoucher = userVoucherRepository.findByUserIdAndVoucherId(user.getId(), currentVoucher.getId()).orElseThrow(() -> new AppException(UserVoucherErrorCode.USER_VOUCHER_NOT_EXISTS));
      if (currentVoucher.getType().equals(VoucherType.UNIQUE)) {
        validateUniqueVoucher(currentVoucher, userVoucher);
      } else {
        validateGenericVoucher(currentVoucher, userVoucher);
      }
      Order order = orderRepository.save(Order.builder()
        .user(user)
        .voucher(currentVoucher)
        .totalPrice(BigDecimal.ZERO)
        .finalPrice(BigDecimal.ZERO)
        .build());
      BigDecimal totalPrice = createOrderItemAndCalculateTotal(order, request.getItems());
      order.setTotalPrice(totalPrice);
      BigDecimal discount = ruleEngine.applyRules(order, currentVoucher.getRuleCampaign(), user);
      order.setFinalPrice(totalPrice.subtract(discount));
      consumeVoucher(currentVoucher, userVoucher);
      return mapToOrderResponse(order);
    });
  }

  public User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_EXISTS));
  }

  public void validateUniqueVoucher(Voucher voucher, UserVoucher userVoucher) {
    if (!voucher.getStatus().equals(VoucherStatus.ACTIVE)) {
      throw new AppException(VoucherErrorCode.INVALID_STATUS);
    }
    if (userVoucher.getStatus().equals(UserVoucherStatus.USED)) {
      throw new AppException(UserVoucherErrorCode.USER_VOUCHER_ALREADY_USED);
    }
  }

  public void validateGenericVoucher(Voucher voucher, UserVoucher userVoucher) {
    if (!voucher.getStatus().equals(VoucherStatus.ACTIVE)) {
      throw new AppException(VoucherErrorCode.INVALID_STATUS);
    }
    if (voucher.getQuantityRemain() <= 0) {
      throw new AppException(VoucherErrorCode.OUT_OF_STOCK);
    }
    if (userVoucher.getStatus().equals(UserVoucherStatus.USED)) {
      throw new AppException(UserVoucherErrorCode.USER_VOUCHER_ALREADY_USED);
    }
  }

  private BigDecimal createOrderItemAndCalculateTotal(
    Order order, List<OrderItemRequest> requests) {
    BigDecimal totalPrice = BigDecimal.ZERO;
    List<Integer> productIds = requests.stream().map(OrderItemRequest::getProductId).distinct().toList();
    List<Product> products = productRepository.findAllById(productIds);
    Map<Integer, Product> productMaps = products.stream().collect(Collectors.toMap(Product::getId, p -> p));
    List<OrderItem> items = new ArrayList<>();
    for (OrderItemRequest request : requests) {
      Product product = productMaps.get(request.getProductId());
      totalPrice = totalPrice.add(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
      OrderItem item = OrderItem.builder()
        .order(order)
        .product(product)
        .quantity(request.getQuantity())
        .price(product.getPrice())
        .build();
      items.add(item);
      orderItemRepository.save(item);
    }
    order.setItems(items);
    return totalPrice;
  }

  private void consumeVoucher(Voucher voucher, UserVoucher userVoucher) {
    if (voucher.getType().equals(VoucherType.UNIQUE)) {
      userVoucher.setStatus(UserVoucherStatus.USED);
      voucher.setStatus(VoucherStatus.DISABLED);
    } else {
      voucher.setQuantityRemain(voucher.getQuantityRemain() - 1);
      if (voucher.getQuantityRemain() == 0) {
        voucher.setStatus(VoucherStatus.DISABLED);
      }
      long usedCount = orderRepository.countByUserIdAndVoucherIdAndStatus(userVoucher.getUser().getId(), voucher.getId(), OrderStatus.SUCCESS);
      if (usedCount >= voucher.getLimitClient()) {
        userVoucher.setStatus(UserVoucherStatus.USED);
      }
    }
  }

  public List<OrderResponse> findAll() {
    List<Order> listOder = orderRepository.findAll();
    return listOder.stream().map(this::mapToOrderResponse).toList();
  }

  public OrderResponse findById(Integer id) {
    Order order =
      orderRepository
        .findById(id)
        .orElseThrow(() -> new AppException(OrderErrorCode.ORDER_NOT_EXISTS));
    return mapToOrderResponse(order);
  }

  public List<OrderResponse> filterByTime(LocalDate startDate, LocalDate endDate) {
    if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("Khoảng thời gian không hợp lệ");
    }
    LocalDateTime start = startDate.atStartOfDay();
    LocalDateTime end = endDate.plusDays(1).atStartOfDay();
    return orderRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndIsDeletedFalse(start, end)
      .stream()
      .map(this::mapToOrderResponse)
      .toList();
  }

  public OrderResponse mapToOrderResponse(Order order) {
    return OrderResponse.builder()
      .id(order.getId())
      .user(order.getUser())
      .voucher(order.getVoucher())
      .status(order.getStatus())
      .totalPrice(order.getTotalPrice())
      .finalPrice(order.getFinalPrice())
      .build();
  }
}
