package xyz.aqlabs.janitor_tool.sweeper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import xyz.aqlabs.janitor_tool.models.SweeperStatus;
import xyz.aqlabs.janitor_tool.models.input.GitHubBranchCommit;
import xyz.aqlabs.janitor_tool.models.input.GitHubCommit;
import xyz.aqlabs.janitor_tool.models.input.GitHubRepo;
import xyz.aqlabs.janitor_tool.models.input.GitHubRepoBranch;
import xyz.aqlabs.janitor_tool.wrapper.ClientWrapper;

import java.util.List;

import static xyz.aqlabs.janitor_tool.utils.Constants.*;

@Slf4j
public class GitHubSweeper implements Sweeper{

    private final boolean branchDryRun;
    private final boolean pullRequestDryRun;
    private final int deleteBranchDaysOld;
    private final int deletePRsDaysOld;
    private final ClientWrapper wrapper;

    public GitHubSweeper(boolean branchDryRun, boolean pullRequestDryRun, int deleteBranchDaysOld, int deletePRsDaysOld, ClientWrapper wrapper) {
        this.branchDryRun = branchDryRun;
        this.pullRequestDryRun = pullRequestDryRun;
        this.deleteBranchDaysOld = deleteBranchDaysOld;
        this.deletePRsDaysOld = deletePRsDaysOld;
        this.wrapper = wrapper;
    }

    @Override
    public SweeperStatus sweep(String orgId){

        log.info("Sweeping organization with ID: {}", orgId);
        List<GitHubRepo> repos = getOrgRepos(orgId);
        if(repos == null)
            return SweeperStatus.FAILED;





        return SweeperStatus.SUCCESSFUL;
    }



    // gets list of repos from the org
    private List<GitHubRepo> getOrgRepos(String orgId){
        ObjectMapper mapper = new ObjectMapper();
        String orgReposApiResponse;
        try{
            orgReposApiResponse = wrapper.get(GITHUB_API_REPOS.formatted(orgId)).getResponse();
        } catch (Exception e) {
            log.error("Failed to get repos from API", e);
            return null;
        }

        List<GitHubRepo> orgRepos;
        try {
            orgRepos = mapper.readValue(orgReposApiResponse, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to map response to organization", e);
            return null;
        }

        return orgRepos;
    }

    // gets list of branches from the repo
    private List<GitHubRepoBranch> getRepoBranches(String url){
        ObjectMapper mapper = new ObjectMapper();
        String orgReposBranchesApiResponse;

        try{
            String cleanedUrl = url.replace(BRANCH_APPENDAGE, "");
            orgReposBranchesApiResponse = wrapper.get(cleanedUrl).getResponse();
        } catch (Exception e) {
            log.error("Failed to get branches from API", e);
            return null;
        }

        List<GitHubRepoBranch> repoBranches;
        try {
            repoBranches = mapper.readValue(orgReposBranchesApiResponse, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to map response to branch obj", e);
            return null;
        }

        return repoBranches;
    }

    // gets list of commits from the branch
    private List<GitHubBranchCommit> getBranchCommits(String url, String shaId){
        ObjectMapper mapper = new ObjectMapper();
        String orgReposBranchesApiResponse;
        String organization = url.split("/")[4];
        String repoName = url.split("/")[5];

        try{
            String formattedUrl = GITHUB_API_COMMITS.formatted(organization, repoName, shaId);
            orgReposBranchesApiResponse = wrapper.get(formattedUrl).getResponse();
        } catch (Exception e) {
            log.error("Failed to get commits from API", e);
            return null;
        }

        List<GitHubBranchCommit> branchCommits;
        try {
            branchCommits = mapper.readValue(orgReposBranchesApiResponse, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to map response to commit obj", e);
            return null;
        }

        return branchCommits;
    }


}
