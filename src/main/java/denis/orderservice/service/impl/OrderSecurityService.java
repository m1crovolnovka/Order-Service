package denis.orderservice.service.impl;

import denis.orderservice.dto.response.OrderResponseDto;
import denis.orderservice.service.OrderService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("ss")
public class OrderSecurityService {
    private final OrderService orderService;

    public OrderSecurityService(OrderService orderService) {
        this.orderService = orderService;
    }

    public boolean isOrderOwner(UUID orderId, String principalId) {
        OrderResponseDto order = orderService.getById(orderId);
        return order.userId().toString().equals(principalId);
    }
}