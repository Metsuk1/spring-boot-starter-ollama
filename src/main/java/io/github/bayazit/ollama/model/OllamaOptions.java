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
public class OllamaOptions {

    private Double temperature;

    @JsonProperty("top_p")
    private Double topP;

    @JsonProperty("top_k")
    private Integer topK;

    @JsonProperty("num_predict")
    private Integer numPredict;

    private Integer seed;

    private List<String> stop;

    @JsonProperty("repeat_penalty")
    private Double repeatPenalty;

    @JsonProperty("presence_penalty")
    private Double presencePenalty;

    @JsonProperty("frequency_penalty")
    private Double frequencyPenalty;

    @JsonProperty("num_ctx")
    private Integer numCtx;

    @JsonProperty("num_gpu")
    private Integer numGpu;

    @JsonProperty("num_thread")
    private Integer numThread;
}
