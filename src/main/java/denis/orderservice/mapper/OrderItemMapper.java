package denis.orderservice.mapper;

import denis.orderservice.dto.request.OrderItemRequestDto;
import denis.orderservice.dto.response.OrderItemResponseDto;
import denis.orderservice.entity.OrderItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    OrderItem toEntity(OrderItemRequestDto dto);

    OrderItemResponseDto toResponseDto(OrderItem entity);
}

