package group2d.promo_graud.modules.orders.dto.response;

import lombok.Builder;
import lombok.Getter;

import group2d.promo_graud.modules.user.entity.User;

@Getter
@Builder
public class UserSummaryResponse {
    private Integer id;
    private String username;
    private String email;

    public static UserSummaryResponse fromEntity(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}
