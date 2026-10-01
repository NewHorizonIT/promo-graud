package group2d.promo_graud.modules.auth.component;

import java.text.ParseException;
import java.util.Objects;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.auth.AuthenticationService;
import group2d.promo_graud.modules.auth.dto.request.IntrospectRequest;

// class implementation của JwtDecoder để Spring Security kiểm tra tính hop le của token
@Component
@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder {
    private final AuthenticationService authenticationService;

    @Value("${jwt.secret}")
    protected String secret;

    private NimbusJwtDecoder nimbusJwtDecoder = null;

    @Override
    public Jwt decode(String token) {
        // Kiểm tra token theo logic nghiệp vụ riêng của project (token có bị sửa đổi, hết hạn, đã
        // logout chưa)
        try {
            var response =
                    authenticationService.introspect(
                            IntrospectRequest.builder().token(token).build());
            if (!response.isValid()) {
                throw new JwtException("Invalid token");
            }
        } catch (JOSEException | ParseException e) {
            throw new JwtException(e.getMessage());
        }

        // dùng đối tượng nimbus (implementation của JwtDecoder) để decode token có hợp lệ hay ko
        if (Objects.isNull(nimbusJwtDecoder)) {
            // Tạo SecretKey từ JWT secret và project sử dụng thuật toán HMAC SHA-512.
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HS512");

            // Tạo NimbusJwtDecoder sử dụng secret key và thuật toán HS512 để verify JWT
            nimbusJwtDecoder =
                    NimbusJwtDecoder.withSecretKey(secretKey)
                            .macAlgorithm(MacAlgorithm.HS512)
                            .build();
        }
        return nimbusJwtDecoder.decode(token);
    }
}
