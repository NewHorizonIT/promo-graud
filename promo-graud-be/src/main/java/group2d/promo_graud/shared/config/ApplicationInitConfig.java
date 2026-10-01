package group2d.promo_graud.shared.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import group2d.promo_graud.modules.user.entity.User;
import group2d.promo_graud.modules.user.enums.RoleEnum;
import group2d.promo_graud.modules.user.repository.UserRepository;

@Configuration
public class ApplicationInitConfig {
    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;

    public ApplicationInitConfig(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Bean
    public ApplicationRunner applicationRunner() {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                User user =
                        User.builder()
                                .username("admin")
                                .role(RoleEnum.ADMIN)
                                .email("hoanganhhuy200499@gmail.com")
                                .password(passwordEncoder.encode("123456"))
                                .build();
                userRepository.save(user);
            }
        };
    }
}
