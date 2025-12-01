package denis.orderservice.service.impl;

import denis.orderservice.entity.Order;
import denis.orderservice.entity.OrderStatus;
import denis.orderservice.repository.OrderRepository;
import denis.orderservice.repository.specification.OrderSpecifications;
import denis.orderservice.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    @Override
    public Order create(Order order) {
        order.getItems().forEach(i -> i.setOrder(order));
        return orderRepository.save(order);
    }

    @Override
    public Order getById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
    }

    @Override
    public Page<Order> getAll(LocalDateTime from,
                              LocalDateTime to,
                              List<OrderStatus> statuses,
                              Pageable pageable) {
        Specification<Order> spec = Specification.unrestricted();
        spec = spec.and(OrderSpecifications.hasStatuses(statuses))
                .and(OrderSpecifications.createdAfter(from))
                .and(OrderSpecifications.createdBefore(to));
        return orderRepository.findAll(spec, pageable);
    }

    @Override
    public Page<Order> getByUserId(UUID userId, Pageable pageable) {
        return orderRepository.findAllByUserId(userId, pageable);
    }

    @Transactional
    @Override
    public Order update(UUID id, Order data) {
        Order order = getById(id);
        order.setStatus(data.getStatus());
        order.setTotalPrice(data.getTotalPrice());
        order.getItems().clear();
        data.getItems().forEach(i -> {
            i.setOrder(order);
            order.getItems().add(i);
        });
        return orderRepository.save(order);
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        orderRepository.deleteById(id);
    }
}
