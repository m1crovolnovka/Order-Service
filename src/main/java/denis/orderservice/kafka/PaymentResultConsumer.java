package denis.orderservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import denis.orderservice.dto.kafka.PaymentEventDto;
import denis.orderservice.service.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PaymentResultConsumer {

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    public PaymentResultConsumer(OrderService orderService, ObjectMapper objectMapper) {
        this.orderService = orderService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "payment-results",
            groupId = "order-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenPaymentResults(byte[] data) {
        try {
            PaymentEventDto event = objectMapper.readValue(data, PaymentEventDto.class);
            orderService.updateStatusFromPayment(event.getOrderId(), event.getStatus());
        } catch (IOException e) {
            throw new RuntimeException("Failed to process payment result event", e);
        }
    }
}
