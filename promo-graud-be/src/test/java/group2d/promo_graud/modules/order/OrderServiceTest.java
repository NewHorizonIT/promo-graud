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
import group2d.promo_graud.modules.orders.repository.OrderItemRepository;
import group2d.promo_graud.modules.orders.repository.OrderRepository;
import group2d.promo_graud.modules.orders.service.DistributedLockService;
import group2d.promo_graud.modules.orders.service.OrderService;
import group2d.promo_graud.modules.products.entity.Product;
import group2d.promo_graud.modules.products.repository.ProductRepository;
import group2d.promo_graud.modules.rules.RuleCampaign;
import group2d.promo_graud.modules.rules.enums.TypeOfRule;
import group2d.promo_graud.modules.user.entity.User;
import group2d.promo_graud.modules.user.entity.UserVoucher;
import group2d.promo_graud.modules.user.enums.UserVoucherStatus;
import group2d.promo_graud.modules.user.repository.UserVoucherRepository;
import group2d.promo_graud.modules.user.service.UserService;
import group2d.promo_graud.modules.voucher.Voucher;
import group2d.promo_graud.modules.voucher.VoucherRepository;
import group2d.promo_graud.modules.voucher.enums.VoucherStatus;

@ExtendWith({MockitoExtension.class})
@DisplayName("Order Service Unit Test")
public class OrderServiceTest {
    @Mock private OrderRepository orderRepository;

    @Mock private UserService userService;

    @Mock private ProductRepository productRepository;

    @Mock private OrderItemRepository orderItemRepository;

    @Mock private VoucherRepository voucherRepository;

    @Mock private DistributedLockService distributedLockService;

    @Mock private TransactionTemplate transactionTemplate;

    @Mock private UserVoucherRepository userVoucherRepository;

    @InjectMocks private OrderService orderService;

    @Test
    @DisplayName("TC1: Calculate percentage discount successfully")
    void calculateFinalPrice_percentage_success() {
        RuleCampaign ruleCampaign =
                RuleCampaign.builder()
                        .typeOfRule(TypeOfRule.PERCENTAGE)
                        .value(BigDecimal.valueOf(20))
                        .minOrderValue(BigDecimal.valueOf(100_000))
                        .maxDiscountValue(BigDecimal.valueOf(60_000))
                        .build();
        Voucher voucher = Voucher.builder().ruleCampaign(ruleCampaign).build();
        BigDecimal totalPrice = BigDecimal.valueOf(200_000);
        BigDecimal result = orderService.calculateFinalPrice(totalPrice, voucher);
        assertEquals(0, BigDecimal.valueOf(160_000).compareTo(result));
    }

