package com.jcaa.usersmanagement.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.jcaa.usersmanagement.infrastructure.adapter.email.BrevoConfig;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.web.client.RestClient;

@DisplayName("BrevoSpringConfig")
class BrevoSpringConfigTest {

  @Test
  @DisplayName("crea la configuración validada y el cliente REST con credenciales")
  void shouldCreateBrevoConfigAndRestClient() {
    // Arrange
    final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context
        .getBeanFactory()
        .setConversionService(ApplicationConversionService.getSharedInstance());
    context
        .getEnvironment()
        .getPropertySources()
        .addFirst(
            new MapPropertySource(
                "brevo-test",
                Map.ofEntries(
                    Map.entry("brevo.base-url", "https://api.brevo.test"),
                    Map.entry("brevo.api-key", "xkeysib-test-api-key"),
                    Map.entry("brevo.from.address", "test@example.com"),
                    Map.entry("brevo.from.name", "Test Sender"),
                    Map.entry("brevo.timeout.connect", "100ms"),
                    Map.entry("brevo.timeout.read", "100ms"),
                    Map.entry("brevo.retry.max-attempts", "1"),
                    Map.entry("brevo.retry.initial-delay", "1ms"),
                    Map.entry("brevo.retry.multiplier", "2.0"),
                    Map.entry("brevo.circuit-breaker.failure-rate-threshold", "50"),
                    Map.entry("brevo.circuit-breaker.sliding-window-size", "10"),
                    Map.entry("brevo.circuit-breaker.minimum-calls", "5"),
                    Map.entry("brevo.circuit-breaker.open-duration", "1s"))));
    context.register(BrevoSpringConfig.class);

    // Act
    context.refresh();

    // Assert
    final BrevoConfig config = context.getBean(BrevoConfig.class);
    final RestClient restClient = context.getBean(RestClient.class);
    assertEquals("https://api.brevo.test", config.baseUrl());
    assertEquals("xkeysib-test-api-key", config.apiKey());
    assertNotNull(restClient);
    context.close();
  }
}
