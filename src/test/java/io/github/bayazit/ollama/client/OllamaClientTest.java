package io.github.bayazit.ollama.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bayazit.ollama.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OllamaClientTest {

    private OllamaClient ollamaClient;
    private MockRestServiceServer mockServer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:11434");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        ollamaClient = new OllamaClient(builder.build());
    }

    @Test
    void chat_shouldPostToCorrectEndpointAndReturnResponse() throws Exception {
        ChatRequest request = ChatRequest.builder()
                .model("llama3.2")
                .messages(List.of(Message.builder().role(Role.USER).content("hello").build()))
                .build();
        ChatResponse expected = ChatResponse.builder()
                .model("llama3.2")
                .done(true)
                .message(Message.builder().role(Role.ASSISTANT).content("hi").build())
                .build();

        mockServer.expect(requestTo("http://localhost:11434/api/chat"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.model").value("llama3.2"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expected), MediaType.APPLICATION_JSON));

        ChatResponse result = ollamaClient.chat(request);

        assertThat(result.getModel()).isEqualTo("llama3.2");
        assertThat(result.getDone()).isTrue();
        assertThat(result.getMessage().getContent()).isEqualTo("hi");
        assertThat(request.getStream()).isFalse();
        mockServer.verify();
    }

    @Test
    void chat_shouldNotOverrideExplicitStreamValue() throws Exception {
        ChatRequest request = ChatRequest.builder()
                .model("llama3.2")
                .stream(true)
                .build();

        mockServer.expect(requestTo("http://localhost:11434/api/chat"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.stream").value(true))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        ollamaClient.chat(request);

        assertThat(request.getStream()).isTrue();
        mockServer.verify();
    }

    @Test
    void generate_shouldPostToCorrectEndpointAndReturnResponse() throws Exception {
        GenerateRequest request = GenerateRequest.builder()
                .model("llama3.2")
                .prompt("hello")
                .build();
        GenerateResponse expected = GenerateResponse.builder()
                .model("llama3.2")
                .response("hi")
                .done(true)
                .build();

        mockServer.expect(requestTo("http://localhost:11434/api/generate"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.model").value("llama3.2"))
                .andExpect(jsonPath("$.prompt").value("hello"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expected), MediaType.APPLICATION_JSON));

        GenerateResponse result = ollamaClient.generate(request);

        assertThat(result.getModel()).isEqualTo("llama3.2");
        assertThat(result.getResponse()).isEqualTo("hi");
        assertThat(request.getStream()).isFalse();
        mockServer.verify();
    }

    @Test
    void embed_shouldPostToCorrectEndpointAndReturnResponse() throws Exception {
        EmbedRequest request = EmbedRequest.builder()
                .model("llama3.2")
                .input(List.of("hello"))
                .build();
        EmbedResponse expected = EmbedResponse.builder()
                .model("llama3.2")
                .embeddings(List.of(List.of(0.1, 0.2, 0.3)))
                .build();

        mockServer.expect(requestTo("http://localhost:11434/api/embed"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.model").value("llama3.2"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expected), MediaType.APPLICATION_JSON));

        EmbedResponse result = ollamaClient.embed(request);

        assertThat(result.getModel()).isEqualTo("llama3.2");
        assertThat(result.getEmbeddings()).hasSize(1);
        mockServer.verify();
    }

    @Test
    void listModels_shouldGetFromCorrectEndpoint() throws Exception {
        ModelList expected = ModelList.builder()
                .models(List.of(ModelInfo.builder().name("llama3.2").build()))
                .build();

        mockServer.expect(requestTo("http://localhost:11434/api/tags"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expected), MediaType.APPLICATION_JSON));

        ModelList result = ollamaClient.listModels();

        assertThat(result.getModels()).hasSize(1);
        assertThat(result.getModels().get(0).getName()).isEqualTo("llama3.2");
        mockServer.verify();
    }

    @Test
    void showModel_shouldPostModelNameAndReturnInfo() throws Exception {
        ModelInfo expected = ModelInfo.builder().name("llama3.2").build();

        mockServer.expect(requestTo("http://localhost:11434/api/show"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.model").value("llama3.2"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expected), MediaType.APPLICATION_JSON));

        ModelInfo result = ollamaClient.showModel("llama3.2");

        assertThat(result.getName()).isEqualTo("llama3.2");
        mockServer.verify();
    }

    @Test
    void pullModel_shouldPostPullRequest() throws Exception {
        mockServer.expect(requestTo("http://localhost:11434/api/pull"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.model").value("llama3.2"))
                .andExpect(jsonPath("$.stream").value(false))
                .andRespond(withSuccess());

        ollamaClient.pullModel("llama3.2");

        mockServer.verify();
    }

    @Test
    void deleteModel_shouldSendDeleteRequest() throws Exception {
        mockServer.expect(requestTo("http://localhost:11434/api/delete"))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(jsonPath("$.model").value("llama3.2"))
                .andRespond(withSuccess());

        ollamaClient.deleteModel("llama3.2");

        mockServer.verify();
    }

    @Test
    void isAvailable_shouldReturnTrueWhenServerResponds() {
        mockServer.expect(requestTo("http://localhost:11434/"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess());

        assertThat(ollamaClient.isAvailable()).isTrue();
        mockServer.verify();
    }

    @Test
    void isAvailable_shouldReturnFalseOnException() {
        // Create a client pointing to an unreachable server
        RestClient failingClient = RestClient.builder().baseUrl("http://localhost:1").build();
        OllamaClient failingOllamaClient = new OllamaClient(failingClient);

        assertThat(failingOllamaClient.isAvailable()).isFalse();
    }
}
