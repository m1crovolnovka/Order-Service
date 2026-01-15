package denis.orderservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import denis.orderservice.config.TestContainersConfig;
import denis.orderservice.dto.request.OrderItemRequestDto;
import denis.orderservice.dto.request.OrderRequestDto;
import denis.orderservice.dto.request.OrderUpdateRequestDto;
import denis.orderservice.dto.response.UserInfoDto;
import denis.orderservice.entity.Item;
import denis.orderservice.entity.Order;
import denis.orderservice.entity.OrderItem;
import denis.orderservice.entity.OrderStatus;
import denis.orderservice.repository.ItemRepository;
import denis.orderservice.repository.OrderItemRepository;
import denis.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
class OrderControllerIntegrationTest {

    private static final WireMockServer WIRE_MOCK_SERVER = new WireMockServer(
            WireMockConfiguration.wireMockConfig().dynamicPort()
    );

    @DynamicPropertySource
    static void setWireMockProperties(DynamicPropertyRegistry registry) {
        registry.add("client.user-service.url",
                () -> "http://localhost:" + WIRE_MOCK_SERVER.port());
    }

    @BeforeAll
    static void startWireMock() {
        WIRE_MOCK_SERVER.start();
    }

    @AfterAll
    static void stopWireMock() {
        WIRE_MOCK_SERVER.stop();
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    private final UUID TEST_USER_ID = UUID.fromString("1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d");
    private Item existingItem;

    @BeforeEach
    void setUp() {
        WIRE_MOCK_SERVER.resetAll();
        existingItem = itemRepository.save(new Item(null, "Test Product", new BigDecimal("50.00")));
    }

    @AfterEach
    void tearDown() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        itemRepository.deleteAll();
    }

    private OrderRequestDto createOrderRequestDto(UUID userId, Item item) {
        return new OrderRequestDto(
                userId,
                List.of(new OrderItemRequestDto(item.getId(), 2))
        );
    }

