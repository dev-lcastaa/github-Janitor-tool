package xyz.aqlabs.janitor_tool.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import xyz.aqlabs.janitor_tool.models.ResultsType;
import xyz.aqlabs.janitor_tool.models.out.ClosedGitHubPullRequest;
import xyz.aqlabs.janitor_tool.models.out.DeletedGitHubBranch;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DiscordNotifier {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${sweeper.discord.notify.endpoint}")
    private String webhookEndpoint;

    public void sendResults(Map<ResultsType, List<?>> results) {
        log.info("Results received...");
        List<ClosedGitHubPullRequest> prs = new ArrayList<>();
        List<DeletedGitHubBranch> branches = new ArrayList<>();

        if(results.containsKey(ResultsType.CLOSED_PRS)){
            Object value = results.get(ResultsType.CLOSED_PRS);
            prs = (List<ClosedGitHubPullRequest>) value;
        }
        if(results.containsKey(ResultsType.DELETED_BRANCHES)){
            Object value = results.get(ResultsType.DELETED_BRANCHES);
            branches = (List<DeletedGitHubBranch>) value;
        }
        String message = combineDiscordMessage(
                buildDiscordPullRequestMessage(prs),
                buildDiscordBranchMessage(branches)
        );

        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("content", message);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(webhookEndpoint, request, String.class);
            log.info("Discord response: {}",response.getStatusCode());
        } catch (Exception e) {
            throw new RuntimeException("Failed to send discord message....");
        }


    }

    private String combineDiscordMessage(String pullRequest, String deletedBranches){
        String header = "🧹 **AQLabs Hygiene Report: %s **\n\n".formatted(LocalDate.now());
        return header + pullRequest + deletedBranches;
    }

    private String buildDiscordBranchMessage(List<DeletedGitHubBranch> deletedBranches) {
        log.info("Building Branches report....");
        if (deletedBranches.isEmpty()) {
            return "✅ No branches were deleted during this sweep.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🔀**Branch Sweep Summary**\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        Map<String, List<DeletedGitHubBranch>> grouped = deletedBranches.stream()
                .collect(Collectors.groupingBy(DeletedGitHubBranch::getRepoName));

        for (Map.Entry<String, List<DeletedGitHubBranch>> entry : grouped.entrySet()) {
            sb.append("📁 **Repository:** `").append(entry.getKey()).append("`\n");
            sb.append("\t│\n");
            for (DeletedGitHubBranch branch : entry.getValue()) {
                sb.append("\t├─**Branch:** `").append(branch.getBranchName()).append("`\n")
                        .append("\t│\t\t\t├─👤 **Author:** ").append(branch.getAuthorName()).append("\n")
                        .append("\t│\t\t\t├─📅 **Last Activity:** ").append(branch.getLastActivity()).append("\n")
                        .append("\t│\t\t\t└─🗑️ **Status:** ").append(branch.getStatus()).append("\n\n");
            }
        }
        log.info("Branches reported {}", deletedBranches.size());
        return sb.toString();
    }

    private String buildDiscordPullRequestMessage(List<ClosedGitHubPullRequest> closedPullRequests) {
        log.info("Building pull request report....");
        if (closedPullRequests.isEmpty()) {
            return "✅ No pull requests were closed during this sweep.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🔃 **Pull Request Sweep Summary**\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        // Grouping PRs by repo name extracted from URL
        Map<String, List<ClosedGitHubPullRequest>> grouped = closedPullRequests.stream()
                .collect(Collectors.groupingBy(pr -> extractRepoNameFromUrl(pr.getUrl())));

        for (Map.Entry<String, List<ClosedGitHubPullRequest>> entry : grouped.entrySet()) {
            sb.append("📁 **Repository:** `").append(entry.getKey()).append("`\n");
            sb.append("\t│\n");
            for (ClosedGitHubPullRequest pr : entry.getValue()) {
                sb.append("\t├─**PR #:** `").append(pr.getPrNumber()).append("`\n")
                        .append("\t│\t\t├─👤 **User:** ").append(pr.getUser()).append("\n")
                        .append("\t│\t\t├─🔗 **URL:** ").append(pr.getUrl()).append("\n")
                        .append("\t│\t\t└─🗑️ **Status:** ").append(pr.getStatus()).append("\n\n");
            }
        }

        sb.append("\n");
        log.info("Pull requests reported {}", closedPullRequests.size());
        return sb.toString();
    }

    private String extractRepoNameFromUrl(String url) {
        try {
            // Extracts 'org/repo' from the URL
            URI uri = new URI(url);
            String[] segments = uri.getPath().split("/");
            return segments.length >= 3 ? segments[1] + "/" + segments[2] : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }
}