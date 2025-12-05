//package denis.orderservice.integration;
//
//import org.junit.jupiter.api.Test;
//import org.testcontainers.containers.PostgreSQLContainer;
//
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//class MinimalTest {
//
//    @Test
//    void testDockerCanRunContainer() {
//        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")) {
//
//            // Запускаем контейнер напрямую, без Spring и @ServiceConnection
//            postgres.start();
//
//            assertTrue(postgres.isRunning(), "Контейнер PostgreSQL должен быть запущен.");
//            System.out.println("Docker успешно запущен по URL: " + postgres.getJdbcUrl());
//
//        } catch (Exception e) {
//            // Выводим сообщение, если тест упал
//            throw new RuntimeException("Не удалось запустить контейнер, возможно, проблема с Docker.", e);
//        }
//    }
//}