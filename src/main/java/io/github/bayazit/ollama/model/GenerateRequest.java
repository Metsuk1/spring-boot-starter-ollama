package io.github.bayazit.ollama.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenerateRequest {

    private String model;
    private String prompt;
    private String system;
    private Boolean stream;
    private List<String> images;
    private String format;
    private List<Long> context;
    private OllamaOptions options;

    @JsonProperty("keep_alive")
    private String keepAlive;
}
