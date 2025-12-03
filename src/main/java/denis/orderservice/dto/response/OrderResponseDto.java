package denis.orderservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponseDto(
        UUID id,
        UUID userId,
        String status,
        BigDecimal totalPrice,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<OrderItemResponseDto> items,
        UserInfoDto user
) {}

