package group2d.promo_graud.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    @Bean
    public RestClient webhookRestClient() {
        // Cấu hình Factory để ép Timeout
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000); // 3 giây không kết nối được -> (Ném lỗi)
        factory.setReadTimeout(5000); // 5 giây bên kia không trả lời -> (Ném lỗi)

        return RestClient.builder()
                .requestFactory(factory)
                .defaultHeader("Content-Type", "application/json") // Định dạng chuẩn cho Webhook
                .build();
    }
}
