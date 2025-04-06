package xyz.aqlabs.janitor_tool.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class Tools {

    public static LocalDate getLocalDateFrom(String gitHubTimestamp){
        Instant instant = Instant.parse(gitHubTimestamp);
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDate getLocalDateThreshHold(long daysToDelete){
        return LocalDate.now().minusDays(daysToDelete);
    }



}
