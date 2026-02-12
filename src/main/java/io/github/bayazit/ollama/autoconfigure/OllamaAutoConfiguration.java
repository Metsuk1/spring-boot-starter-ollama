package io.github.bayazit.ollama.autoconfigure;

import io.github.bayazit.ollama.client.OllamaClient;
import io.github.bayazit.ollama.client.OllamaStreamingClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@AutoConfiguration
@EnableConfigurationProperties(OllamaProperties.class)
@ConditionalOnClass(RestClient.class)
public class OllamaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "ollamaRestClient")
    public RestClient ollamaRestClient(OllamaProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.getTimeout().toMillis());
        factory.setReadTimeout((int) properties.getTimeout().toMillis());

        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(factory)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public OllamaClient ollamaClient(RestClient ollamaRestClient) {
        return new OllamaClient(ollamaRestClient);
    }

    @Bean
    @ConditionalOnClass(WebClient.class)
    @ConditionalOnMissingBean(name = "ollamaWebClient")
    public WebClient ollamaWebClient(OllamaProperties properties) {
        return WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    @Bean
    @ConditionalOnClass(WebClient.class)
    @ConditionalOnMissingBean
    public OllamaStreamingClient ollamaStreamingClient(WebClient ollamaWebClient) {
        return new OllamaStreamingClient(ollamaWebClient);
    }
}
