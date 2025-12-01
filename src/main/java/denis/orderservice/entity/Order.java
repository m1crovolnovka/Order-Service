package denis.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue
    private UUID id;
    private UUID userId;
    private String status;
    private BigDecimal totalPrice;
    private Boolean deleted;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;
}
