package group2d.promo_graud.modules.vnpay;

import java.net.URI;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.shared.dto.ApiResponse;

@RestController
@RequestMapping("/api/v1/payments/vnpay")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping("/create-url")
    public ResponseEntity<ApiResponse> createPaymentUrl(
            @RequestParam Integer orderId, HttpServletRequest request) {

        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isBlank()) {
            ipAddress = request.getRemoteAddr();
        } else {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        String paymentUrl = paymentService.createPaymentUrl(orderId, ipAddress);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .code(200)
                        .message("Create VNPay URL success")
                        .result(Map.of("url", paymentUrl))
                        .build());
    }

    @GetMapping("/ipn")
    public ResponseEntity<?> ipnHandler(@RequestParam Map<String, String> queryParams) {
        // IPN handler do VNPay gọi nên trả thẳng Map JSON thay vì bọc trong ApiResponse
        // để VNPay đọc được định dạng chuẩn của họ.
        Map<String, String> response = paymentService.processIpn(queryParams);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/return")
    public ResponseEntity<?> returnHandler(
            HttpServletRequest request, @RequestParam Map<String, String> queryParams) {
        String orderId = queryParams.get("vnp_TxnRef");
        String responseCode = queryParams.get("vnp_ResponseCode");

        // Redirect về Frontend hiển thị UI
        String feRedirectUrl =
                "http://localhost:5173/payment-result?orderId=" + orderId + "&code=" + responseCode;

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(feRedirectUrl)).build();
    }
}
