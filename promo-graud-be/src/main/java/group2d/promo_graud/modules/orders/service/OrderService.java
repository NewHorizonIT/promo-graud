package group2d.promo_graud.modules.orders.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.orders.OrderErrorCode;
import group2d.promo_graud.modules.orders.OrderPaymentEnum;
import group2d.promo_graud.modules.orders.OrderPaymentStatusEnum;
import group2d.promo_graud.modules.orders.OrderStatus;
import group2d.promo_graud.modules.orders.dto.request.OrderItemRequest;
import group2d.promo_graud.modules.orders.dto.request.OrderPreviewRequest;
import group2d.promo_graud.modules.orders.dto.request.OrderRequest;
import group2d.promo_graud.modules.orders.dto.request.OrderStatusRequest;
import group2d.promo_graud.modules.orders.dto.response.OrderItemResponse;
import group2d.promo_graud.modules.orders.dto.response.OrderPreviewResponse;
import group2d.promo_graud.modules.orders.dto.response.OrderResponse;
import group2d.promo_graud.modules.orders.dto.response.UserSummaryResponse;
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
import group2d.promo_graud.modules.voucher.VoucherRedemption;
import group2d.promo_graud.modules.voucher.VoucherRedemptionRepository;
import group2d.promo_graud.modules.voucher.VoucherRepository;
import group2d.promo_graud.modules.voucher.dto.responses.VoucherResponse;
import group2d.promo_graud.modules.voucher.enums.VoucherErrorCode;
import group2d.promo_graud.modules.voucher.enums.VoucherStatus;
import group2d.promo_graud.modules.voucher.enums.VoucherType;
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
    private final VoucherRedemptionRepository voucherRedemptionRepository;
    private final DistributedLockService distributedLockService;
    private final TransactionTemplate transactionTemplate;
    private final UserVoucherRepository userVoucherRepository;
    private final RuleEngine ruleEngine;

    public OrderResponse createOrder(OrderRequest request, String ipAddress, String idemKey) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new AppException(OrderErrorCode.ORDER_ITEMS_NOT_FOUND);
        }
        String key = (idemKey == null || idemKey.isBlank()) ? null : idemKey.trim();
        if (key != null && key.length() > 64) {
            throw new AppException(OrderErrorCode.INVALID_IDEMPOTENCY_KEY); // thêm vào enum
        }
        User user = getCurrentUser();

        // Lớp 1: replay nhanh, không cần chờ lock
        if (key != null) {
            Optional<Order> existing =
                    orderRepository.findByUserIdAndIdempotencyKey(user.getId(), key);
            if (existing.isPresent()) {
                return mapToOrderResponse(existing.get());
            }
        }
        try {

            if (request.getVoucherId() == null) {
                return createOrderWithOutVoucher(request, key);
            }

            String lockKey = "lock:voucher:" + request.getVoucherId();
            String lockValue = UUID.randomUUID().toString();
            boolean isLocked = distributedLockService.tryLock(lockKey, lockValue, 10);
            if (!isLocked) {
                throw new AppException(VoucherErrorCode.VOUCHER_BUSY);
            }
            try {
                return createOrderVoucher(request, ipAddress, key);
            } finally {
                distributedLockService.unLock(lockKey, lockValue);
            }
        } catch (DataIntegrityViolationException e) {
            // Lớp 3: 2 request cùng key lọt qua lớp 1 cùng lúc, unique index chỉ cho 1 request
            // insert
            if (key != null) {
                return orderRepository
                        .findByUserIdAndIdempotencyKey(user.getId(), key)
                        .map(this::mapToOrderResponse)
                        .orElseThrow(() -> e); // không có đơn cùng key thì là lỗi khác, ném lại
            }
            throw e;
        }
    }

    private OrderResponse createOrderWithOutVoucher(OrderRequest request, String key) {
        return transactionTemplate.execute(
                status -> {
                    User user = getCurrentUser();
                    Order order =
                            orderRepository.save(
                                    Order.builder()
                                            .user(user)
                                            .totalPrice(BigDecimal.ZERO)
                                            .finalPrice(BigDecimal.ZERO)
                                            .idempotencyKey(key)
                                            .paymentMethod(request.getPaymentMethod())
                                            .paymentStatus(OrderPaymentStatusEnum.UNPAID)
                                            .voucher(null)
                                            .build());
                    BigDecimal totalPrice =
                            createOrderItemAndCalculateTotal(order, request.getItems());
                    order.setTotalPrice(totalPrice);
                    order.setFinalPrice(totalPrice);
                    order.setStatus(OrderStatus.PENDING);
                    order.setPaymentStatus(OrderPaymentStatusEnum.UNPAID);
                    orderRepository.save(order);

                    return mapToOrderResponse(order);
                });
    }

    private OrderResponse createOrderVoucher(OrderRequest request, String ipAddress, String key) {
        return transactionTemplate.execute(
                status -> {
                    User user = getCurrentUser();
                    // Lớp 2: kiểm tra lại SAU KHI đã có lock
                    if (key != null) {
                        Optional<Order> existing =
                                orderRepository.findByUserIdAndIdempotencyKey(user.getId(), key);
                        if (existing.isPresent()) {
                            return mapToOrderResponse(existing.get());
                        }
                    }
                    Voucher currentVoucher =
                            voucherRepository
                                    .findById(request.getVoucherId())
                                    .orElseThrow(
                                            () -> new AppException(VoucherErrorCode.NOT_EXIST));
                    UserVoucher userVoucher =
                            userVoucherRepository
                                    .findByUserIdAndVoucherId(user.getId(), currentVoucher.getId())
                                    .orElseThrow(
                                            () ->
                                                    new AppException(
                                                            UserVoucherErrorCode
                                                                    .USER_VOUCHER_NOT_EXISTS));
                    if (currentVoucher.getType().equals(VoucherType.UNIQUE)) {
                        validateUniqueVoucher(currentVoucher, userVoucher);
                    } else {
                        validateGenericVoucher(currentVoucher, userVoucher);
                    }
                    Order order =
                            orderRepository.save(
                                    Order.builder()
                                            .user(user)
                                            .voucher(currentVoucher)
                                            .paymentMethod(request.getPaymentMethod())
                                            .paymentStatus(OrderPaymentStatusEnum.UNPAID)
                                            .idempotencyKey(key)
                                            .totalPrice(BigDecimal.ZERO)
                                            .finalPrice(BigDecimal.ZERO)
                                            .build());
                    BigDecimal totalPrice =
                            createOrderItemAndCalculateTotal(order, request.getItems());
                    order.setTotalPrice(totalPrice);
                    BigDecimal discount =
                            ruleEngine.applyRules(order, currentVoucher.getRuleCampaign(), user);
                    // Fix: check final price >= 0
                    BigDecimal finalPrice = totalPrice.subtract(discount).max(BigDecimal.ZERO);
                    order.setFinalPrice(finalPrice);
                    order.setPaymentMethod(request.getPaymentMethod());
                    order.setStatus(OrderStatus.PENDING);
                    order.setPaymentStatus(OrderPaymentStatusEnum.UNPAID);
                    consumeVoucher(currentVoucher, userVoucher);
                    voucherRedemptionRepository.save(
                            VoucherRedemption.builder()
                                    .user(user)
                                    .order(order)
                                    .voucher(currentVoucher)
                                    .ipAddress(ipAddress)
                                    .statusOfOrder(OrderStatus.PENDING)
                                    .build());
                    orderRepository.save(order);
                    return mapToOrderResponse(order);
                });
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_EXISTS));
    }

    public void validateUniqueVoucher(Voucher voucher, UserVoucher userVoucher) {
        if (userVoucher.getStatus().equals(UserVoucherStatus.USED)) {
            throw new AppException(UserVoucherErrorCode.USER_VOUCHER_ALREADY_USED);
        }
        if (!voucher.getStatus().equals(VoucherStatus.ACTIVE)) {
            throw new AppException(VoucherErrorCode.INVALID_STATUS);
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
        List<Integer> productIds =
                requests.stream().map(OrderItemRequest::getProductId).distinct().toList();
        List<Product> products = productRepository.findAllById(productIds);
        Map<Integer, Product> productMaps =
                products.stream().collect(Collectors.toMap(Product::getId, p -> p));
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemRequest request : requests) {
            Product product = productMaps.get(request.getProductId());
            if (product == null) {
                throw new AppException(OrderErrorCode.ORDER_ITEMS_NOT_FOUND);
            }
            totalPrice =
                    totalPrice.add(
                            product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
            OrderItem item =
                    OrderItem.builder()
                            .order(order)
                            .product(product)
                            .quantity(request.getQuantity())
                            .price(product.getPrice())
                            .build();
            items.add(item);
        }
        orderItemRepository.saveAll(items);
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
            long usedCount =
                    orderRepository.countByUserIdAndVoucherIdAndStatusIn(
                            userVoucher.getUser().getId(),
                            voucher.getId(),
                            List.of(OrderStatus.SUCCESS, OrderStatus.PENDING));
            if (usedCount >= voucher.getLimitClient()) {
                userVoucher.setStatus(UserVoucherStatus.USED);
            }
        }
        voucherRepository.save(voucher);
        userVoucherRepository.save(userVoucher);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Page<OrderResponse> findAll(
            Integer page, Integer size, OrderStatus status, String username) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));
        boolean hasStatus = status != null;
        boolean hasUsername = username != null && !username.isBlank();
        if (hasStatus && hasUsername) {
            return orderRepository
                    .findByStatusAndUserUsernameContainingIgnoreCaseAndIsDeletedFalse(
                            status, username, pageable)
                    .map(this::mapToOrderResponse);
        }
        if (hasStatus) {
            return orderRepository
                    .findByStatusAndIsDeletedFalse(status, pageable)
                    .map(this::mapToOrderResponse);
        }
        if (hasUsername) {
            return orderRepository
                    .findByUserUsernameContainingIgnoreCaseAndIsDeletedFalse(username, pageable)
                    .map(this::mapToOrderResponse);
        }
        return orderRepository.findByIsDeletedFalse(pageable).map(this::mapToOrderResponse);
    }

    @PostAuthorize(
            "hasAuthority('ROLE_ADMIN') or returnObject.user.username == authentication.name")
    public OrderResponse findById(Integer id) {
        Order order =
                orderRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(OrderErrorCode.ORDER_NOT_EXISTS));
        return mapToOrderResponse(order);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<OrderResponse> filterByTime(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Khoảng thời gian không hợp lệ");
        }
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        return orderRepository
                .findByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndIsDeletedFalse(start, end)
                .stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    public Page<OrderResponse> findByUserId(Integer userId, Integer page, Integer limit) {
        Pageable pageable =
                PageRequest.of(
                        Math.max(0, page), Math.max(1, limit), Sort.by(Sort.Direction.DESC, "id"));

        Page<Order> orders = orderRepository.findByUserId(userId, pageable);

        return orders.map(this::mapToOrderResponse);
    }

    public void releaseVoucher(Order order) {
        if (order.getVoucher() == null) {
            return;
        }

        String lockKey = "lock:voucher:" + order.getVoucher().getId();
        String lockValue = UUID.randomUUID().toString();
        boolean isLocked = distributedLockService.tryLock(lockKey, lockValue, 10);
        if (!isLocked) {
            throw new AppException(VoucherErrorCode.VOUCHER_BUSY);
        }
        try {
            transactionTemplate.execute(
                    status -> {
                        Voucher voucher =
                                voucherRepository.findById(order.getVoucher().getId()).orElse(null);
                        if (voucher == null) {
                            return null;
                        }

                        LocalDateTime ruleEndTime = voucher.getRuleCampaign().getEndTime();
                        boolean isExpired =
                                ruleEndTime != null && LocalDateTime.now().isAfter(ruleEndTime);

                        if (isExpired) {
                            // Nếu Rule đã hết hạn -> KHÔNG hoàn lại trạng thái ACTIVE và UNUSED
                            // Có thể set status của Voucher thành DISABLED (nếu cần thiết để dọn
                            // dẹp)
                            voucher.setStatus(VoucherStatus.DISABLED);
                            voucherRepository.save(voucher);
                            return null;
                        }

                        if (voucher.getType().equals(VoucherType.UNIQUE)) {
                            voucher.setStatus(VoucherStatus.ACTIVE);
                        } else {
                            voucher.setQuantityRemain(voucher.getQuantityRemain() + 1);
                            voucher.setStatus(VoucherStatus.ACTIVE);
                        }
                        voucherRepository.save(voucher);

                        userVoucherRepository
                                .findByUserIdAndVoucherId(order.getUser().getId(), voucher.getId())
                                .ifPresent(
                                        uv -> {
                                            uv.setStatus(UserVoucherStatus.UNUSED);
                                            userVoucherRepository.save(uv);
                                        });
                        return null;
                    });
        } finally {
            distributedLockService.unLock(lockKey, lockValue);
        }
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public OrderResponse updatedStatus(Integer id, OrderStatusRequest request) {
        Order order =
                orderRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(OrderErrorCode.ORDER_NOT_EXISTS));

        OrderStatus current = order.getStatus();
        OrderStatus target = request.getStatus();
        OrderPaymentStatusEnum pay = order.getPaymentStatus(); // lấy từ DB, không tin client
        boolean isCod = order.getPaymentMethod() == OrderPaymentEnum.COD;

        if (target == current) {
            throw new AppException(OrderErrorCode.ORDER_CANNOT_DUPLICATE);
        }
        if (current != OrderStatus.PENDING) {
            throw new AppException(OrderErrorCode.ORDER_STATUS);
        }

        OrderPaymentStatusEnum newPays = pay;
        if (target == OrderStatus.CANCELLED) {
            // VNP đã PAID thì phải hoàn tiền thủ công, không hủy ở đây
            if (!isCod && pay != OrderPaymentStatusEnum.UNPAID) {
                throw new AppException(OrderErrorCode.ORDER_STATUS);
            }
        } else if (target == OrderStatus.SUCCESS) {
            if (isCod) {
                newPays = OrderPaymentStatusEnum.PAID; // COD: giao xong mới thu tiền
            } else if (pay != OrderPaymentStatusEnum.PAID) {
                throw new AppException(OrderErrorCode.ORDER_STATUS);
            }
        } else {
            throw new AppException(
                    OrderErrorCode.ORDER_STATUS); // admin chỉ được chọn SUCCESS / CANCELLED
        }

        int updated =
                orderRepository.transition(
                        id, current, pay, target, newPays, order.getTransactionNo());
        if (updated == 0) {
            throw new AppException(OrderErrorCode.ORDER_STATUS); // vừa bị IPN/admin khác đổi
        }

        Order fresh =
                orderRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(OrderErrorCode.ORDER_NOT_EXISTS));
        if (target == OrderStatus.CANCELLED) {
            releaseVoucher(fresh);
        }
        return mapToOrderResponse(fresh);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void deleteOrder(Integer id) {
        Order order =
                orderRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(OrderErrorCode.ORDER_NOT_EXISTS));

        if (order.getStatus() == OrderStatus.PENDING) {
            throw new AppException(OrderErrorCode.ORDER_CANNOT_DELETE);
        } else if (order.getStatus() == OrderStatus.SUCCESS) {
            throw new AppException(OrderErrorCode.ORDER_CANNOT_DELETE);
        }
        order.setIsDeleted(true);

        orderRepository.save(order);
    }

    public OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses =
                order.getItems().stream()
                        .map(
                                item ->
                                        OrderItemResponse.builder()
                                                .id(item.getId())
                                                .productName(item.getProduct().getName())
                                                .quantity(item.getQuantity())
                                                .price(item.getPrice())
                                                .build())
                        .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .user(UserSummaryResponse.fromEntity(order.getUser()))
                .voucher(
                        order.getVoucher() != null
                                ? VoucherResponse.fromEntity(order.getVoucher())
                                : null)
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .finalPrice(order.getFinalPrice())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .idempotencyKey(order.getIdempotencyKey())
                .createdAt(order.getCreatedAt())
                .isDeleted(order.getIsDeleted())
                .items(itemResponses)
                .build();
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public OrderResponse cancelOrder(Integer id) {
        Order order =
                orderRepository
                        .findById(id)
                        .orElseThrow(() -> new AppException(OrderErrorCode.ORDER_NOT_EXISTS));
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new AppException(OrderErrorCode.ORDER_CANNOT_CANCEL);
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        return mapToOrderResponse(order);
    }

    public BigDecimal getTotalSpending(Integer userId) {
        BigDecimal totalSpending = orderRepository.sumFinalPriceByUserId(userId);

        return totalSpending;
    }

    public OrderPreviewResponse previewOrder(OrderPreviewRequest request) {
        User user = getCurrentUser();

        List<Integer> productIds =
                request.getItems().stream().map(i -> i.getProductId()).distinct().toList();
        List<Product> products = productRepository.findAllById(productIds);
        Map<Integer, Product> productMap =
                products.stream().collect(Collectors.toMap(p -> p.getId(), p -> p));

        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItemRequest item : request.getItems()) {
            Product product = productMap.get(item.getProductId());
            if (product == null) {
                throw new AppException(OrderErrorCode.ORDER_ITEMS_NOT_FOUND);
            }
            subtotal =
                    subtotal.add(
                            product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        if (request.getVoucherId() == null) {
            return OrderPreviewResponse.builder()
                    .subtotal(subtotal)
                    .discount(BigDecimal.ZERO)
                    .finalAmount(subtotal)
                    .build();
        }

        Voucher voucher =
                voucherRepository
                        .findById(request.getVoucherId())
                        .orElseThrow(() -> new AppException(VoucherErrorCode.NOT_EXIST));
        UserVoucher userVoucher =
                userVoucherRepository
                        .findByUserIdAndVoucherId(user.getId(), voucher.getId())
                        .orElseThrow(
                                () ->
                                        new AppException(
                                                UserVoucherErrorCode.USER_VOUCHER_NOT_EXISTS));

        if (userVoucher.getStatus() != UserVoucherStatus.UNUSED) {
            throw new AppException(UserVoucherErrorCode.USER_VOUCHER_ALREADY_USED);
        }

        if (voucher.getType().equals(VoucherType.UNIQUE)) {
            validateUniqueVoucher(voucher, userVoucher);
        } else {
            validateGenericVoucher(voucher, userVoucher);
        }

        List<OrderItem> itemList = new ArrayList<>();
        for (OrderItemRequest req : request.getItems()) {
            Product product = productMap.get(req.getProductId());
            itemList.add(
                    OrderItem.builder()
                            .product(product)
                            .quantity(req.getQuantity())
                            .price(product.getPrice())
                            .build());
        }

        Order transientOrder =
                Order.builder()
                        .user(user)
                        .totalPrice(subtotal)
                        .finalPrice(subtotal)
                        .items(itemList)
                        .build();

        BigDecimal discount =
                ruleEngine.applyRules(transientOrder, voucher.getRuleCampaign(), user);

        return OrderPreviewResponse.builder()
                .subtotal(subtotal)
                .discount(discount)
                .finalAmount(subtotal.subtract(discount))
                .build();
    }
}
