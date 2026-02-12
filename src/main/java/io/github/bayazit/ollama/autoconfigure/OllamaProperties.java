package io.github.bayazit.ollama.autoconfigure;

import io.github.bayazit.ollama.model.OllamaOptions;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "ollama")
public class OllamaProperties {

    /**
     * Base URL for the Ollama API.
     */
    private String baseUrl = "http://localhost:11434";

    /**
     * Default model name.
     */
    private String model = "llama3.2";

    /**
     * Request timeout.
     */
    private Duration timeout = Duration.ofSeconds(60);

    /**
     * Default generation options.
     */
    private OllamaOptions options;
}
