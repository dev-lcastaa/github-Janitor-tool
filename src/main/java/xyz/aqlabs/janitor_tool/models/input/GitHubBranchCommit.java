package xyz.aqlabs.janitor_tool.models.input;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@RequiredArgsConstructor
public class GitHubBranchCommit {

    @JsonProperty("sha")
    private String sha;

    @JsonProperty("commit")
    private GitHubCommit commit;

}
