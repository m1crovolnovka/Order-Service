package denis.orderservice.service.impl;

import denis.orderservice.client.UserServiceClient;
import denis.orderservice.dto.request.OrderItemRequestDto;
import denis.orderservice.dto.request.OrderRequestDto;
import denis.orderservice.dto.request.OrderUpdateRequestDto;
import denis.orderservice.dto.response.OrderResponseDto;
import denis.orderservice.dto.response.UserInfoDto;
import denis.orderservice.entity.Item;
import denis.orderservice.entity.Order;
import denis.orderservice.entity.OrderItem;
import denis.orderservice.entity.OrderStatus;
import denis.orderservice.exception.ItemNotFoundException;
import denis.orderservice.exception.OrderNotFoundException;
import denis.orderservice.mapper.OrderMapper;
import denis.orderservice.repository.ItemRepository;
import denis.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private UserServiceClient userClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    private final UUID orderId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID itemId = UUID.randomUUID();
    private Item mockItem;
    private UserInfoDto mockUser;
    private Order mockOrder;

    @BeforeEach
    void setUp() {
        mockItem = Item.builder()
                .id(itemId)
                .price(new BigDecimal("100.00"))
                .name("Test Item")
                .build();

        mockUser = UserInfoDto.builder()
                .id(userId)
                .email("user@test.com")
                .name("Test")
                .build();

        mockOrder = Order.builder()
                .id(orderId)
                .userId(userId)
                .status(OrderStatus.CREATED)
                .totalPrice(new BigDecimal("200.00"))
                .items(new ArrayList<>())
                .build();
    }

    @Test
    void createSuccessful() {
        OrderItemRequestDto itemDto = new OrderItemRequestDto(itemId, 2);
        OrderRequestDto requestDto = new OrderRequestDto(userId, List.of(itemDto));
        OrderResponseDto expectedResponse = new OrderResponseDto(
                orderId, userId, "NEW", new BigDecimal("200.00"),
                null, null, Collections.emptyList(), mockUser);

        when(orderMapper.toEntity(any(OrderRequestDto.class))).thenReturn(mockOrder);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(mockItem));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        when(userClient.getUserById(userId)).thenReturn(mockUser);
        when(orderMapper.toDto(any(Order.class), any(UserInfoDto.class))).thenReturn(expectedResponse);

        OrderResponseDto actualResponse = orderService.create(requestDto);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.id(), actualResponse.id());
        assertEquals(new BigDecimal("200.00"), actualResponse.totalPrice());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createWhenItemNotFound() {
        OrderItemRequestDto itemDto = new OrderItemRequestDto(itemId, 1);
        OrderRequestDto requestDto = new OrderRequestDto(userId, List.of(itemDto));
        when(orderMapper.toEntity(any(OrderRequestDto.class))).thenReturn(mockOrder);
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());
        assertThrows(ItemNotFoundException.class, () -> orderService.create(requestDto));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getByIdWhenFound() {
        OrderResponseDto expectedResponse = new OrderResponseDto(
                orderId, userId, "NEW", new BigDecimal("200.00"),
                null, null, Collections.emptyList(), mockUser);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(userClient.getUserById(userId)).thenReturn(mockUser);
        when(orderMapper.toDto(any(Order.class), any(UserInfoDto.class))).thenReturn(expectedResponse);
        OrderResponseDto actualResponse = orderService.getById(orderId);
        assertNotNull(actualResponse);
        assertEquals(orderId, actualResponse.id());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void getByIdWhenNotFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> orderService.getById(orderId));
        verify(userClient, never()).getUserById(any());
    }

    @Test
    void updateWhenSuccessful() {
        Order initialOrder = mockOrder;
        initialOrder.getItems().add(new OrderItem());
        OrderItemRequestDto newItemDto = new OrderItemRequestDto(itemId, 1);
        OrderUpdateRequestDto updateDto = new OrderUpdateRequestDto( List.of(newItemDto),OrderStatus.PROCESSING);
        Order updatedOrder = Order.builder()
                .id(orderId)
                .userId(userId)
                .status(OrderStatus.PROCESSING)
                .totalPrice(new BigDecimal("100.00"))
                .build();
        OrderResponseDto expectedResponse = new OrderResponseDto(
                orderId, userId, "PROCESSING", new BigDecimal("100.00"),
                null, null, Collections.emptyList(), mockUser);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(initialOrder));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(mockItem));
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);
        when(userClient.getUserById(userId)).thenReturn(mockUser);
        when(orderMapper.toDto(any(Order.class), any(UserInfoDto.class))).thenReturn(expectedResponse);

        OrderResponseDto actualResponse = orderService.update(orderId, updateDto);

        assertNotNull(actualResponse);
        assertEquals(OrderStatus.PROCESSING.toString(), actualResponse.status());
        assertEquals(new BigDecimal("100.00"), actualResponse.totalPrice());
        assertEquals(1, initialOrder.getItems().size());
    }

    @Test
    void updateWhenOrderDoesNotExist() {
        OrderUpdateRequestDto updateDto = new OrderUpdateRequestDto(Collections.emptyList(), OrderStatus.PROCESSING);
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.update(orderId, updateDto));

        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateWhenNewItemDoesNotExist() {
        OrderItemRequestDto itemDto = new OrderItemRequestDto(itemId, 1);
        OrderUpdateRequestDto updateDto = new OrderUpdateRequestDto(List.of(itemDto), OrderStatus.PROCESSING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> orderService.update(orderId, updateDto));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void deleteWhenSuccessful() {
        when(orderRepository.existsById(orderId)).thenReturn(true);

        orderService.delete(orderId);

        verify(orderRepository, times(1)).deleteById(orderId);
    }

    @Test
    void deleteWhenOrderDoesNotExist() {
        when(orderRepository.existsById(orderId)).thenReturn(false);

        assertThrows(OrderNotFoundException.class, () -> orderService.delete(orderId));

        verify(orderRepository, never()).deleteById(any());
    }

    @Test
    void getByUserIdWhenSuccessful() {
        List<Order> orderList = List.of(mockOrder);
        Page<Order> mockPage = new PageImpl<>(orderList);
        when(orderRepository.findAllByUserId(eq(userId), any())).thenReturn(mockPage);
        when(userClient.getUserById(userId)).thenReturn(mockUser);
        orderService.getByUserId(userId, mock(org.springframework.data.domain.Pageable.class));

        verify(orderRepository, times(1)).findAllByUserId(eq(userId), any());
        verify(userClient, times(1)).getUserById(userId);
        verify(orderMapper, times(orderList.size())).toDto(any(Order.class), any(UserInfoDto.class));
    }

    @Test
    void getAllWithFilters() {
        LocalDateTime from = LocalDateTime.now().minusDays(7);
        LocalDateTime to = LocalDateTime.now();
        List<OrderStatus> statuses = List.of(OrderStatus.CREATED);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        List<Order> orderList = List.of(mockOrder);
        Page<Order> mockPage = new PageImpl<>(orderList, pageable, 1);
        OrderResponseDto expectedDto = new OrderResponseDto(
                orderId, userId, OrderStatus.CREATED.name(), mockOrder.getTotalPrice(),
                null, null, Collections.emptyList(), mockUser);

        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);
        when(userClient.getUserById(userId)).thenReturn(mockUser);
        when(orderMapper.toDto(eq(mockOrder), eq(mockUser))).thenReturn(expectedDto);

        Page<OrderResponseDto> actualPage = orderService.getAll(from, to, statuses, pageable);

        assertNotNull(actualPage, "Страница не должна быть null");
        assertEquals(1, actualPage.getTotalElements(), "На странице должен быть один элемент");
        assertEquals(expectedDto.id(), actualPage.getContent().get(0).id(), "ID заказа должен совпадать");
        assertEquals(expectedDto.user().id(), actualPage.getContent().get(0).user().id(), "ID пользователя должен совпадать");
        verify(orderRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        verify(userClient, times(1)).getUserById(userId);
        verify(orderMapper, times(1)).toDto(eq(mockOrder), eq(mockUser));
    }
}
