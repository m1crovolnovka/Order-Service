package denis.orderservice.dto.response;

import java.util.UUID;

public record OrderItemResponseDto(
        UUID id,
        UUID itemId,
        Integer quantity
) {}