    private UserInfoDto createUserInfoDto(UUID id) {
        return new UserInfoDto(id, "Test", "User", "test.user@example.com");
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void createOrder_shouldReturnCreatedOrderAndCallUserService() throws Exception {
        OrderRequestDto requestDto = createOrderRequestDto(TEST_USER_ID, existingItem);
        UserInfoDto expectedUser = createUserInfoDto(TEST_USER_ID);
        String expectedUserJson = objectMapper.writeValueAsString(expectedUser);

        WIRE_MOCK_SERVER.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/api/users/" + TEST_USER_ID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(expectedUserJson)));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value(OrderStatus.CREATED.name()))
                .andExpect(jsonPath("$.user.id").value(TEST_USER_ID.toString()))
                .andExpect(jsonPath("$.totalPrice").value(100.00));

        WIRE_MOCK_SERVER.verify(1, getRequestedFor(urlEqualTo("/api/users/" + TEST_USER_ID)));
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void getOrderById_shouldReturnOrderAndCallUserService() throws Exception {
        UserInfoDto expectedUser = createUserInfoDto(TEST_USER_ID);
        String expectedUserJson = objectMapper.writeValueAsString(expectedUser);

        WIRE_MOCK_SERVER.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/api/users/" + TEST_USER_ID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(expectedUserJson)));

        OrderItem orderItem = new OrderItem(null,null,existingItem, 2);
        Order orderToSave = new Order(null,TEST_USER_ID,OrderStatus.CREATED,existingItem.getPrice().multiply(BigDecimal.valueOf(2)),List.of(orderItem));
        Order savedOrder = orderRepository.save(orderToSave);
        UUID orderId = savedOrder.getId();
        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.user.name").value("Test"));

        WIRE_MOCK_SERVER.verify(1, getRequestedFor(urlEqualTo("/api/users/" + TEST_USER_ID)));
    }


    @Test
    @WithMockUser(authorities = {"USER"})
    void deleteOrder_shouldReturnNoContent() throws Exception {

        OrderItem orderItem = new OrderItem(null,null,existingItem, 2);
        Order orderToSave = new Order(null,TEST_USER_ID,OrderStatus.CREATED,existingItem.getPrice().multiply(BigDecimal.valueOf(2)),List.of(orderItem));
        Order savedOrder = orderRepository.save(orderToSave);
        UUID orderId = savedOrder.getId();

        mockMvc.perform(delete("/api/orders/{id}", orderId))
                .andExpect(status().isNoContent());

    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void updateOrder_shouldReturnUpdatedOrder() throws Exception {
        UserInfoDto expectedUser = createUserInfoDto(TEST_USER_ID);
        String expectedUserJson = objectMapper.writeValueAsString(expectedUser);

        WIRE_MOCK_SERVER.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/api/users/" + TEST_USER_ID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(expectedUserJson)));

        OrderItem orderItem = new OrderItem(null,null,existingItem, 2);
        Order orderToSave = new Order(null,TEST_USER_ID,OrderStatus.CREATED,existingItem.getPrice().multiply(BigDecimal.valueOf(2)),List.of(orderItem));
        Order savedOrder = orderRepository.save(orderToSave);
        UUID orderId = savedOrder.getId();

        OrderUpdateRequestDto updateDto = new OrderUpdateRequestDto(
                List.of(new OrderItemRequestDto(existingItem.getId(), 5)),
                OrderStatus.PROCESSING
        );

        mockMvc.perform(put("/api/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value(OrderStatus.PROCESSING.name()))
                .andExpect(jsonPath("$.totalPrice").value(250.00));
        WIRE_MOCK_SERVER.verify(1, getRequestedFor(urlEqualTo("/api/users/" + TEST_USER_ID)));
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void getOrdersByUserId_shouldReturnPageOfOrders() throws Exception {
        UserInfoDto expectedUser = createUserInfoDto(TEST_USER_ID);
        String expectedUserJson = objectMapper.writeValueAsString(expectedUser);

        WIRE_MOCK_SERVER.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/api/users/" + TEST_USER_ID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(expectedUserJson)));

        OrderItem orderItem1 = new OrderItem(null,null,existingItem, 2);
        Order orderToSave1 = new Order(null,TEST_USER_ID,OrderStatus.CREATED,existingItem.getPrice().multiply(BigDecimal.valueOf(2)),List.of(orderItem1));
        orderRepository.save(orderToSave1);
        OrderItem orderItem2 = new OrderItem(null,null,existingItem, 2);
        Order orderToSave2 = new Order(null,TEST_USER_ID,OrderStatus.CREATED,existingItem.getPrice().multiply(BigDecimal.valueOf(2)),List.of(orderItem2));
        orderRepository.save(orderToSave2);

        mockMvc.perform(get("/api/orders/user/{userId}", TEST_USER_ID)
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].user.name").value("Test"));
        WIRE_MOCK_SERVER.verify(1, getRequestedFor(urlEqualTo("/api/users/" + TEST_USER_ID)));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void getAllOrders_shouldReturnFilteredPage() throws Exception {

        UserInfoDto expectedUser = createUserInfoDto(TEST_USER_ID);
        String expectedUserJson = objectMapper.writeValueAsString(expectedUser);
        WIRE_MOCK_SERVER.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo("/api/users/" + TEST_USER_ID))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(expectedUserJson)));

        OrderItem orderItem1 = new OrderItem(null,null,existingItem, 2);
        Order orderToSave1 = new Order(null,TEST_USER_ID,OrderStatus.CREATED,existingItem.getPrice().multiply(BigDecimal.valueOf(2)),List.of(orderItem1));
        orderRepository.save(orderToSave1);
        OrderItem orderItem2 = new OrderItem(null,null,existingItem, 2);
        Order orderToSave2 = new Order(null,TEST_USER_ID,OrderStatus.CREATED,existingItem.getPrice().multiply(BigDecimal.valueOf(2)),List.of(orderItem2));
        orderRepository.save(orderToSave2);

        mockMvc.perform(get("/api/orders")
                        .param("statuses", OrderStatus.CREATED.name())
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].status").value(OrderStatus.CREATED.name()));
        mockMvc.perform(get("/api/orders")
                        .param("statuses", OrderStatus.PROCESSING.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
        WIRE_MOCK_SERVER.verify(2, getRequestedFor(urlEqualTo("/api/users/" + TEST_USER_ID)));
    }
}
