package denis.orderservice.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserInfoDto(
        UUID id,
        String name,
        String surname,
        String email
) {}
