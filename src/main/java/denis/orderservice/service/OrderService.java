package denis.orderservice.service;

import denis.orderservice.dto.request.OrderRequestDto;
import denis.orderservice.dto.request.OrderUpdateRequestDto;
import denis.orderservice.dto.response.OrderResponseDto;
import denis.orderservice.entity.Order;
import denis.orderservice.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderResponseDto create(OrderRequestDto orderRequestDto);
    OrderResponseDto  getById(UUID id);
    Page<OrderResponseDto> getAll(LocalDateTime from, LocalDateTime to, List<OrderStatus> statuses, Pageable pageable);
    Page<OrderResponseDto> getByUserId(UUID userId, Pageable pageable);
    OrderResponseDto  update(UUID id, OrderUpdateRequestDto dto);
    void delete(UUID id);
    void updateStatusFromPayment(UUID orderId, String paymentStatus);
}
