package com.app.gateway.api.dto;

// 这个类只用来接收 HTTP 请求里的 JSON 数据
public class JobSubmitRequest {

    private String type;
    private String payload;

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}