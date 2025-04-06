package xyz.aqlabs.janitor_tool.sweeper;

import xyz.aqlabs.janitor_tool.models.SweeperStatus;

public interface Sweeper {

    SweeperStatus sweep(String orgId);

}
