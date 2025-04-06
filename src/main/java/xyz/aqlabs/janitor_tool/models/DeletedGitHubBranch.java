package xyz.aqlabs.janitor_tool.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DeletedGitHubBranch {

    private String organization;
    private String repoName;
    private String branchName;
    private LocalDate lastActivity;
    private String authorName;
    private String authorEmail;
    private DeletionStatus status;

}
