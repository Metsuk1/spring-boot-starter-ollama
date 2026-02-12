# Spring Boot Starter Ollama

A Spring Boot starter that provides auto-configuration for connecting to an [Ollama](https://ollama.ai) instance. Supports the full Ollama API including chat, text generation, embeddings, and model management with both synchronous and streaming modes.

## Requirements

- Java 17+
- Spring Boot 3.2+
- Running Ollama instance

## Quick Start

### 1. Add the dependency

```xml
<dependency>
    <groupId>io.github.bayazit</groupId>
    <artifactId>spring-boot-starter-ollama</artifactId>
    <version>0.1.0</version>
</dependency>
```

### 2. Configure (optional)

Add to `application.yml`:

```yaml
ollama:
  base-url: http://localhost:11434
  model: llama3.2
  timeout: 60s
  options:
    temperature: 0.7
    top-p: 0.9
    num-predict: 256
```

All properties are optional — defaults are shown above (except `options`, which is `null` by default).

### 3. Inject and use

```java
@Service
public class MyService {

    private final OllamaClient ollamaClient;

    public MyService(OllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    public String askQuestion(String question) {
        ChatRequest request = ChatRequest.builder()
                .model("llama3.2")
                .messages(List.of(
                        Message.builder().role(Role.USER).content(question).build()
                ))
                .build();

        ChatResponse response = ollamaClient.chat(request);
        return response.getMessage().getContent();
    }
}
```

## Configuration Properties

| Property | Type | Default | Description |
|---|---|---|---|
| `ollama.base-url` | `String` | `http://localhost:11434` | Ollama server URL |
| `ollama.model` | `String` | `llama3.2` | Default model name |
| `ollama.timeout` | `Duration` | `60s` | Request timeout |
| `ollama.options.temperature` | `Double` | — | Sampling temperature |
| `ollama.options.top-p` | `Double` | — | Top-p (nucleus) sampling |
| `ollama.options.top-k` | `Integer` | — | Top-k sampling |
| `ollama.options.num-predict` | `Integer` | — | Max tokens to predict |
| `ollama.options.seed` | `Integer` | — | Random seed |
| `ollama.options.stop` | `List<String>` | — | Stop sequences |
| `ollama.options.repeat-penalty` | `Double` | — | Repeat penalty |
| `ollama.options.num-ctx` | `Integer` | — | Context window size |

## API Reference

### OllamaClient (Synchronous)

The `OllamaClient` bean uses Spring's `RestClient` for synchronous HTTP calls.

#### Chat

```java
ChatRequest request = ChatRequest.builder()
        .model("llama3.2")
        .messages(List.of(
                Message.builder().role(Role.SYSTEM).content("You are a helpful assistant.").build(),
                Message.builder().role(Role.USER).content("Hello!").build()
        ))
        .options(OllamaOptions.builder().temperature(0.8).build())
        .build();

ChatResponse response = ollamaClient.chat(request);
```

#### Generate (Text Completion)

```java
GenerateRequest request = GenerateRequest.builder()
        .model("llama3.2")
        .prompt("Why is the sky blue?")
        .build();

GenerateResponse response = ollamaClient.generate(request);
String text = response.getResponse();
```

#### Embeddings

```java
EmbedRequest request = EmbedRequest.builder()
        .model("llama3.2")
        .input(List.of("Hello world", "Ollama is great"))
        .build();

EmbedResponse response = ollamaClient.embed(request);
List<List<Double>> embeddings = response.getEmbeddings();
```

#### Model Management

```java
// List all models
ModelList models = ollamaClient.listModels();

// Show model details
ModelInfo info = ollamaClient.showModel("llama3.2");

// Pull a model
ollamaClient.pullModel("llama3.2");

// Delete a model
ollamaClient.deleteModel("llama3.2");

// Health check
boolean available = ollamaClient.isAvailable();
```

### OllamaStreamingClient (Reactive)

The `OllamaStreamingClient` bean uses Spring WebFlux `WebClient` for streaming responses.

#### Streaming Chat

```java
@Service
public class StreamingService {

    private final OllamaStreamingClient streamingClient;

    public StreamingService(OllamaStreamingClient streamingClient) {
        this.streamingClient = streamingClient;
    }

    public Flux<String> streamChat(String userMessage) {
        ChatRequest request = ChatRequest.builder()
                .model("llama3.2")
                .messages(List.of(
                        Message.builder().role(Role.USER).content(userMessage).build()
                ))
                .build();

        return streamingClient.chatStream(request)
                .map(response -> response.getMessage().getContent());
    }
}
```

#### Streaming Generation

```java
GenerateRequest request = GenerateRequest.builder()
        .model("llama3.2")
        .prompt("Write a poem about spring")
        .build();

streamingClient.generateStream(request)
        .doOnNext(chunk -> System.out.print(chunk.getResponse()))
        .blockLast();
```

## Auto-Configuration

The starter automatically configures the following beans:

| Bean | Type | Condition |
|---|---|---|
| `ollamaRestClient` | `RestClient` | `@ConditionalOnMissingBean` |
| `ollamaClient` | `OllamaClient` | `@ConditionalOnMissingBean` |
| `ollamaWebClient` | `WebClient` | `@ConditionalOnClass(WebClient.class)` |
| `ollamaStreamingClient` | `OllamaStreamingClient` | `@ConditionalOnClass(WebClient.class)` |

You can override any bean by defining your own with the same type.

## Building from Source

```bash
git clone https://github.com/bayazit/spring-boot-starter-ollama.git
cd spring-boot-starter-ollama
mvn clean install
```
