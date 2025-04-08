package xyz.aqlabs.janitor_tool.sweeper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import xyz.aqlabs.janitor_tool.models.DeletedGitHubBranch;
import xyz.aqlabs.janitor_tool.models.DeletionStatus;
import xyz.aqlabs.janitor_tool.models.SweeperStatus;
import xyz.aqlabs.janitor_tool.models.input.GitHubBranchCommit;
import xyz.aqlabs.janitor_tool.models.input.GitHubRepo;
import xyz.aqlabs.janitor_tool.models.input.GitHubRepoBranch;
import xyz.aqlabs.janitor_tool.utils.Tools;
import xyz.aqlabs.janitor_tool.wrapper.ClientWrapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static xyz.aqlabs.janitor_tool.utils.Constants.*;

@Slf4j
public class GitHubSweeper implements Sweeper{

    private final boolean branchDryRun;
    private final boolean pullRequestDryRun;
    private final int deleteBranchDaysOld;
    private final int deletePRsDaysOld;
    private final ClientWrapper wrapper;
    private final List<String> ignoreList;

    private List<DeletedGitHubBranch> deletedGitHubBranches = new ArrayList<>();

    public GitHubSweeper(boolean branchDryRun, boolean pullRequestDryRun, int deleteBranchDaysOld, int deletePRsDaysOld, ClientWrapper wrapper, List<String> ignoreList) {
        this.branchDryRun = branchDryRun;
        this.pullRequestDryRun = pullRequestDryRun;
        this.deleteBranchDaysOld = deleteBranchDaysOld;
        this.deletePRsDaysOld = deletePRsDaysOld;
        this.wrapper = wrapper;
        this.ignoreList = ignoreList;
    }

    @Override
    public Map<SweeperStatus, List<DeletedGitHubBranch>> sweep(String orgId){

        log.info("Sweeping organization with ID: {}", orgId);
        List<GitHubRepo> repos = getOrgRepos(orgId);
        if(repos == null)
            return Map.of(SweeperStatus.FAILED, new ArrayList<>());


        repos.forEach(repo -> {
            log.info("Processing Repository {}",repo.getName());
            log.info("Getting branches.....");
            List<GitHubRepoBranch> branches = getRepoBranches(repo.getBranchesUrl());
            if(branches == null)
                throw new RuntimeException("Repo branches was null");
           processBranches(repo, branches);
        });

        //close PRs

        log.info("Now deleting collected branches....");
        // delete branches
        deleteBranches();

        return Map.of(SweeperStatus.SUCCESSFUL, deletedGitHubBranches);
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

    // gets list of pull requests
    private List<?> getPullRequests(String url){
        return new ArrayList<>();
    }

    // process branches from repos
    private void processBranches(GitHubRepo repo, List<GitHubRepoBranch> branches) {

        LocalDate deleteFrom = Tools.getLocalDateThreshHold(deleteBranchDaysOld);

        //filter main & develop branches
        branches.removeIf(gitHubRepoBranch -> ignoreList.contains(gitHubRepoBranch.getName()));

        branches.forEach(branch -> {
            List<GitHubBranchCommit> commits = getBranchCommits(repo.getBranchesUrl(), branch.getBranchCommit().getSha());
            if (commits == null)
                throw new RuntimeException("Failed to get commits from " + repo.getName());
            log.info("Latest commit on branch is {}", commits.get(0).getCommit());
            LocalDate lastCommitDate = Tools.getLocalDateFrom(commits.get(0).getCommit().getCommiter().getDate());
            if (lastCommitDate.isBefore(deleteFrom)) {
                DeletedGitHubBranch deletedGitHubBranch = new DeletedGitHubBranch(
                        getOrg(repo.getUrl()),
                        repo.getName(),
                        branch.getName(),
                        lastCommitDate,
                        commits.get(0).getCommit().getAuthor().getName(),
                        commits.get(0).getCommit().getAuthor().getEmail(),
                        DeletionStatus.TBD

                );
                deletedGitHubBranches.add(deletedGitHubBranch);
            }
        });
    }

    // deleteBranches
    private void deleteBranches(){
        if(branchDryRun)
            deletedGitHubBranches.forEach(branch -> {
                log.info("[DRY-RUN] Delete branch {} @ {}", branch.getRepoName(), branch.getBranchName());
                branch.setStatus(DeletionStatus.DELETE_MANUALLY);
            });

        if(!branchDryRun)
            deletedGitHubBranches.forEach(branch -> {
            log.info("Deleting branch {} @ {}", branch.getRepoName(), branch.getBranchName());
            wrapper.delete(
                    GITHUB_API_DELETE_BRANCH_API.formatted(
                            branch.getOrganization(),
                            branch.getRepoName(),
                            branch.getBranchName()
                    )
            );
        });
    }



    private String getOrg(String url){
        return url.split("/")[4];

    }

}
