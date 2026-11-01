package group2d.promo_graud.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import group2d.promo_graud.modules.user.entity.User;
import group2d.promo_graud.modules.user.enums.RoleEnum;
import group2d.promo_graud.modules.user.repository.UserRepository;

@Configuration
public class ApplicationInitConfig {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.email}")
    private String adminEmail;

    public ApplicationInitConfig(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Bean
    public ApplicationRunner applicationRunner() {
        return args -> {
            if (!userRepository.existsByUsername(adminUsername)) {
                User user =
                        User.builder()
                                .username(adminUsername)
                                .role(RoleEnum.ADMIN)
                                .email(adminEmail)
                                .password(passwordEncoder.encode(adminPassword))
                                .build();
                userRepository.save(user);
            }
        };
    }
}
