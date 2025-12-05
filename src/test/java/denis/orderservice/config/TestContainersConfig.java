// Файл: denis.orderservice.config.TestContainersConfig.java

package denis.orderservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration
public class TestContainersConfig {

    @Bean
    @ServiceConnection // Автоматически подключает контейнер к DataSource
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>("postgres:13")
                .withDatabaseName("test_db")
                .withUsername("testuser")
                .withPassword("testpass");
    }
}