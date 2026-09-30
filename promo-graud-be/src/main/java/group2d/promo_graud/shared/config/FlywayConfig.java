package group2d.promo_graud.shared.config;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
      Flyway flyway = Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .load();

      flyway.repair();   // TẠM THỜI: cập nhật lại checksum V1 trong DB
      flyway.migrate();

      return flyway;
    }

    @Bean
    public static BeanFactoryPostProcessor jpaFlywayOrderPostProcessor() {
        return factory -> {
            if (factory.containsBeanDefinition("entityManagerFactory")) {
                factory.getBeanDefinition("entityManagerFactory").setDependsOn("flyway");
            }
        };
    }
}
