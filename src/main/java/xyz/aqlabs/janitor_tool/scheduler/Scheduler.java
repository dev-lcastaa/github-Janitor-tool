package xyz.aqlabs.janitor_tool.scheduler;


import ch.qos.logback.core.net.server.Client;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import xyz.aqlabs.janitor_tool.models.SweeperStatus;
import xyz.aqlabs.janitor_tool.sweeper.GitHubSweeper;
import xyz.aqlabs.janitor_tool.wrapper.ClientWrapper;

import java.util.List;

@Slf4j
@Component
public class Scheduler {

    @Autowired
    private ClientWrapper wrapper;

    @Value("${schedule.org.id}")
    private String orgId;

    @Value("${sweeper.branch.dryRun}")
    private boolean branchDryRun;

    @Value("${sweeper.branch.delete.days}")
    private int branchDeleteDays;

    @Value("${sweeper.pullRequest.dryRun}")
    private boolean pullRequestDryRun;

    @Value("${sweeper.pullRequest.delete.days}")
    private int pullRequestDeleteDays;

    @Value("${sweeper.ignore.branches}")
    private List<String> ignoreList;


    //Scheduled job every Monday @ 7am
    @Scheduled(cron = "0 7 * * 1")
    public void scheduledRun(){
        SweeperStatus sweeperStatus = run();
        log.info("Scheduled run status was {}", sweeperStatus);
    }

    //Allows test from controller
    public SweeperStatus triggeredRun(){
        SweeperStatus sweeperStatus = run();
        log.info("Triggered run status was {}", sweeperStatus);
        return sweeperStatus;
    }

    // sweep repos
    private SweeperStatus run(){
        GitHubSweeper sweeper = getGitHubSweeper();

        return sweeper.sweep(orgId);
    }

    // gets a sweeper
    private GitHubSweeper getGitHubSweeper(){
        return new GitHubSweeper(
                branchDryRun,
                pullRequestDryRun,
                branchDeleteDays,
                pullRequestDeleteDays,
                wrapper,
                ignoreList
        );
    }

    @PostConstruct
    public void init(){
        log.info("Scheduler is initialized");
        log.info("Sweeper is on stand by....");
    }


}
