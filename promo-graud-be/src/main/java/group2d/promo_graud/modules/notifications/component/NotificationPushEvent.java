package group2d.promo_graud.modules.notifications.component;

import java.util.List;

import group2d.promo_graud.modules.notifications.NotificationResponse;

public record NotificationPushEvent(List<NotificationResponse> recipients) {}
