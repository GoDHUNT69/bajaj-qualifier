package com.example.bfhl.dto;

public class GenerateWebhookResponse {

    private String webhook;
    private String accessToken;

    public GenerateWebhookResponse() {
    }

    public String getWebhook() {
        return webhook;
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String toString() {
        return "GenerateWebhookResponse{" +
                "webhook='" + webhook + '\'' +
                ", accessToken='" + accessToken + '\'' +
                '}';
    }
}
