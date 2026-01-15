package denis.orderservice.dto.response;

import java.util.UUID;

public record OrderItemResponseDto(
        UUID id,
        ItemResponseDto item,
        Integer quantity
) {}

