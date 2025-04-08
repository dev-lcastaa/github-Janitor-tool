package xyz.aqlabs.janitor_tool.models.input;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@RequiredArgsConstructor
public class GitHubPullRequest {

    @JsonProperty("url")
    private String url;

    @JsonProperty("id")
    private String id;

    @JsonProperty("number")
    private String prNumber;

    @JsonProperty("state")
    private String state;

    @JsonProperty("user")
    private GitHubUser user;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("merged_at")
    private String merged_at;

}
