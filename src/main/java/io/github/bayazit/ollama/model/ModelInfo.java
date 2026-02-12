package io.github.bayazit.ollama.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelInfo {

    private String name;

    @JsonProperty("modified_at")
    private String modifiedAt;

    private Long size;
    private String digest;
    private Map<String, Object> details;
}
