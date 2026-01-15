package denis.orderservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemRequestDto(
        @NotBlank
        String name,
        @NotNull
        @DecimalMin(value = "0", inclusive = true)
        BigDecimal price
) {}