package denis.orderservice.controller;

import denis.orderservice.dto.request.OrderRequestDto;
import denis.orderservice.dto.request.OrderUpdateRequestDto;
import denis.orderservice.dto.response.OrderResponseDto;
import denis.orderservice.entity.OrderStatus;
import denis.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody @Valid OrderRequestDto dto) {
        OrderResponseDto response = orderService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or @ss.isOrderOwner(#id, authentication.name)")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable UUID id) {
        OrderResponseDto response = orderService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<OrderResponseDto>> getAllOrders(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) List<OrderStatus> statuses,
            Pageable pageable) {

        Page<OrderResponseDto> page = orderService.getAll(from, to, statuses, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or authentication.name == #userId.toString()")
    public ResponseEntity<Page<OrderResponseDto>> getOrdersByUserId(
            @PathVariable UUID userId,
            Pageable pageable) {

        Page<OrderResponseDto> page = orderService.getByUserId(userId, pageable);
        return ResponseEntity.ok(page);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or @ss.isOrderOwner(#id, authentication.name)")
    public ResponseEntity<OrderResponseDto> updateOrder(
            @PathVariable UUID id,
            @RequestBody @Valid OrderUpdateRequestDto dto) {

        OrderResponseDto response = orderService.update(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN') or @ss.isOrderOwner(#id, authentication.name)")
    public ResponseEntity<Void> deleteOrder(@PathVariable UUID id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
