package group2d.promo_graud.modules.auth;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import group2d.promo_graud.modules.auth.dto.request.AuthenticationRequest;
import group2d.promo_graud.modules.auth.dto.request.IntrospectRequest;
import group2d.promo_graud.modules.auth.dto.request.LogoutRequest;
import group2d.promo_graud.modules.auth.dto.response.AuthenticationResponse;
import group2d.promo_graud.modules.auth.dto.response.IntrospectResponse;
import group2d.promo_graud.modules.user.entity.User;
import group2d.promo_graud.modules.user.error.UserErrorCode;
import group2d.promo_graud.modules.user.repository.UserRepository;
import group2d.promo_graud.shared.exception.AppException;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.secret}")
    protected String secret;

    @Value("${jwt.refresh-secret}")
    protected String refresSecret;

    @Value("${jwt.valid-duration}")
    protected Long validDuration;

    @Value("${jwt.refresh-duration}")
    protected Long refreshDuration;

    public AuthenticationResponse login(AuthenticationRequest request) {
        User user =
                userRepository
                        .findByUsername(request.getUsername())
                        .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_EXISTS));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!matches) {
            throw new AppException(UserErrorCode.USER_NOT_EXISTS);
        }
        String token = generateToken(user, false);
        String refreshToken = generateToken(user, true);
        return AuthenticationResponse.builder()
                .valid(true)
                .token(token)
                .refreshToken(refreshToken)
                .build();
    }

    public void logout(LogoutRequest request) throws JOSEException, ParseException {
        try {
            SignedJWT signedJWT = verifyToken(request.getToken(), false);
            String jti = signedJWT.getJWTClaimsSet().getJWTID();
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            long ttl = expirationTime.getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                String key = "invalidated_token:" + jti;
                redisTemplate.opsForValue().set(key, "1", ttl, TimeUnit.MILLISECONDS);
            }
        } catch (AppException e) {
            log.info("Token is expired");
        }

        try {
            SignedJWT signedJWT = verifyToken(request.getRefreshToken(), true);
            String jti = signedJWT.getJWTClaimsSet().getJWTID();
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            long ttl = expirationTime.getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                String key = "invalidated_token:" + jti;
                redisTemplate.opsForValue().set(key, "1", ttl, TimeUnit.MILLISECONDS);
            }
        } catch (AppException e) {
            log.info("Token is expired");
        }
    }

    public IntrospectResponse introspect(IntrospectRequest request)
            throws ParseException, JOSEException {
        boolean isValid = true;
        try {
            verifyToken(request.getToken(), false);
        } catch (AppException e) {
            isValid = false;
        }
        return IntrospectResponse.builder().valid(isValid).build();
    }

    public AuthenticationResponse refreshToken(String refreshToken)
            throws ParseException, JOSEException {
        SignedJWT signedJwt = verifyToken(refreshToken, true);
        String jti = signedJwt.getJWTClaimsSet().getJWTID();
        Date expirationTime = signedJwt.getJWTClaimsSet().getExpirationTime();
        long ttl = expirationTime.getTime() - System.currentTimeMillis();
        if (ttl > 0) {
            String key = "invalidated_token:" + jti;
            redisTemplate.opsForValue().set(key, "1", ttl, TimeUnit.MILLISECONDS);
        }
        String username = signedJwt.getJWTClaimsSet().getSubject();
        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_EXISTS));
        String token = generateToken(user, false);
        return AuthenticationResponse.builder().valid(true).token(token).build();
    }

    public SignedJWT verifyToken(String token, boolean isRefresh)
            throws JOSEException, ParseException {
        JWSVerifier verifier =
                isRefresh
                        ? new MACVerifier(refresSecret.getBytes())
                        : new MACVerifier(secret.getBytes());
        SignedJWT signedJwt = SignedJWT.parse(token);
        Date expirationTime = signedJwt.getJWTClaimsSet().getExpirationTime();
        boolean valid = signedJwt.verify(verifier);
        if (!valid) {
            throw new AppException(AuthErrorCode.INVALID_TOKEN);
        }
        if (!expirationTime.after(new Date())) {
            throw new AppException(AuthErrorCode.TOKEN_EXPIRED);
        }
        String jti = signedJwt.getJWTClaimsSet().getJWTID();
        String key = "invalidated_token:" + jti;
        boolean invalidated = redisTemplate.hasKey(key);
        if (invalidated) {
            throw new AppException(AuthErrorCode.TOKEN_INVALIDATED);
        }
        return signedJwt;
    }

    public String generateToken(User user, boolean isFresh) {
        Date expirationTime =
                isFresh
                        ? new Date(
                                Instant.now()
                                        .plus(refreshDuration, ChronoUnit.SECONDS)
                                        .toEpochMilli())
                        : new Date(
                                Instant.now()
                                        .plus(validDuration, ChronoUnit.SECONDS)
                                        .toEpochMilli());
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet =
                new JWTClaimsSet.Builder()
                        .subject(user.getUsername())
                        .issuer("ahuy.com")
                        .issueTime(new Date())
                        .expirationTime(expirationTime)
                        .jwtID(UUID.randomUUID().toString())
                        .claim("scope", buildScope(user))
                        .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);
        try {
            if (isFresh) {
                jwsObject.sign(new MACSigner(refresSecret.getBytes()));
            } else {
                jwsObject.sign(new MACSigner(secret.getBytes()));
            }
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner("");
        if (user.getRole() != null) {
            stringJoiner.add("ROLE_" + user.getRole().name());
        }
        return stringJoiner.toString();
    }
}
