package denis.orderservice.service.impl;

import denis.orderservice.dto.request.ItemRequestDto;
import denis.orderservice.dto.response.ItemResponseDto;
import denis.orderservice.entity.Item;
import denis.orderservice.exception.ItemNotFoundException;
import denis.orderservice.mapper.ItemMapper;
import denis.orderservice.repository.ItemRepository;
import denis.orderservice.service.ItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    public ItemServiceImpl(ItemRepository itemRepository,
                           ItemMapper itemMapper) {
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
    }

    @Override
    @Transactional
    public ItemResponseDto create(ItemRequestDto dto) {
        Item item = itemMapper.toEntity(dto);
        Item itemSaved = itemRepository.save(item);
        return itemMapper.toDto(itemSaved);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponseDto getById(UUID id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with id: " + id));
        return itemMapper.toDto(item);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ItemResponseDto> getAll(Pageable pageable) {
        Page<Item> items = itemRepository.findAll(pageable);
        return items.map(itemMapper::toDto);
    }

    @Override
    @Transactional
    public ItemResponseDto update(UUID id, ItemRequestDto dto) {
        Item existingItem = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Item not found with id: " + id));
        existingItem.setName(dto.name());
        existingItem.setPrice(dto.price());
        Item savedItem =  itemRepository.save(existingItem);
        return itemMapper.toDto(savedItem);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!itemRepository.existsById(id)) {
            throw new ItemNotFoundException("Item not found with id: " + id);
        }
        itemRepository.deleteById(id);
    }
}
