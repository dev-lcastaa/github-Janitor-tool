package xyz.aqlabs.janitor_tool.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xyz.aqlabs.janitor_tool.models.SweeperStatus;

@Slf4j
@Component
public class Scheduler {

    //Scheduled job
    @Scheduled(cron = " * * * * ")
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
        return SweeperStatus.SUCCESSFUL;
    }


}
