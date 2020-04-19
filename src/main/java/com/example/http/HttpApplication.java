package com.example.http;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static java.time.Duration.ofSeconds;
import static java.util.stream.Stream.generate;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static reactor.core.publisher.Flux.fromStream;

@SpringBootApplication
public class HttpApplication {
    public static void main(String[] args) {
        SpringApplication.run(HttpApplication.class, args);
    }

    // Functional reactive endpoints
    @Bean
    RouterFunction<ServerResponse> routes(GreetingService service) {
        return route()
                .GET("/greeting/{name}", request -> ok().body(service.greetOnce(new GreetingRequest(request.pathVariable("name"))), GreetingResponse.class))
                .GET("/greetings/{name}", request -> ok()
                        .contentType(MediaType.TEXT_EVENT_STREAM)
                        .body(service.greetMany(new GreetingRequest(request.pathVariable("name"))), GreetingResponse.class))
                .build();

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class GreetingRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class GreetingResponse {
        private String message;
    }

	/*@RestController
	@RequiredArgsConstructor
	class GreetingRestController{
		private final GreetingService greetingService;
		//@PostMapping
		//@DeleteMapping
		@GetMapping("/greetings/{name}")
		Mono<GreetingResponse> greet(@PathVariable String name){
			return this.greetingService.greet(new GreetingRequest(name));
		}
	}*/

    @Service
    class GreetingService {

        private GreetingResponse greet(String name) {
            return new GreetingResponse("Hello " + name + ":" + Instant.now());
        }

        private Flux<GreetingResponse> greetMany(GreetingRequest request) {
            return fromStream(
                    generate(() -> greet(request.getName())))
                    .delayElements(ofSeconds(1));
        }

        Mono<GreetingResponse> greetOnce(GreetingRequest request) {
            return Mono.just(greet(request.getName()));
        }
    }
}
