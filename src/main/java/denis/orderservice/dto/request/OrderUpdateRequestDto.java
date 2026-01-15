package denis.orderservice.dto.request;

import denis.orderservice.entity.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderUpdateRequestDto(
        @Valid
        List<OrderItemRequestDto> items,
        @NotNull
        OrderStatus status
) {}
