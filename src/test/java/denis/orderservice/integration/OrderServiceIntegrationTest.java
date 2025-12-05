package denis.orderservice.integration;

//не подключаются testContainers к docker


import com.fasterxml.jackson.databind.ObjectMapper;
import denis.orderservice.OrderServiceApplication;
import denis.orderservice.client.UserServiceClient; // 💡 Предположим, это ваш Feign-клиент
import denis.orderservice.config.TestContainersConfig;
import denis.orderservice.dto.request.OrderItemRequestDto;
import denis.orderservice.dto.request.OrderRequestDto;
import denis.orderservice.dto.response.OrderResponseDto;
import denis.orderservice.dto.response.UserInfoDto;
import denis.orderservice.entity.Item;
import denis.orderservice.repository.ItemRepository;
import denis.orderservice.repository.OrderRepository;
import denis.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*; // 💡 ИМПОРТ MOCKITO
//
//@SpringBootTest(
//        classes = OrderServiceApplication.class,
//        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
//        // Добавляем обязательное свойство для запуска приложения (JWT)
//        properties = {
//                "jwt.secret.key=this-is-a-very-long-and-secure-secret-key-for-testing-purposes-1234567890",
//                // Поскольку Feign теперь мокируется, нам не нужно указывать его URL
//                "client.user-service.url=http://ignored-by-mockbean"
//        }
//)
//@Testcontainers
//@Import(TestContainersConfig.class)
class OrderServiceIntegrationTest {

//    // 💡 МОКИРОВАНИЕ: Объявляем Feign-клиента как MockBean.
//    // Это заменяет реальный бин UserClient мок-версией в Spring Context.
//    @MockBean
//    private UserServiceClient userClient;
//
//    @Autowired
//    OrderService orderService;
//
//    @Autowired
//    ItemRepository itemRepository;
//
//    @Autowired
//    ObjectMapper objectMapper;
//
//    @Autowired
//    OrderRepository orderRepository;
//
//    @BeforeEach
//    void setup() {
//        orderRepository.deleteAll();
//        itemRepository.deleteAll();
//
//        itemRepository.save(Item.builder()
//                .id(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
//                .name("Test")
//                .price(new BigDecimal("50.00"))
//                .build());
//    }
//
//    @Test
//    void contextLoads() {
//        // Проверяем, что контекст Spring Boot успешно загрузился с PostgreSQL и моками.
//        assertTrue(true, "Spring Boot Context should load successfully with PostgreSQL container and MockBeans.");
//    }
//
//    @Test
//    void createOrder_success_with_mockito() throws Exception {
//        UUID userId = UUID.randomUUID();
//
//        // 1. Создаем DTO, которое должен вернуть мок-сервис
//        UserInfoDto mockUserResponse = UserInfoDto.builder()
//                .id(userId)
//                .name("John")
//                .surname("Tester")
//                .email("john@test.com")
//                .build();
//
//        // 2. ОПРЕДЕЛЯЕМ ПОВЕДЕНИЕ МОКА (Mockito.when)
//        // Когда OrderService вызывает userClient.getUserById(любой UUID),
//        // мок должен вернуть mockUserResponse.
//        when(userClient.getUserById(any(UUID.class)))
//                .thenReturn(mockUserResponse);
//
//        // 3. Выполняем тестируемую операцию
//        OrderRequestDto request = new OrderRequestDto(
//                userId,
//                List.of(new OrderItemRequestDto(
//                        UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"), 2))
//        );
//
//        OrderResponseDto response = orderService.create(request);
//
//        // 4. Проверки
//        assertNotNull(response);
//        assertEquals("john@test.com", response.user().email());
//        assertEquals(new BigDecimal("100.00"), response.totalPrice());
//
//        // 5. Проверяем, что мок-метод был вызван (Mockito.verify)
//        verify(userClient, times(1)).getUserById(userId);
//    }
}