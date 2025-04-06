package xyz.aqlabs.janitor_tool.wrapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import xyz.aqlabs.janitor_tool.models.out.WrapperResponse;

@Slf4j
@Component
public class ClientWrapper {

    private final RestTemplate restTemplate;

    @Value("${sweeper.api.key}")
    private String key;

    @Autowired
    public ClientWrapper() {
        this.restTemplate = new RestTemplate();
    }

    // Method for GET request
    public WrapperResponse get(String url) {
        log.info("Request received GET {}", url);
        HttpHeaders httpHeaders = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        if(!response.hasBody())
            throw new RuntimeException();
        return new WrapperResponse(
                response.getBody(),
                response.getStatusCode().value()
        );
    }

    // Method for DELETE request
    public WrapperResponse delete(String url) {
        log.info("Request received DELETE {}", url);
        HttpHeaders httpHeaders = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
        if(!response.hasBody())
            throw new RuntimeException();
        return new WrapperResponse(
                response.getBody(),
                response.getStatusCode().value()
        );
    }

    // Helper method to create headers
    private HttpHeaders createHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(key);
        return httpHeaders;
    }


}
