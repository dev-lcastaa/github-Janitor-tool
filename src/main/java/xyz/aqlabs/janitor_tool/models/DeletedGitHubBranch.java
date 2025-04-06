package xyz.aqlabs.janitor_tool.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import xyz.aqlabs.janitor_tool.models.input.GitHubAuthor;

import java.time.LocalDate;

@Setter
@Getter
@RequiredArgsConstructor
public class DeletedGitHubBranch {

    private String repoUrl;
    private String branchName;
    private GitHubAuthor author;
    private LocalDate lastActivity;

}
