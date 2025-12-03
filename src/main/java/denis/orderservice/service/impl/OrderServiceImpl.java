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
import denis.orderservice.mapper.OrderItemMapper;
import denis.orderservice.mapper.OrderMapper;
import denis.orderservice.repository.ItemRepository;
import denis.orderservice.repository.OrderItemRepository;
import denis.orderservice.repository.OrderRepository;
import denis.orderservice.repository.specification.OrderSpecifications;
import denis.orderservice.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserServiceClient userClient;

    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                            ItemRepository itemRepository,
                            OrderMapper orderMapper,
                            OrderItemMapper orderItemMapper,
                            UserServiceClient userClient) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.itemRepository = itemRepository;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.userClient = userClient;
    }

    @Transactional
    @Override
    public OrderResponseDto create(OrderRequestDto dto) {
        Order order = orderMapper.toEntity(dto);
        order.setStatus(OrderStatus.CREATED);
        order.setItems(new ArrayList<>());
        return saveCreateOrderItem(order,dto.items());
    }

    public OrderResponseDto saveCreateOrderItem(Order order,List<OrderItemRequestDto> itemsDto){
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequestDto oi : itemsDto) {
            Item item = itemRepository.findById(oi.itemId())
                    .orElseThrow(() -> new ItemNotFoundException("Item not found: " + oi.itemId()));
            OrderItem orderItem = new OrderItem();
            orderItem.setQuantity(oi.quantity());
            orderItem.setItem(item);
            orderItem.setOrder(order);
            order.getItems().add(orderItem);
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(oi.quantity())));
        }
        order.setTotalPrice(total);
        Order saved = orderRepository.save(order);
        UserInfoDto user = userClient.getUserById(saved.getUserId());
        return orderMapper.toDto(saved, user);
    }

    @Transactional(readOnly = true)
    @Override
    public OrderResponseDto  getById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        UserInfoDto user = userClient.getUserById(order.getUserId());
        return orderMapper.toDto(order, user);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<OrderResponseDto> getAll(LocalDateTime from,
                              LocalDateTime to,
                              List<OrderStatus> statuses,
                              Pageable pageable) {

        Specification<Order> spec = Specification.unrestricted();
        spec = spec.and(OrderSpecifications.hasStatuses(statuses))
                .and(OrderSpecifications.createdAfter(from))
                .and(OrderSpecifications.createdBefore(to));
        Page<Order> page = orderRepository.findAll(spec, pageable);
        return page.map(order -> {
            UserInfoDto user = userClient.getUserById(order.getUserId());
            return orderMapper.toDto(order, user);
        });
    }

    @Transactional(readOnly = true)
    @Override
    public Page<OrderResponseDto> getByUserId(UUID userId, Pageable pageable) {
        Page<Order> page = orderRepository.findAllByUserId(userId,pageable);
        UserInfoDto user = userClient.getUserById(userId);
        return page.map( order -> orderMapper.toDto(order, user));
    }

    @Transactional
    @Override
    public OrderResponseDto update(UUID id, OrderUpdateRequestDto dto) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        order.setStatus(dto.status());
        order.getItems().clear();
        return saveCreateOrderItem(order, dto.items());
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        orderRepository.deleteById(id);
    }
}
