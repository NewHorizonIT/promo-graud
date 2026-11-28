package group2d.promo_graud.modules.notifications.component;

import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.auth.AuthErrorCode;
import group2d.promo_graud.modules.notifications.NotificationErrorCode;
import group2d.promo_graud.modules.user.entity.User;
import group2d.promo_graud.modules.user.error.UserErrorCode;
import group2d.promo_graud.modules.user.repository.UserRepository;
import group2d.promo_graud.shared.exception.AppException;

@RequiredArgsConstructor
public class JwtStompChannelInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        // Xác thực khi client kết nối STOMP
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authorization = accessor.getFirstNativeHeader("Authorization");

            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new AppException(AuthErrorCode.INVALID_TOKEN);
            }

            String token = authorization.substring(7);

            // JwtDecoder kiểm tra chữ ký và thời hạn token
            Jwt jwt = jwtDecoder.decode(token);

            String username = jwt.getSubject();

            User user =
                    userRepository
                            .findByUsername(username)
                            .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_EXISTS));

            // Principal name là userId để gửi notification theo userId
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            String.valueOf(user.getId()), null, List.of());

            accessor.setUser(authentication);
        }

        // Client chỉ được subscribe private notification queue
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

            String destination = accessor.getDestination();

            if (!"/user/queue/notifications".equals(destination)) {
                throw new AppException(NotificationErrorCode.NOTIFICATION_NOT_REACH);
            }
        }

        // Client không được tự gửi message vào server
        if (StompCommand.SEND.equals(accessor.getCommand())) {
            throw new AppException(NotificationErrorCode.NOTIFICATION_CLIENT_NOT_REACH);
        }

        return message;
    }
}
