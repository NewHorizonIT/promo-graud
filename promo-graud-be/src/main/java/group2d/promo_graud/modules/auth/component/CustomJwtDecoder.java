package group2d.promo_graud.modules.auth.component;

import java.text.ParseException;

import javax.crypto.spec.SecretKeySpec;

import jakarta.annotation.PostConstruct;

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

    private NimbusJwtDecoder nimbusJwtDecoder;

    @PostConstruct
    public void init() {
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HS512");
        nimbusJwtDecoder =
                NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS512).build();
    }

    @Override
    public Jwt decode(String token) {
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

        return nimbusJwtDecoder.decode(token);
    }
}
