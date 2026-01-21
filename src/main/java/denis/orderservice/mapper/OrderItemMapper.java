package denis.orderservice.mapper;

import denis.orderservice.dto.request.OrderItemRequestDto;
import denis.orderservice.dto.response.OrderItemResponseDto;
import denis.orderservice.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",  uses = {ItemMapper.class})
public interface OrderItemMapper {

    @Mapping(source = "itemId", target = "item.id")
    OrderItem toEntity(OrderItemRequestDto dto);

    OrderItemResponseDto toResponseDto(OrderItem entity);
}

