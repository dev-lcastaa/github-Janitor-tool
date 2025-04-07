package xyz.aqlabs.janitor_tool.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import xyz.aqlabs.janitor_tool.models.DeletedGitHubBranch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DiscordNotifier {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${sweeper.discord.notify.endpoint}")
    private String webhookEndpoint;

    public void sendResults(List<DeletedGitHubBranch> deletedBranches) {
        String message = buildDiscordMessage(deletedBranches);
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("content", message);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(webhookEndpoint, request, String.class);
           log.info("Discord response: {} ", response.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String buildDiscordMessage(List<DeletedGitHubBranch> deletedBranches) {
        if (deletedBranches.isEmpty()) {
            return "✅ No branches were deleted during this sweep.";
        }

        StringBuilder sb = new StringBuilder("🧹 **Branch Sweep Summary:**\n");

        Map<String, List<DeletedGitHubBranch>> grouped = deletedBranches.stream()
                .collect(Collectors.groupingBy(DeletedGitHubBranch::getRepoName));

        for (Map.Entry<String, List<DeletedGitHubBranch>> entry : grouped.entrySet()) {
            sb.append("\n**Repository:** ").append(entry.getKey()).append("\n");

            for (DeletedGitHubBranch branch : entry.getValue()) {
                sb.append("• `").append(branch.getBranchName()).append("`\n")
                        .append("  ├─ 👤 Author: ").append(branch.getAuthorName()).append(" (").append(branch.getAuthorEmail()).append(")\n")
                        .append("  ├─ 🏢 Org: ").append(branch.getOrganization()).append("\n")
                        .append("  ├─ 📆 Last Activity: ").append(branch.getLastActivity()).append("\n")
                        .append("  └─ 🗑️ Status: ").append(branch.getStatus()).append("\n");
            }
        }

        return sb.toString();
    }
}