package denis.orderservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderItemRequestDto(
        @NotNull
        UUID itemId,
        @NotNull
        @Min(1)
        Integer quantity
) {}
