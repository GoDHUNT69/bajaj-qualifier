package com.example.bfhl.dto;

public class SolutionPayload {

    private String finalQuery;

    public SolutionPayload() {
    }

    public SolutionPayload(String finalQuery) {
        this.finalQuery = finalQuery;
    }

    public String getFinalQuery() {
        return finalQuery;
    }

    public void setFinalQuery(String finalQuery) {
        this.finalQuery = finalQuery;
    }
}
