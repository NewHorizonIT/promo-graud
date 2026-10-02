package group2d.promo_graud.modules.auth.component;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import group2d.promo_graud.modules.auth.AuthErrorCode;
import group2d.promo_graud.shared.dto.ApiResponse;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // Method này được Spring Security gọi khi request không được xác thực hoặc authentication thất
    // bại.
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException, ServletException {
        AuthErrorCode errorCode = AuthErrorCode.UNAUTHENTICATED;
        response.setStatus(errorCode.getStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<?> apiRespone =
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build();
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(apiRespone));
        response.flushBuffer();
    }
}
