package group2d.promo_graud.modules.auth;

import java.text.ParseException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.nimbusds.jose.JOSEException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import group2d.promo_graud.modules.auth.dto.request.AuthenticationRequest;
import group2d.promo_graud.modules.auth.dto.request.IntrospectRequest;
import group2d.promo_graud.modules.auth.dto.response.AuthenticationResponse;
import group2d.promo_graud.modules.auth.dto.response.IntrospectResponse;
import group2d.promo_graud.modules.user.entity.User;
import group2d.promo_graud.modules.user.enums.RoleEnum;
import group2d.promo_graud.modules.user.repository.UserRepository;
import group2d.promo_graud.shared.exception.AppException;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock private RedisTemplate<String, String> redisTemplate;

    @Mock private UserRepository userRepository;

    @InjectMocks private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService.secret =
                "20b84f35360762f63e3f9a00d82ea614b53a207dbb842ada7cd869e35e214a94";
        authenticationService.refresSecret =
                "20b84f35360762f63e3f9a00d82ea614b53a207dbb842ada7cd869e35dsadsadsaPh";
        authenticationService.validDuration = 7200L;
        authenticationService.refreshDuration = 72000L;
    }

    @Test
    @DisplayName("TC1: Login thành cong")
    public void loginSuccess() {
        AuthenticationRequest request =
                AuthenticationRequest.builder().username("admin").password("123456").build();
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        User user =
                User.builder()
                        .id(1)
                        .email("ahuy200499@gmail.com")
                        .username("admin")
                        .password(passwordEncoder.encode("123456"))
                        .build();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        AuthenticationResponse response = authenticationService.login(request);
        assertNotNull(response.getToken());
        assertTrue(response.isValid());
    }

    @Test
    @DisplayName("TC2: Login không thành công vì không tìm thấy user")
    public void loginFailed() {
        AuthenticationRequest request =
                AuthenticationRequest.builder().username("admin").password("123456").build();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        assertThrows(AppException.class, () -> authenticationService.login(request));
    }

    @Test
    @DisplayName("TC3: Login không thành công vì password sai")
    public void loginFailPassWrong() {
        AuthenticationRequest request =
                AuthenticationRequest.builder().username("admin").password("123456").build();
        User user =
                User.builder()
                        .id(1)
                        .username("admin")
                        .email("ahuy200499@gmail.com")
                        .password("222222")
                        .build();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        assertThrows(AppException.class, () -> authenticationService.login(request));
    }

    @Test
    @DisplayName("TC4: Verify token thành công")
    public void introspectSuccess() throws ParseException, JOSEException {
        User user =
                User.builder()
                        .id(1)
                        .username("admin")
                        .email("ahuy200499@gmail.com")
                        .role(RoleEnum.ADMIN)
                        .build();
        String token = authenticationService.generateToken(user, false);
        IntrospectRequest request = IntrospectRequest.builder().token(token).build();
        IntrospectResponse response = authenticationService.introspect(request);
        assertTrue(response.isValid());
    }

    @Test
    @DisplayName("TC5: Verify token thất bại vì token không hợp lệ")
    public void introspectFailed() throws ParseException, JOSEException {
        IntrospectRequest request =
                IntrospectRequest.builder()
                        .token("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0In0.abcxyz")
                        .build();
        IntrospectResponse response = authenticationService.introspect(request);
        assertFalse(response.isValid());
    }

    @Test
    @DisplayName("TC6: Verify token thất bại vì token đã bị logout")
    public void verifyTokenLogoutFailed() {
        User user =
                User.builder()
                        .email("ahuy200499@gmail.com")
                        .username("admin")
                        .password("123456")
                        .build();
        String token = authenticationService.generateToken(user, false);
        when(redisTemplate.hasKey(anyString())).thenReturn(true);
        assertThrows(AppException.class, () -> authenticationService.verifyToken(token, false));
    }
}
