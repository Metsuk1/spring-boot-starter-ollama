package io.github.bayazit.ollama.autoconfigure;

import io.github.bayazit.ollama.client.OllamaClient;
import io.github.bayazit.ollama.client.OllamaStreamingClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class OllamaAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OllamaAutoConfiguration.class));

    @Test
    void shouldCreateAllBeansWithDefaultConfig() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(OllamaClient.class);
            assertThat(context).hasSingleBean(OllamaStreamingClient.class);
            assertThat(context).hasBean("ollamaRestClient");
            assertThat(context).hasBean("ollamaWebClient");
            assertThat(context).hasSingleBean(OllamaProperties.class);
        });
    }

    @Test
    void shouldApplyDefaultProperties() {
        contextRunner.run(context -> {
            OllamaProperties properties = context.getBean(OllamaProperties.class);
            assertThat(properties.getBaseUrl()).isEqualTo("http://localhost:11434");
            assertThat(properties.getModel()).isEqualTo("llama3.2");
            assertThat(properties.getTimeout()).isEqualTo(Duration.ofSeconds(60));
            assertThat(properties.getOptions()).isNull();
        });
    }

    @Test
    void shouldApplyCustomProperties() {
        contextRunner
                .withPropertyValues(
                        "ollama.base-url=http://custom:1234",
                        "ollama.model=mistral",
                        "ollama.timeout=30s"
                )
                .run(context -> {
                    OllamaProperties properties = context.getBean(OllamaProperties.class);
                    assertThat(properties.getBaseUrl()).isEqualTo("http://custom:1234");
                    assertThat(properties.getModel()).isEqualTo("mistral");
                    assertThat(properties.getTimeout()).isEqualTo(Duration.ofSeconds(30));
                });
    }

    @Test
    void shouldBackOffWhenUserDefinesOllamaClient() {
        contextRunner
                .withUserConfiguration(CustomClientConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(OllamaClient.class);
                    assertThat(context.getBean(OllamaClient.class))
                            .isSameAs(context.getBean("customOllamaClient"));
                });
    }

    @Test
    void shouldBackOffWhenUserDefinesOllamaRestClientByName() {
        contextRunner
                .withUserConfiguration(CustomRestClientConfig.class)
                .run(context -> {
                    // user-defined ollamaRestClient should take precedence
                    RestClient bean = (RestClient) context.getBean("ollamaRestClient");
                    assertThat(bean).isNotNull();
                    // auto-config should not create its own
                    assertThat(context.getBeansOfType(RestClient.class)).hasSize(1);
                });
    }

    @Test
    void shouldBackOffWhenUserDefinesStreamingClient() {
        contextRunner
                .withUserConfiguration(CustomStreamingClientConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(OllamaStreamingClient.class);
                    assertThat(context.getBean(OllamaStreamingClient.class))
                            .isSameAs(context.getBean("customStreamingClient"));
                });
    }

    @Configuration
    static class CustomClientConfig {
        @Bean
        OllamaClient customOllamaClient() {
            return new OllamaClient(mock(RestClient.class));
        }
    }

    @Configuration
    static class CustomRestClientConfig {
        @Bean
        RestClient ollamaRestClient() {
            return mock(RestClient.class);
        }
    }

    @Configuration
    static class CustomStreamingClientConfig {
        @Bean
        OllamaStreamingClient customStreamingClient() {
            return new OllamaStreamingClient(mock(WebClient.class));
        }
    }
}
