package com.phuc.gatewayserver;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

//***Parameters Explained: parameters for Retry Configuration
//initialInterval	The starting wait time between the first and second retry (e.g. 100ms)
//maxInterval	The maximum wait time between retries (e.g. 1000ms)
//multiplier	The factor to multiply the delay after each retry (exponential increase)
//random	If true, adds a small random jitter to avoid thundering herd problem

//***Related to Circuit Breaker(TimeLimiter) and Retry at the Individual Service Level
//httpclient.response-timeout = "If no response from remote server in 2s, abort the connection"
//TimeLimiterConfig.timeoutDuration = "If this entire operation takes over 3s, give up and fallback"

//***Rate Limiter
//Nếu bạn muốn hỗ trợ 100 request mỗi giây, thì:
//Thông số	Ý nghĩa
//replenishRate = 100	Mỗi giây nạp lại 100 tokens (tức là cho phép tối đa 100 request mỗi giây).
//burstCapacity = 100	Cho phép tối đa 100 request đến cùng lúc (ví dụ: trong 1 ms).
//Nếu bạn không đặt burstCapacity = 100,
// ví dụ đặt thấp hơn như burstCapacity = 10,
// thì dù mỗi giây bạn có 100 token,
// nhưng chỉ cho phép 10 request đến cùng lúc,
// 90 request còn lại sẽ bị chặn nếu đến trong cùng thời điểm
// (dù vẫn trong giới hạn của replenishRate).


@SpringBootApplication
public class GatewayserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayserverApplication.class, args);
	}

	@Bean
	public RouteLocator eazyBankRouteConfig(RouteLocatorBuilder routeLocatorBuilder) {
		return routeLocatorBuilder.routes()
				.route(p -> p
						.path("/phucnguyen/accounts/**")
						.filters( f -> f.rewritePath("/phucnguyen/accounts/(?<segment>.*)","/${segment}")
								.addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
								.circuitBreaker(config -> config
										.setName("accountsCircuitBreaker")
										.setFallbackUri("forward:/contactSupport")
								)
						)
						.uri("lb://ACCOUNTS"))
				.route(p -> p
						.path("/phucnguyen/loans/**")
						.filters( f -> f.rewritePath("/phucnguyen/loans/(?<segment>.*)","/${segment}")
								.addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
								.retry(retryConfig -> retryConfig
										.setRetries(3)
										.setMethods(HttpMethod.GET)
										.setBackoff(Duration.ofMillis(1000), Duration.ofMillis(9000), 2, true)
								)
						)
						.uri("lb://LOANS"))
				.route(p -> p
						.path("/phucnguyen/cards/**")
						.filters( f -> f.rewritePath("/phucnguyen/cards/(?<segment>.*)","/${segment}")
								.addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
								.requestRateLimiter(config -> config
										.setRateLimiter(redisRateLimiter())
										.setKeyResolver(userKeyResolver())
								)
						)
						.uri("lb://CARDS")).build();


	}
	// If you don't set timeLimiterConfig, the default timeout is 1 second.
	@Bean
	public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
		return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
				.circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
				.timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(10)).build()).build());
	}

	@Bean
	public RedisRateLimiter redisRateLimiter() {
		return new RedisRateLimiter(1, 1, 1);
	}

	@Bean
	KeyResolver userKeyResolver() {
		return exchange -> Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("user"))
				.defaultIfEmpty("anonymous");
	}

}
