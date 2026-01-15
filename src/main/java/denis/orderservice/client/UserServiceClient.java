package denis.orderservice.client;

import denis.orderservice.dto.response.UserInfoDto;
import denis.orderservice.exception.ExternalServiceException;
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

    default UserInfoDto getUserByIdFallback(UUID id, Throwable ex) {
        throw new ExternalServiceException("User Service is currently unavailable. Order flow interrupted.");
    }
}