package xyz.aqlabs.janitor_tool.models.out;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClosedGitHubPullRequest {

    private String user;
    private String url;
    private String prNumber;
    private PullRequestStatus status;

}
