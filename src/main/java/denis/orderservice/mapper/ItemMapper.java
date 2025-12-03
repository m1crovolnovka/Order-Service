package denis.orderservice.mapper;

import denis.orderservice.dto.request.ItemRequestDto;
import denis.orderservice.dto.response.ItemResponseDto;
import denis.orderservice.entity.Item;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    Item toEntity(ItemRequestDto dto);
    ItemResponseDto toDto(Item item);
}