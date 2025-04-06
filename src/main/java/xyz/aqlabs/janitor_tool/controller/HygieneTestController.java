package xyz.aqlabs.janitor_tool.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.aqlabs.janitor_tool.models.SweeperStatus;
import xyz.aqlabs.janitor_tool.scheduler.Scheduler;

@Slf4j
@RestController
@RequestMapping("/api/v1/janitor")
public class HygieneTestController {

    @Autowired
    private Scheduler scheduler;

    @GetMapping("/run")
    public ResponseEntity<?> testRunJanitor(){
       SweeperStatus status = scheduler.triggeredRun();
        if(status.equals(SweeperStatus.SUCCESSFUL))
            return ResponseEntity.ok("{\"msg\" : \"Manual trigger of sweeper ran successfully\"}");
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }





}
