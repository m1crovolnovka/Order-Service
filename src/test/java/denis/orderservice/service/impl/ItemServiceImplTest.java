package denis.orderservice.service.impl;

import denis.orderservice.dto.request.ItemRequestDto;
import denis.orderservice.dto.response.ItemResponseDto;
import denis.orderservice.entity.Item;
import denis.orderservice.exception.ItemNotFoundException;
import denis.orderservice.mapper.ItemMapper;
import denis.orderservice.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemMapper itemMapper;
    @InjectMocks
    private ItemServiceImpl itemService;
    private final UUID itemId = UUID.randomUUID();
    private Item mockItem;
    private ItemResponseDto mockResponseDto;
    private ItemRequestDto mockRequestDto;

    @BeforeEach
    void setUp() {
        mockItem = Item.builder()
                .id(itemId)
                .name("Old Name")
                .price(new BigDecimal("10.50"))
                .build();

        mockResponseDto = new ItemResponseDto(itemId, "Old Name", new BigDecimal("10.50"));
        mockRequestDto = new ItemRequestDto("New Name", new BigDecimal("20.00"));
    }

    @Test
    void createWhenSuccessful() {
        when(itemMapper.toEntity(mockRequestDto)).thenReturn(mockItem);
        when(itemRepository.save(mockItem)).thenReturn(mockItem);
        when(itemMapper.toDto(mockItem)).thenReturn(mockResponseDto);

        ItemResponseDto result = itemService.create(mockRequestDto);

        assertNotNull(result);
        assertEquals(mockResponseDto.id(), result.id());
        verify(itemMapper, times(1)).toEntity(mockRequestDto);
        verify(itemRepository, times(1)).save(mockItem);
        verify(itemMapper, times(1)).toDto(mockItem);
    }

    @Test
    void getByIdWhenFound() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(mockItem));
        when(itemMapper.toDto(mockItem)).thenReturn(mockResponseDto);

        ItemResponseDto result = itemService.getById(itemId);

        assertNotNull(result);
        assertEquals(itemId, result.id());
        verify(itemRepository, times(1)).findById(itemId);
        verify(itemMapper, times(1)).toDto(mockItem);
    }

    @Test
    void getByIdWhenNotFound() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> itemService.getById(itemId));

        verify(itemMapper, never()).toDto(any());
    }

    @Test
    void getAllWithFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Item> itemList = List.of(mockItem);
        Page<Item> mockPage = new PageImpl<>(itemList, pageable, itemList.size());
        when(itemRepository.findAll(pageable)).thenReturn(mockPage);
        when(itemMapper.toDto(any(Item.class))).thenReturn(mockResponseDto);

        Page<ItemResponseDto> resultPage = itemService.getAll(pageable);

        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        assertEquals(itemId, resultPage.getContent().get(0).id());
        verify(itemRepository, times(1)).findAll(pageable);
        verify(itemMapper, times(1)).toDto(mockItem);
    }

    @Test
    void updateWhenSuccessful() {
        ItemResponseDto updatedResponseDto = new ItemResponseDto(itemId, mockRequestDto.name(), mockRequestDto.price());
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(mockItem));
        when(itemRepository.save(any(Item.class))).thenReturn(mockItem);
        when(itemMapper.toDto(any(Item.class))).thenReturn(updatedResponseDto);

        ItemResponseDto result = itemService.update(itemId, mockRequestDto);

        assertNotNull(result);
        assertEquals(mockRequestDto.name(), result.name(), "Имя должно быть обновлено");
        assertEquals(mockRequestDto.price(), result.price(), "Цена должна быть обновлена");
        assertEquals(mockRequestDto.name(), mockItem.getName());
        verify(itemRepository, times(1)).findById(itemId);
        verify(itemRepository, times(1)).save(mockItem);
    }

    @Test
    void updateWhenItemNotFound() {
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> itemService.update(itemId, mockRequestDto));

        verify(itemRepository, never()).save(any());
    }

    @Test
    void deleteWhenItemExists() {
        when(itemRepository.existsById(itemId)).thenReturn(true);

        itemService.delete(itemId);

        verify(itemRepository, times(1)).existsById(itemId);
        verify(itemRepository, times(1)).deleteById(itemId);
    }

    @Test
    void delete_WhenItemDoesNotExist() {
        when(itemRepository.existsById(itemId)).thenReturn(false);

        assertThrows(ItemNotFoundException.class, () -> itemService.delete(itemId));

        verify(itemRepository, never()).deleteById(any());
    }
}
