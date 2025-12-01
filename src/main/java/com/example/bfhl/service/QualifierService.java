package com.example.bfhl.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.bfhl.config.QualifierProperties;
import com.example.bfhl.dto.GenerateWebhookRequest;
import com.example.bfhl.dto.GenerateWebhookResponse;
import com.example.bfhl.dto.SolutionPayload;
import com.example.bfhl.sql.SqlQueries;

@Service
public class QualifierService {

    private static final Logger log = LoggerFactory.getLogger(QualifierService.class);

    private final RestTemplate restTemplate;
    private final QualifierProperties properties;

    public QualifierService(RestTemplate restTemplate, QualifierProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public void executeFlow() {
        try {
            // 1. Call generateWebhook/JAVA
            GenerateWebhookResponse response = callGenerateWebhook();

            if (response == null || response.getWebhook() == null || response.getAccessToken() == null) {
                log.error("Invalid response from generateWebhook: {}", response);
                return;
            }

            String webhookUrl = response.getWebhook();
            String accessToken = response.getAccessToken();

            // 2. Decide Question 1 vs Question 2 based on last 2 digits of regNo
            String finalQuery = buildSqlForRegNo(properties.getRegNo());

            log.info("Using finalQuery:\n{}", finalQuery);

            // 3. Send finalQuery to webhook with JWT token in Authorization header
            sendSolution(webhookUrl, accessToken, finalQuery);

        } catch (Exception e) {
            log.error("Error executing qualifier flow", e);
        }
    }

    private GenerateWebhookResponse callGenerateWebhook() {
        String url = properties.getGenerateUrl();

        GenerateWebhookRequest requestBody = new GenerateWebhookRequest(
                properties.getName(),
                properties.getRegNo(),
                properties.getEmail()
        );

        log.info("Calling generateWebhook at {} with regNo={}", url, properties.getRegNo());

        ResponseEntity<GenerateWebhookResponse> responseEntity =
                restTemplate.postForEntity(url, requestBody, GenerateWebhookResponse.class);

        log.info("generateWebhook status: {}", responseEntity.getStatusCode());

        return responseEntity.getBody();
    }

    /**
     * Last two digits:
     *  - Odd  → Question 1
     *  - Even → Question 2 (your Employee query)
     */
    private String buildSqlForRegNo(String regNo) {
        if (regNo == null || regNo.isBlank()) {
            throw new IllegalArgumentException("regNo must not be null/blank");
        }

        String digits = regNo.replaceAll("\\D", "");
        if (digits.length() == 0) {
            throw new IllegalArgumentException("regNo must contain digits");
        }

        String lastTwoStr = digits.length() >= 2
                ? digits.substring(digits.length() - 2)
                : digits;

        int lastTwo = Integer.parseInt(lastTwoStr);
        boolean isOdd = (lastTwo % 2) != 0;

        log.info("regNo={} -> lastTwo={} -> {}", regNo, lastTwo,
                isOdd ? "Odd → Question 1" : "Even → Question 2");

        return isOdd ? SqlQueries.QUESTION_1_QUERY : SqlQueries.QUESTION_2_QUERY;
    }

    private void sendSolution(String webhookUrl, String accessToken, String finalQuery) {
        SolutionPayload payload = new SolutionPayload(finalQuery);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Spec says: Authorization : <accessToken> (no "Bearer " unless they say so)
        headers.set("Authorization", accessToken);

        HttpEntity<SolutionPayload> requestEntity = new HttpEntity<>(payload, headers);

        log.info("Posting solution to webhook {}", webhookUrl);

        ResponseEntity<String> responseEntity =
                restTemplate.postForEntity(webhookUrl, requestEntity, String.class);

        log.info("Solution POST status: {}", responseEntity.getStatusCode());
        log.info("Response body: {}", responseEntity.getBody());
    }
}
