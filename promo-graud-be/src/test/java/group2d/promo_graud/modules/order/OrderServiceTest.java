package group2d.promo_graud.modules.order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import group2d.promo_graud.modules.orders.dto.request.OrderItemRequest;
import group2d.promo_graud.modules.orders.dto.request.OrderRequest;
import group2d.promo_graud.modules.orders.dto.response.OrderResponse;
import group2d.promo_graud.modules.orders.entity.Order;
import group2d.promo_graud.modules.orders.repository.OrderItemRepository;
import group2d.promo_graud.modules.orders.repository.OrderRepository;
import group2d.promo_graud.modules.orders.service.DistributedLockService;
import group2d.promo_graud.modules.orders.service.OrderService;
import group2d.promo_graud.modules.products.entity.Product;
import group2d.promo_graud.modules.products.repository.ProductRepository;
import group2d.promo_graud.modules.rules.RuleCampaign;
import group2d.promo_graud.modules.rules.engine.RuleEngine;
import group2d.promo_graud.modules.user.entity.User;
import group2d.promo_graud.modules.user.entity.UserVoucher;
import group2d.promo_graud.modules.user.enums.UserVoucherStatus;
import group2d.promo_graud.modules.user.repository.UserRepository;
import group2d.promo_graud.modules.user.repository.UserVoucherRepository;
import group2d.promo_graud.modules.voucher.Voucher;
import group2d.promo_graud.modules.voucher.VoucherRepository;
import group2d.promo_graud.modules.voucher.enums.VoucherStatus;

@ExtendWith({MockitoExtension.class})
@DisplayName("Order Service Unit Test")
public class OrderServiceTest {
    @Mock private OrderRepository orderRepository;

    @Mock private RuleEngine ruleEngine;

    @Mock private UserVoucherRepository userVoucherRepository;

    @Mock private ProductRepository productRepository;

    @Mock private OrderItemRepository orderItemRepository;

    @Mock private VoucherRepository voucherRepository;

    @Mock private DistributedLockService distributedLockService;

    @Mock private TransactionTemplate transactionTemplate;

    @Mock private UserRepository userRepository;

    @InjectMocks private OrderService orderService;

    private final User user = User.builder().id(1).username("huy123").password("123456").build();

    private final Product product1 =
            Product.builder().id(1).price(BigDecimal.valueOf(200000)).build();

    private final Product product2 =
            Product.builder().id(2).price(BigDecimal.valueOf(400000)).build();

    private final Product product3 =
            Product.builder().id(2).price(BigDecimal.valueOf(400000)).build();

    private final List<Integer> productIds = List.of(product1.getId(), product2.getId());
    private final List<Product> products = List.of(product1, product2);

    private final OrderItemRequest itemRequest1 =
            OrderItemRequest.builder().productId(1).quantity(2).build();

    private final OrderItemRequest itemRequest2 =
            OrderItemRequest.builder().productId(2).quantity(1).build();

    private final List<OrderItemRequest> itemRequest = List.of(itemRequest1, itemRequest2);

    private final OrderRequest request =
            OrderRequest.builder().voucherId(1).items(itemRequest).build();

    @Test
    @DisplayName("TC1: Tạo hóa đơn thành công không có voucher")
    void createOrder_withoutVoucher_success() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("huy123", "123456");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Order order = Order.builder().id(1).user(user).build();

        when(userRepository.findByUsername("huy123")).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(productRepository.findAllById(productIds)).thenReturn(products);
        // TransactionTemplate chạy callback thật
        when(transactionTemplate.execute(any(TransactionCallback.class)))
                .thenAnswer(
                        invocation -> {
                            TransactionCallback<OrderResponse> callback = invocation.getArgument(0);
                            return callback.doInTransaction(null);
                        });
        request.setVoucherId(null);
        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(0, BigDecimal.valueOf(800_000).compareTo(response.getTotalPrice()));
        assertEquals(0, BigDecimal.valueOf(800_000).compareTo(response.getFinalPrice()));
        assertNull(response.getVoucher());
    }

    @Test
    @DisplayName("TC2: Tạo hóa đơn thành công có voucher")
    public void createOrder_hasVoucher_typeOfRulePercentage_success() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("huy123", "123456");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        RuleCampaign ruleCampaign = RuleCampaign.builder().id(1).build();

        Voucher voucher =
                Voucher.builder()
                        .id(1)
                        .ruleCampaign(ruleCampaign)
                        .status(VoucherStatus.ACTIVE)
                        .quantity(100)
                        .quantityRemain(10)
                        .build();

        UserVoucher userVoucher =
                UserVoucher.builder()
                        .user(user)
                        .voucher(voucher)
                        .status(UserVoucherStatus.UNUSED)
                        .build();

        Order order = Order.builder().id(1).voucher(voucher).user(user).build();

        when(userRepository.findByUsername("huy123")).thenReturn(Optional.of(user));
        when(voucherRepository.findById(1)).thenReturn(Optional.of(voucher));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(ruleEngine.applyRules(order, ruleCampaign, user))
                .thenReturn(BigDecimal.valueOf(240000));
        when(productRepository.findAllById(productIds)).thenReturn(products);
        when(userVoucherRepository.findByUserIdAndVoucherId(1, 1))
                .thenReturn(Optional.of(userVoucher));
        when(transactionTemplate.execute(any(TransactionCallback.class)))
                .thenAnswer(
                        invocation -> {
                            TransactionCallback<OrderResponse> callback = invocation.getArgument(0);
                            return callback.doInTransaction(null);
                        });
        when(distributedLockService.tryLock(eq("lock:voucher:1"), anyString(), eq(10L)))
                .thenReturn(true);
        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(response.getVoucher().getQuantityRemain(), 9);
        assertEquals(0, response.getTotalPrice().compareTo(BigDecimal.valueOf(800000)));
        assertEquals(0, response.getFinalPrice().compareTo(BigDecimal.valueOf(560000)));
    }
}
