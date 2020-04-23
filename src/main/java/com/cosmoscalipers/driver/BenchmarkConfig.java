package com.cosmoscalipers.driver;

public class BenchmarkConfig {
    private String host;
    private String databaseId;
    private String collectionId;
    private String masterKey;
    private long numberOfDocuments;
    private String samplePayload;
    private String consistencyLevel;
    private int provisionedRUs;
    private int maxPoolSize;
    private int maxRetryAttempts;
    private int retryWaitTimeInSeconds;
    private String operation;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getMasterKey() {
        return masterKey;
    }

    public void setMasterKey(String masterKey) {
        this.masterKey = masterKey;
    }

    public long getNumberOfDocuments() {
        return numberOfDocuments;
    }

    public void setNumberOfDocuments(long numberOfDocuments) {
        this.numberOfDocuments = numberOfDocuments;
    }

    public String getSamplePayload() {
        return samplePayload;
    }

    public void setSamplePayload(String samplePayload) {
        this.samplePayload = samplePayload;
    }

    public String getConsistencyLevel() {
        return consistencyLevel;
    }

    public void setConsistencyLevel(String consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
    }

    public int getProvisionedRUs() {
        return provisionedRUs;
    }

    public void setProvisionedRUs(int provisionedRUs) {
        this.provisionedRUs = provisionedRUs;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    public void setMaxRetryAttempts(int maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
    }

    public int getRetryWaitTimeInSeconds() {
        return retryWaitTimeInSeconds;
    }

    public void setRetryWaitTimeInSeconds(int retryWaitTimeInSeconds) {
        this.retryWaitTimeInSeconds = retryWaitTimeInSeconds;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(String databaseId) {
        this.databaseId = databaseId;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }
}
