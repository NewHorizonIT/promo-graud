package group2d.promo_graud.shared.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import group2d.promo_graud.modules.auth.component.CustomJwtDecoder;
import group2d.promo_graud.modules.auth.component.JwtAuthenticationEntryPoint;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final String[] publicEndpoints = {
        "/api/v1/users",
        "/api/v1/auth/login",
        "/api/v1/auth/introspect",
        "/api/v1/auth/logout",
        "/api/v1/auth/refresh"
    };
    private final String[] publicEndpointsGet = {
        "/api/v1/vouchers", "/api/v1/products", "/api/v1/products/type"
    };
    private CustomJwtDecoder customJwtDecoder;

    public SecurityConfig(CustomJwtDecoder customJwtDecoder) {
        this.customJwtDecoder = customJwtDecoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
        httpSecurity.authorizeHttpRequests(
                request ->
                        request.requestMatchers(HttpMethod.POST, publicEndpoints)
                                .permitAll()
                                .requestMatchers(HttpMethod.GET, publicEndpointsGet)
                                .permitAll()
                                .anyRequest()
                                .authenticated());

        // dùng oauth2ResourceServer cấu hình ứng dụng hoạt động (mọi request gửi tới đều phải đính
        // kèm jwt)
        httpSecurity.oauth2ResourceServer(
                oauth2 ->
                        oauth2
                                // Cấu hình cách Spring Security xử lý JWT
                                .jwt(
                                        jwtConfigurer ->
                                                jwtConfigurer
                                                        // Chỉ định interface JwtDecoder dùng để
                                                        // verify JWT ( JwtAuthenticationProvider
                                                        // nhận token và gọi decode của
                                                        // customJwtDecoder(token) )
                                                        .decoder(customJwtDecoder)
                                                        // Sau khi JWT hợp lệ và được decode thành
                                                        // công, dùng converter này để chuyển Jwt ->
                                                        // Authentication và đưa SecurityContext
                                                        // quản lý
                                                        .jwtAuthenticationConverter(
                                                                jwtAuthenticationConverter()))
                                // Nếu authentication thất bại (ví dụ token không hợp lệ trả HTTP
                                // 401 Unauthorized
                                .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        httpSecurity.cors(Customizer.withDefaults());
        return httpSecurity.build();
    }

    // Cấu hình cách chuyển các quyền (authorities) từ JWT thành GrantedAuthority của Spring
    // Security.
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
                new JwtGrantedAuthoritiesConverter();
        // Không thêm prefix mặc định "SCOPE_" vào authority.
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
                jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    // Đăng ký PasswordEncoder để mã hóa và kiểm tra mật khẩu.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
