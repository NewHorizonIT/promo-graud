package group2d.promo_graud.modules.notifications;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import group2d.promo_graud.modules.user.error.UserErrorCode;
import group2d.promo_graud.modules.user.repository.UserRepository;
import group2d.promo_graud.shared.dto.ApiResponse;
import group2d.promo_graud.shared.exception.AppException;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    // 1. Lấy 20 thông báo gần nhất của user/admin đang đăng nhập
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications() {
        Integer userId = getCurrentUserId();
        return ResponseEntity.ok(
                ApiResponse.<List<NotificationResponse>>builder()
                        .result(notificationService.getMyNotifications(userId))
                        .build());
    }

    // 2. Đếm số lượng thông báo chưa đọc để hiện badge đỏ trên icon Bell
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        Integer userId = getCurrentUserId();
        return ResponseEntity.ok(
                ApiResponse.<Long>builder()
                        .result(notificationService.countUnread(userId))
                        .build());
    }

    // 3. Đánh dấu 1 thông báo là đã đọc (id ở đây là id của bảng notification_recipients)
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Boolean>> markAsRead(@PathVariable Integer id) {
        Integer userId = getCurrentUserId();
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(
                ApiResponse.<Boolean>builder().message("Đã đánh dấu đã đọc").result(true).build());
    }

    // 4. Đánh dấu tất cả là đã đọc
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Boolean>> markAllAsRead() {
        Integer userId = getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(
                ApiResponse.<Boolean>builder()
                        .message("Đã đánh dấu đọc tất cả thông báo")
                        .result(true)
                        .build());
    }

    // 5. Xóa mềm 1 thông báo khỏi danh sách của user
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteNotification(@PathVariable Integer id) {
        Integer userId = getCurrentUserId();
        notificationService.delete(id, userId);
        return ResponseEntity.ok(
                ApiResponse.<Boolean>builder().message("Đã xóa thông báo").result(true).build());
    }

    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            Long userId = jwt.getClaim("userId");
            if (userId != null) {
                return userId.intValue();
            }
        }
        return userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_EXISTS))
                .getId();
    }
}
