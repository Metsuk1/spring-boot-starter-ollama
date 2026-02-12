package io.github.bayazit.ollama.client;

import io.github.bayazit.ollama.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OllamaStreamingClientTest {

    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    private OllamaStreamingClient streamingClient;

    @BeforeEach
    void setUp() {
        streamingClient = new OllamaStreamingClient(webClient);
    }

    private void stubPost(String uri) {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        doReturn(requestBodyUriSpec).when(requestBodyUriSpec).uri(uri);
        doReturn(requestHeadersSpec).when(requestBodyUriSpec).bodyValue(any());
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
    }

    @Test
    void chatStream_shouldPostToCorrectEndpointAndStreamResponses() {
        ChatRequest request = ChatRequest.builder()
                .model("llama3.2")
                .messages(List.of(Message.builder().role(Role.USER).content("hello").build()))
                .build();

        ChatResponse chunk1 = ChatResponse.builder()
                .model("llama3.2")
                .done(false)
                .message(Message.builder().role(Role.ASSISTANT).content("Hi").build())
                .build();
        ChatResponse chunk2 = ChatResponse.builder()
                .model("llama3.2")
                .done(true)
                .message(Message.builder().role(Role.ASSISTANT).content("!").build())
                .build();

        stubPost("/api/chat");
        when(responseSpec.bodyToFlux(ChatResponse.class)).thenReturn(Flux.just(chunk1, chunk2));

        StepVerifier.create(streamingClient.chatStream(request))
                .expectNext(chunk1)
                .expectNext(chunk2)
                .verifyComplete();

        assertThat(request.getStream()).isTrue();
    }

    @Test
    void generateStream_shouldPostToCorrectEndpointAndStreamResponses() {
        GenerateRequest request = GenerateRequest.builder()
                .model("llama3.2")
                .prompt("hello")
                .build();

        GenerateResponse chunk1 = GenerateResponse.builder()
                .model("llama3.2")
                .response("Hi")
                .done(false)
                .build();
        GenerateResponse chunk2 = GenerateResponse.builder()
                .model("llama3.2")
                .response("!")
                .done(true)
                .build();

        stubPost("/api/generate");
        when(responseSpec.bodyToFlux(GenerateResponse.class)).thenReturn(Flux.just(chunk1, chunk2));

        StepVerifier.create(streamingClient.generateStream(request))
                .expectNext(chunk1)
                .expectNext(chunk2)
                .verifyComplete();

        assertThat(request.getStream()).isTrue();
    }
}
