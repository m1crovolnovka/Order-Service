package denis.orderservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import denis.orderservice.config.TestContainersConfig;
import denis.orderservice.dto.request.ItemRequestDto;
import denis.orderservice.entity.Item;
import denis.orderservice.repository.ItemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.UUID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
class ItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        itemRepository.deleteAll();
    }

    private ItemRequestDto createItemRequestDto(String name, BigDecimal price) {
        return new ItemRequestDto(name, price);
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void createItem_shouldReturnCreatedItem() throws Exception {
        ItemRequestDto requestDto = createItemRequestDto("Test Item", new BigDecimal("10.99"));
        String requestJson = objectMapper.writeValueAsString(requestDto);
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.price").value(10.99));
       assertThat(itemRepository.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void getItemById_shouldReturnItem_whenItemExists() throws Exception {
        UUID savedId = itemRepository.save(new Item(
                null, "Existing Item", new BigDecimal("25.00")
        )).getId();

        mockMvc.perform(get("/api/items/{id}", savedId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedId.toString()))
                .andExpect(jsonPath("$.name").value("Existing Item"))
                .andExpect(jsonPath("$.price").value(25.00));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void getAllItems_shouldReturnPagedListOfItems() throws Exception {
        itemRepository.save(new denis.orderservice.entity.Item(null, "Item A", new BigDecimal("5.00")));
        itemRepository.save(new denis.orderservice.entity.Item(null, "Item B", new BigDecimal("15.00")));

        mockMvc.perform(get("/api/items?size=1&page=0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Item A"));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void updateItem_shouldReturnUpdatedItem() throws Exception {
        UUID savedId = itemRepository.save(new denis.orderservice.entity.Item(
                null, "Old Name", new BigDecimal("99.99")
        )).getId();
        ItemRequestDto updateDto = createItemRequestDto("New Name", new BigDecimal("1.50"));
        String updateJson = objectMapper.writeValueAsString(updateDto);

        mockMvc.perform(put("/api/items/{id}", savedId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedId.toString()))
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.price").value(1.50));
        assertThat(itemRepository.findById(savedId).get().getName()).isEqualTo("New Name");
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void deleteItem_shouldReturnNoContent() throws Exception {
        UUID savedId = itemRepository.save(new denis.orderservice.entity.Item(
                null, "To Delete", new BigDecimal("1.00")
        )).getId();
        mockMvc.perform(delete("/api/items/{id}", savedId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        assertThat(itemRepository.existsById(savedId)).isFalse();
    }
}
