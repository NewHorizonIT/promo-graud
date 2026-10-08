package group2d.promo_graud.modules.orders.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.orders.OrderErrorCode;
import group2d.promo_graud.modules.orders.dto.request.OrderItemRequest;
import group2d.promo_graud.modules.orders.dto.request.OrderRequest;
import group2d.promo_graud.modules.orders.dto.response.OrderResponse;
import group2d.promo_graud.modules.orders.entity.Order;
import group2d.promo_graud.modules.orders.entity.OrderItem;
import group2d.promo_graud.modules.orders.repository.OrderItemRepository;
import group2d.promo_graud.modules.orders.repository.OrderRepository;
import group2d.promo_graud.modules.products.ProductErrorCode;
import group2d.promo_graud.modules.products.entity.Product;
import group2d.promo_graud.modules.products.repository.ProductRepository;
import group2d.promo_graud.modules.rules.RuleCampaign;
import group2d.promo_graud.modules.rules.enums.TypeOfRule;
import group2d.promo_graud.modules.user.entity.User;
import group2d.promo_graud.modules.user.entity.UserVoucher;
import group2d.promo_graud.modules.user.enums.UserVoucherStatus;
import group2d.promo_graud.modules.user.error.UserVoucherErrorCode;
import group2d.promo_graud.modules.user.repository.UserVoucherRepository;
import group2d.promo_graud.modules.user.service.UserService;
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
    private final UserService userService;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final VoucherRepository voucherRepository;
    private final DistributedLockService distributedLockService;
    private final TransactionTemplate transactionTemplate;
    private final UserVoucherRepository userVoucherRepository;

    public OrderResponse createOrder(OrderRequest request) {
        if (request.getVoucherId() == null) {
            return executeOrderTransaction(request);
        }
        String lockKey = "lock:voucher:" + request.getVoucherId();
        String lockValue = UUID.randomUUID().toString();
        boolean isLocked = distributedLockService.tryLock(lockKey, lockValue, 10);
        if (!isLocked) {
            throw new AppException(VoucherErrorCode.VOUCHER_BUSY);
        }
        try {
            // xử lý validate voucher,tạo order,update voucher, sau khi đc redis cấp lock
            return executeOrderTransaction(request);
        } finally {
            distributedLockService.unLock(lockKey, lockValue);
        }
    }

    private OrderResponse executeOrderTransaction(OrderRequest request) {
        return transactionTemplate.execute(
                status -> {
                    Authentication authentication =
                            SecurityContextHolder.getContext().getAuthentication();
                    User user = userService.findByUsername(authentication.getName());
                    Voucher voucher = null;
                    UserVoucher userVoucher = null;
                    if (request.getVoucherId() != null) {
                        voucher =
                                voucherRepository
                                        .findById(request.getVoucherId())
                                        .orElseThrow(
                                                () -> new AppException(VoucherErrorCode.NOT_EXIST));
                        userVoucher =
                                userVoucherRepository
                                        .findByUserIdAndVoucherId(user.getId(), voucher.getId())
                                        .orElseThrow(
                                                () ->
                                                        new AppException(
                                                                UserVoucherErrorCode
                                                                        .USER_VOUCHER_NOT_EXISTS));
                        validate(voucher, userVoucher);
                    }
                    Order order = Order.builder().user(user).voucher(voucher).build();
                    BigDecimal totalPrice =
                            createOrderItemAndCalculateTotal(order, request.getOrderItemRequests());
                    BigDecimal finalPrice = totalPrice;
                    if (request.getVoucherId() != null) {
                        finalPrice = calculateFinalPrice(totalPrice, voucher);
                    }
                    order.setTotalPrice(totalPrice);
                    order.setFinalPrice(finalPrice);
                    orderRepository.save(order);
                    if (voucher != null) {
                        consumeVoucher(voucher, userVoucher);
                    }
                    return mapToOrderResponse(order);
                });
    }

    public BigDecimal calculateFinalPrice(BigDecimal totalPrice, Voucher voucher) {
        BigDecimal finalPrice = totalPrice;
        RuleCampaign ruleCampaign = voucher.getRuleCampaign();
        if (ruleCampaign.getTypeOfRule().equals(TypeOfRule.PERCENTAGE)) {
            if (totalPrice.compareTo(ruleCampaign.getMinOrderValue()) < 0) {
                return totalPrice;
            }
            BigDecimal discount =
                    totalPrice
                            .multiply(ruleCampaign.getValue())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (discount.compareTo(ruleCampaign.getMaxDiscountValue()) > 0) {
                discount = ruleCampaign.getMaxDiscountValue();
            }
            finalPrice = totalPrice.subtract(discount);
        }
        if (ruleCampaign.getTypeOfRule().equals(TypeOfRule.FIXED_AMOUNT)) {
            if (totalPrice.compareTo(ruleCampaign.getMinOrderValue()) < 0) {
                return totalPrice;
            }
            finalPrice = totalPrice.subtract(ruleCampaign.getValue());
        }
        return finalPrice;
    }

    private BigDecimal createOrderItemAndCalculateTotal(
            Order order, List<OrderItemRequest> requests) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderItemRequest request : requests) {
            Product product =
                    productRepository
                            .findById(request.getProductId())
                            .orElseThrow(
                                    () -> new AppException(ProductErrorCode.PRODUCT_NOT_FOUND));
            totalPrice =
                    totalPrice.add(
                            product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
            OrderItem orderItem =
                    OrderItem.builder()
                            .order(order)
                            .product(product)
                            .quantity(request.getQuantity())
                            .price(product.getPrice())
                            .build();
            orderItemRepository.save(orderItem);
        }
        return totalPrice;
    }

    public void validate(Voucher voucher, UserVoucher userVoucher) {
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

    private void consumeVoucher(Voucher voucher, UserVoucher userVoucher) {
        voucher.setQuantityRemain(voucher.getQuantityRemain() - 1);
        userVoucher.setStatus(UserVoucherStatus.USED);
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
