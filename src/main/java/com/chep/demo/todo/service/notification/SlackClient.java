package com.chep.demo.todo.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class SlackClient {
    private static final Logger log = LoggerFactory.getLogger(SlackClient.class);
    private final WebClient webClient;

    public SlackClient(@Value("${slack.webhook-url}") String webhookUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(webhookUrl)
                .build();
    }

    public void send(String message) {
        log.info("Sending Slack message: {}", message);
        webClient.post()
                .bodyValue(Map.of("text", message))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        log.info("Slack message sent successfully");
    }
}
