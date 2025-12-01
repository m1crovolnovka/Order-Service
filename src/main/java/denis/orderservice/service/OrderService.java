package denis.orderservice.service;

import denis.orderservice.entity.Order;
import denis.orderservice.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderService {
    Order create(Order order);
    Order getById(UUID id);
    Page<Order> getAll(LocalDateTime from, LocalDateTime to, List<OrderStatus> statuses, Pageable pageable);
    Page<Order> getByUserId(UUID userId, Pageable pageable);
    Order update(UUID id, Order update);
    void delete(UUID id);
}
