package com.cosmoscalipers.driver;

import com.azure.cosmos.ConsistencyLevel;

public class BenchmarkConfig {
    private String host;
    private String databaseId;
    private String collectionId;
    private String masterKey;
    private int numberOfDocuments;
    private int payloadSize;
    private ConsistencyLevel consistencyLevel;
    private int provisionedRUs;
    private int maxPoolSize;
    private int maxRetryAttempts;
    private int retryWaitTimeInSeconds;
    private String operation;
    private String reporter;

    private boolean deleteContainer;

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

    public int getNumberOfDocuments() {
        return numberOfDocuments;
    }

    public void setNumberOfDocuments(int numberOfDocuments) {
        this.numberOfDocuments = numberOfDocuments;
    }

    public int getPayloadSize() {
        return payloadSize;
    }

    public void setPayloadSize(int payloadSize) {
        this.payloadSize = payloadSize;
    }

    public ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    public void setConsistencyLevel(ConsistencyLevel consistencyLevel) {
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

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public boolean isDeleteContainer() { return deleteContainer; }

    public void setDeleteContainer(boolean deleteContainer) { this.deleteContainer = deleteContainer; }

}
