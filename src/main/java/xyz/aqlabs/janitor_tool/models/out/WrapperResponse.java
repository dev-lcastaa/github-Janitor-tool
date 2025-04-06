package xyz.aqlabs.janitor_tool.models.out;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WrapperResponse {
    private String response;
    private int responseCode;
}