    @Test
    @DisplayName("TC2: Tạo hóa đơn thành công không có voucher")
    void createOrder_withoutVoucher_success() {
        User user = User.builder().id(1).username("huy123").password("123456").build();

        Product product = Product.builder().id(1).price(BigDecimal.valueOf(100_000)).build();

        OrderItemRequest itemRequest = OrderItemRequest.builder().productId(1).quantity(2).build();

        OrderRequest request =
                OrderRequest.builder()
                        .voucherId(null)
                        .orderItemRequests(List.of(itemRequest))
                        .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken("huy123", "123456");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userService.findByUsername("huy123")).thenReturn(user);
        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        // TransactionTemplate chạy callback thật
        when(transactionTemplate.execute(any(TransactionCallback.class)))
                .thenAnswer(
                        invocation -> {
                            TransactionCallback<OrderResponse> callback = invocation.getArgument(0);
                            return callback.doInTransaction(null);
                        });

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(0, BigDecimal.valueOf(200_000).compareTo(response.getTotalPrice()));
        assertEquals(0, BigDecimal.valueOf(200_000).compareTo(response.getFinalPrice()));
        assertNull(response.getVoucher());
    }

    @Test
    @DisplayName("TC3: Tạo hóa đơn thành công có voucher với typeOfRule là PERCENTAGE")
    public void createOrder_hasVoucher_typeOfRulePercentage_success() {
        User user = User.builder().id(1).username("huy123").password("123456").build();

        Product product1 = Product.builder().id(1).price(BigDecimal.valueOf(200000)).build();

        Product product2 = Product.builder().id(2).price(BigDecimal.valueOf(400000)).build();

        OrderItemRequest itemRequest1 = OrderItemRequest.builder().productId(1).quantity(2).build();

        OrderItemRequest itemRequest2 = OrderItemRequest.builder().productId(2).quantity(1).build();
        List<OrderItemRequest> itemRequest = List.of(itemRequest1, itemRequest2);

        OrderRequest request =
                OrderRequest.builder().voucherId(1).orderItemRequests(itemRequest).build();

        RuleCampaign ruleCampaign =
                RuleCampaign.builder()
                        .typeOfRule(TypeOfRule.PERCENTAGE)
                        .value(BigDecimal.valueOf(30))
                        .maxDiscountValue(BigDecimal.valueOf(265000))
                        .minOrderValue(BigDecimal.valueOf(300000))
                        .build();

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

        Authentication authentication = new UsernamePasswordAuthenticationToken("huy123", "123456");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userService.findByUsername("huy123")).thenReturn(user);
        when(voucherRepository.findById(1)).thenReturn(Optional.of(voucher));
        when(productRepository.findById(1)).thenReturn(Optional.of(product1));
        when(productRepository.findById(2)).thenReturn(Optional.of(product2));
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

    @Test
    @DisplayName("TC4: Tạo hóa đơn thành công có voucher với typeOfRule là FIXED_AMOUNT")
    public void createOrder_hasVoucher_typeOfRuleFixedMount_success() {
        User user = User.builder().id(1).username("huy123").password("123456").build();

        Product product1 = Product.builder().id(1).price(BigDecimal.valueOf(200000)).build();

        Product product2 = Product.builder().id(2).price(BigDecimal.valueOf(400000)).build();

        OrderItemRequest itemRequest1 = OrderItemRequest.builder().productId(1).quantity(2).build();

        OrderItemRequest itemRequest2 = OrderItemRequest.builder().productId(2).quantity(1).build();
        List<OrderItemRequest> itemRequest = List.of(itemRequest1, itemRequest2);

        OrderRequest request =
                OrderRequest.builder().voucherId(1).orderItemRequests(itemRequest).build();

        RuleCampaign ruleCampaign =
                RuleCampaign.builder()
                        .id(1)
                        .value(BigDecimal.valueOf(70000))
                        .minOrderValue(BigDecimal.valueOf(300000))
                        .typeOfRule(TypeOfRule.FIXED_AMOUNT)
                        .build();

        Voucher voucher =
                Voucher.builder()
                        .id(1)
                        .ruleCampaign(ruleCampaign)
                        .status(VoucherStatus.ACTIVE)
                        .quantityRemain(10)
                        .build();

        UserVoucher userVoucher =
                UserVoucher.builder()
                        .user(user)
                        .voucher(voucher)
                        .status(UserVoucherStatus.UNUSED)
                        .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken("huy123", "123456");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userService.findByUsername("huy123")).thenReturn(user);
        when(voucherRepository.findById(1)).thenReturn(Optional.of(voucher));
        when(productRepository.findById(1)).thenReturn(Optional.of(product1));
        when(productRepository.findById(2)).thenReturn(Optional.of(product2));
        when(distributedLockService.tryLock(eq("lock:voucher:1"), anyString(), eq(10L)))
                .thenReturn(true);
        when(userVoucherRepository.findByUserIdAndVoucherId(1, 1))
                .thenReturn(Optional.of(userVoucher));
        when(transactionTemplate.execute(any(TransactionCallback.class)))
                .thenAnswer(
                        invocation -> {
                            TransactionCallback<OrderResponse> callback = invocation.getArgument(0);
                            return callback.doInTransaction(null);
                        });
        OrderResponse response = orderService.createOrder(request);
        assertEquals(response.getVoucher().getQuantityRemain(), 9);
        assertEquals(0, response.getTotalPrice().compareTo(BigDecimal.valueOf(800000)));
        assertEquals(0, response.getFinalPrice().compareTo(BigDecimal.valueOf(730000)));
        assertNotNull(response);
    }
}
