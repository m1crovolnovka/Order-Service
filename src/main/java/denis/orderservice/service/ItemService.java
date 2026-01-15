package denis.orderservice.service;

import denis.orderservice.dto.request.ItemRequestDto;
import denis.orderservice.dto.response.ItemResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface ItemService {
    ItemResponseDto create(ItemRequestDto dto);
    ItemResponseDto getById(UUID id);
    Page<ItemResponseDto> getAll(Pageable pageable);
    ItemResponseDto update(UUID id, ItemRequestDto dto);
    void delete(UUID id);
}
