package denis.orderservice.mapper;

import denis.orderservice.dto.request.OrderRequestDto;
import denis.orderservice.dto.response.OrderResponseDto;
import denis.orderservice.dto.response.UserInfoDto;
import denis.orderservice.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {
    Order toEntity(OrderRequestDto dto);
    @Mappings({
            @Mapping(source = "order.id", target = "id"),
            @Mapping(source = "user", target = "user")
    })
    OrderResponseDto toDto(Order order, UserInfoDto user);
}
