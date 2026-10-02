package group2d.promo_graud.modules.auth;

import java.text.ParseException;
import java.time.Duration;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nimbusds.jose.JOSEException;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.auth.dto.request.AuthenticationRequest;
import group2d.promo_graud.modules.auth.dto.request.IntrospectRequest;
import group2d.promo_graud.modules.auth.dto.request.LogoutRequest;
import group2d.promo_graud.modules.auth.dto.response.AuthenticationResponse;
import group2d.promo_graud.modules.auth.dto.response.IntrospectResponse;
import group2d.promo_graud.shared.dto.ApiResponse;
import group2d.promo_graud.shared.exception.AppException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
            @Valid @RequestBody AuthenticationRequest request) {
        AuthenticationResponse authenticationResponse = authenticationService.login(request);

        ResponseCookie cookie =
                ResponseCookie.from("refreshToken", authenticationResponse.getRefreshToken())
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .maxAge(Duration.ofDays(7))
                        .sameSite("Strict")
                        .build();

        AuthenticationResponse response = AuthenticationResponse.builder()
          .valid(true)
          .token(authenticationResponse.getToken())
          .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(
                        ApiResponse.<AuthenticationResponse>builder()
                                .code(201)
                                .message("Login thành công")
                                .result(response)
                                .build());
    }

    @PostMapping("/introspect")
    public ResponseEntity<ApiResponse<IntrospectResponse>> introspect(
            @Valid @RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        IntrospectResponse introspectResponse = authenticationService.introspect(request);
        return ResponseEntity.ok(
                ApiResponse.<IntrospectResponse>builder()
                        .code(201)
                        .message("Kiểm tra token thành công")
                        .result(introspectResponse)
                        .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody IntrospectRequest request,
            @CookieValue(value = "refreshToken", required = false) String refreshToken)
            throws ParseException, JOSEException {

        LogoutRequest logoutRequest =
                LogoutRequest.builder()
                        .token(request.getToken())
                        .refreshToken(refreshToken)
                        .build();

        authenticationService.logout(logoutRequest);

        // Xóa Refresh Token khỏi Cookie
        ResponseCookie deleteCookie =
                ResponseCookie.from("refreshToken", "")
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .maxAge(0)
                        .sameSite("Strict")
                        .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(ApiResponse.<Void>builder().code(201).message("Logout thành công").build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken)
            throws ParseException, JOSEException {
        if (refreshToken == null) {
            throw new AppException(AuthErrorCode.UNAUTHENTICATED);
        }

        AuthenticationResponse authenticationResponse =
                authenticationService.refreshToken(refreshToken);

        // Cập nhật lại Refresh Token vào Cookie
        ResponseCookie cookie =
                ResponseCookie.from("refreshToken", authenticationResponse.getRefreshToken())
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .maxAge(Duration.ofDays(7))
                        .sameSite("Strict")
                        .build();

      AuthenticationResponse response = AuthenticationResponse.builder()
        .token(authenticationResponse.getToken())
        .valid(true)
        .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(
                        ApiResponse.<AuthenticationResponse>builder()
                                .code(201)
                                .message("Refresh token thành công")
                                .result(response)
                                .build());
    }
}
