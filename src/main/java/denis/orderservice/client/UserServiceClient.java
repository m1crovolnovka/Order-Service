package denis.orderservice.client;

import denis.orderservice.dto.response.UserInfoDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;


@FeignClient(
        name = "user-service-client",
        url = "${client.user-service.url}"
)
public interface UserServiceClient {

    String CB_NAME = "userServiceCB";

    @GetMapping("/api/users/{id}")
    @CircuitBreaker(name = CB_NAME, fallbackMethod = "getUserByIdFallback")
     UserInfoDto getUserById(@PathVariable("id") UUID id);

    default UserInfoDto getUserByIdFallback(String email, Throwable ex) {
        return UserInfoDto.builder()
                .id(null)
                .name("Unknown")
                .surname("User")
                .email(email)
                .build();
    }
}