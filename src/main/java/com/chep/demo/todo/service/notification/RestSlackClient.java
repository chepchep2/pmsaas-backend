package com.chep.demo.todo.service.notification;

import com.chep.demo.todo.exception.notification.NonRetryableSlackException;
import com.chep.demo.todo.exception.notification.RetryableSlackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Service
public class RestSlackClient implements SlackClient {
    private static final Logger log = LoggerFactory.getLogger(RestSlackClient.class);
    private final RestClient restClient;

    public RestSlackClient(RestClient.Builder restClientBuilder, @Value("${slack.webhook-url}") String webhookUrl) {
        this.restClient = restClientBuilder.baseUrl(webhookUrl).build();
    }

    public void send(String message) {
        log.info("Sending Slack message: {}", message);
        try {
            String responseBody = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("text", message))
                    .retrieve()
                    .body(String.class);
            log.info("Slack message sent. response={}", responseBody);

        } catch (RestClientResponseException e) {
            log.error("Slack responded with error. status={} body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            if (e.getStatusCode().value() == 429 || e.getStatusCode().is5xxServerError()) {
                throw new RetryableSlackException(e.getMessage());
            } else {
                throw new NonRetryableSlackException(e.getMessage());
            }
        } catch (ResourceAccessException e) {
            log.error("Slack request failed due to I/O issue (timeout/connection).", e);
            throw new RetryableSlackException(e.getMessage());
        }
    }
}
