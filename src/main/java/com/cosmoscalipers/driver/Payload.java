package com.cosmoscalipers.driver;

public class Payload {
    private String id;
    private String payloadId;
    private String payload;

    public Payload() {
    }

    public Payload(String id, String payloadId, String payload) {
        this.id = id;
        this.payloadId = payloadId;
        this.payload = payload;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPayloadId() {
        return payloadId;
    }

    public void setPayloadId(String payloadId) {
        this.payloadId = payloadId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

}