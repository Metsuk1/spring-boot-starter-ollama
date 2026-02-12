package io.github.bayazit.ollama.client;

import io.github.bayazit.ollama.model.ChatRequest;
import io.github.bayazit.ollama.model.ChatResponse;
import io.github.bayazit.ollama.model.GenerateRequest;
import io.github.bayazit.ollama.model.GenerateResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

public class OllamaStreamingClient {

    private final WebClient webClient;

    public OllamaStreamingClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<ChatResponse> chatStream(ChatRequest request) {
        request.setStream(true);
        return webClient.post()
                .uri("/api/chat")
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(ChatResponse.class);
    }

    public Flux<GenerateResponse> generateStream(GenerateRequest request) {
        request.setStream(true);
        return webClient.post()
                .uri("/api/generate")
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(GenerateResponse.class);
    }
}
