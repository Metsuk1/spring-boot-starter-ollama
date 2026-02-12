package io.github.bayazit.ollama.client;

import io.github.bayazit.ollama.model.*;
import org.springframework.web.client.RestClient;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.Map;

public class OllamaClient {

    private final RestClient restClient;

    public OllamaClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public ChatResponse chat(ChatRequest request) {
        if (request.getStream() == null) {
            request.setStream(false);
        }
        return restClient.post()
                .uri("/api/chat")
                .body(request)
                .retrieve()
                .body(ChatResponse.class);
    }

    public GenerateResponse generate(GenerateRequest request) {
        if (request.getStream() == null) {
            request.setStream(false);
        }
        return restClient.post()
                .uri("/api/generate")
                .body(request)
                .retrieve()
                .body(GenerateResponse.class);
    }

    public EmbedResponse embed(EmbedRequest request) {
        return restClient.post()
                .uri("/api/embed")
                .body(request)
                .retrieve()
                .body(EmbedResponse.class);
    }

    public ModelList listModels() {
        return restClient.get()
                .uri("/api/tags")
                .retrieve()
                .body(ModelList.class);
    }

    @SuppressWarnings("unchecked")
    public ModelInfo showModel(String model) {
        return restClient.post()
                .uri("/api/show")
                .body(Map.of("model", model))
                .retrieve()
                .body(ModelInfo.class);
    }

    public void pullModel(String model) {
        restClient.post()
                .uri("/api/pull")
                .body(PullRequest.builder().model(model).stream(false).build())
                .retrieve()
                .toBodilessEntity();
    }

    public void deleteModel(String model) {
        restClient.method(HttpMethod.DELETE)
                .uri("/api/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .body(DeleteRequest.builder().model(model).build())
                .retrieve()
                .toBodilessEntity();
    }

    public boolean isAvailable() {
        try {
            restClient.get()
                    .uri("/")
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
