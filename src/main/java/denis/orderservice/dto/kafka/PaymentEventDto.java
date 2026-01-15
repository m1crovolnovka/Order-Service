package denis.orderservice.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class PaymentEventDto {
    private UUID orderId;
    private String status;
}
