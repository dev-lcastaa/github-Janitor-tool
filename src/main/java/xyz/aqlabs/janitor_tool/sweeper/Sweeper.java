package xyz.aqlabs.janitor_tool.sweeper;

import xyz.aqlabs.janitor_tool.models.out.DeletedGitHubBranch;
import xyz.aqlabs.janitor_tool.models.SweeperStatus;

import java.util.List;
import java.util.Map;

public interface Sweeper {

    SweeperStatus sweep(String orgId);

}
