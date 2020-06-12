package com.cosmoscalipers.driver;

import com.azure.cosmos.*;
import com.azure.cosmos.models.*;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.cosmoscalipers.workload.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LoadRunner {

    private static final MetricRegistry metrics = new MetricRegistry();
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadRunner.class);
    private enum Workflow {ASYNC, SYNC};

    public void execute(BenchmarkConfig config) {

        ScheduledReporter reporter = startReport(config.getReporter());
        String operation = config.getOperation();

        switch(operation) {

            case Constants.CONST_OPERATION_SQL_ASYNC_PARTITION_KEY_READ:
                executeWorkflow(Workflow.ASYNC, Constants.CONST_OPERATION_SQL_ASYNC_PARTITION_KEY_READ, config, true);
                break;

            case Constants.CONST_OPERATION_SQL_SYNC_PARTITION_KEY_READ:
                executeWorkflow(Workflow.SYNC, Constants.CONST_OPERATION_SQL_SYNC_PARTITION_KEY_READ, config, true);
                break;

            case Constants.CONST_OPERATION_SQL_ASYNC_POINT_READ:
                executeWorkflow(Workflow.ASYNC, Constants.CONST_OPERATION_SQL_ASYNC_POINT_READ, config, true);
                break;

            case Constants.CONST_OPERATION_SQL_SYNC_POINT_READ:
                executeWorkflow(Workflow.SYNC, Constants.CONST_OPERATION_SQL_SYNC_POINT_READ, config, true);
                break;

            case Constants.CONST_OPERATION_SQL_ASYNC_UPSERT:
                executeWorkflow(Workflow.ASYNC, Constants.CONST_OPERATION_SQL_ASYNC_UPSERT, config, true);
                break;

            case Constants.CONST_OPERATION_SQL_SYNC_UPSERT:
                executeWorkflow(Workflow.SYNC, Constants.CONST_OPERATION_SQL_SYNC_UPSERT, config, true);
                break;

            case Constants.CONST_OPERATION_ALL_SYNC_OPS:
                executeWorkflow(Workflow.SYNC, Constants.CONST_OPERATION_ALL_SYNC_OPS, config, true);
                break;

            case Constants.CONST_OPERATION_ALL_ASYNC_OPS:
                executeWorkflow(Workflow.ASYNC, Constants.CONST_OPERATION_ALL_ASYNC_OPS, config, true);
                break;

            case Constants.CONST_OPERATION_SQL_ALL:
                executeWorkflow(Workflow.SYNC, Constants.CONST_OPERATION_ALL_SYNC_OPS, config, true);
                executeWorkflow(Workflow.ASYNC, Constants.CONST_OPERATION_ALL_ASYNC_OPS, config, false);

        }

        publishMetrics(reporter);

    }

    private static void executeWorkflow(Workflow workflow, String operation, BenchmarkConfig config, boolean isContainerDeleted) {

        int maxPoolSize = config.getMaxPoolSize();
        int maxRetryAttempts = config.getMaxRetryAttempts();
        int retryWaitTimeInSeconds = config.getRetryWaitTimeInSeconds();
        int numberOfItems = config.getNumberOfDocuments();
        int payloadSize = config.getPayloadSize();
        int provisionedRUs = config.getProvisionedRUs();

        String hostName = config.getHost();
        String databaseName = config.getDatabaseId();
        String collection = config.getCollectionId();
        String masterKey = config.getMasterKey();
        ConsistencyLevel consistencyLevel = config.getConsistencyLevel();

        SyncBootstrap syncBootstrap = new SyncBootstrap();
        AsyncBootstrap asyncBootstrap = new AsyncBootstrap();
        List<String> orderIdList = null;
        CosmosAsyncClient asyncClient = null;
        CosmosClient syncClient = null;
        CosmosAsyncDatabase asyncDatabase = null;
        CosmosDatabase syncDatabase = null;
        CosmosContainer container = null;
        CosmosAsyncContainer asyncContainer = null;

        System.out.println("Payload size : " + config.getPayloadSize());

        if(workflow == Workflow.ASYNC) {

            asyncClient = buildCosmosAsyncClient(ConnectionMode.DIRECT, maxPoolSize, maxRetryAttempts,
                    retryWaitTimeInSeconds, hostName, masterKey, consistencyLevel);
            asyncDatabase = getDB(asyncClient, databaseName);
            if (isContainerDeleted) {
                deleteContainer(asyncDatabase, collection);
            }
            asyncContainer = setupContainer(asyncDatabase, collection, provisionedRUs);
            orderIdList = asyncBootstrap.createDocs(asyncContainer, numberOfItems, payloadSize, metrics);

        } else {

            syncClient = buildCosmosClient(ConnectionMode.DIRECT, maxPoolSize, maxRetryAttempts,
                    retryWaitTimeInSeconds, hostName, masterKey, consistencyLevel);
            syncDatabase = getDB(syncClient, databaseName);
            if (isContainerDeleted) {
                deleteContainer(syncDatabase, collection);
            }
            container = setupContainer(syncDatabase, collection, provisionedRUs);
            orderIdList = syncBootstrap.createDocs(container, numberOfItems, payloadSize, metrics);

        }

        switch(operation) {

            case Constants.CONST_OPERATION_SQL_ASYNC_PARTITION_KEY_READ:
                sqlAsyncReadWorkload(asyncContainer, orderIdList, numberOfItems, metrics);
                break;

            case Constants.CONST_OPERATION_SQL_SYNC_PARTITION_KEY_READ:
                sqlSyncReadWorkload(container, orderIdList, numberOfItems, metrics);
                break;

            case Constants.CONST_OPERATION_SQL_SYNC_POINT_READ:
                sqlSyncPointReadWorkload(container, orderIdList, numberOfItems, metrics);
                break;

            case Constants.CONST_OPERATION_SQL_ASYNC_POINT_READ:
                sqlAsyncPointReadWorkload(asyncContainer, orderIdList, numberOfItems, metrics);
                break;

            case Constants.CONST_OPERATION_SQL_ASYNC_UPSERT:
                sqlAsyncUpsertWorkload(asyncContainer, orderIdList, numberOfItems, metrics);
                break;

            case Constants.CONST_OPERATION_SQL_SYNC_UPSERT:
                sqlSyncUpsertWorkload(container, orderIdList, numberOfItems, metrics);
                break;

            case Constants.CONST_OPERATION_ALL_SYNC_OPS:
                sqlSyncReadWorkload(container, orderIdList, numberOfItems, metrics);
                sqlSyncPointReadWorkload(container, orderIdList, numberOfItems, metrics);
                sqlSyncUpsertWorkload(container, orderIdList, numberOfItems, metrics);
                break;

            case Constants.CONST_OPERATION_ALL_ASYNC_OPS:
                sqlAsyncReadWorkload(asyncContainer, orderIdList, numberOfItems, metrics);
                sqlAsyncPointReadWorkload(asyncContainer, orderIdList, numberOfItems, metrics);
                sqlAsyncUpsertWorkload(asyncContainer, orderIdList, numberOfItems, metrics);
                break;

        }

        if(workflow == Workflow.ASYNC) {
            sqlAsyncDeleteWorkload(asyncContainer, orderIdList, numberOfItems, metrics);
            asyncClient.close();
        } else {
            sqlSyncDeleteWorkload(container, orderIdList, numberOfItems, metrics);
            syncClient.close();
        }

    }

    private static void sqlAsyncReadWorkload(CosmosAsyncContainer container, List<String> orderIdList, int numberOfItems, MetricRegistry metrics ) {
        SQLAsyncRead SQLAsyncReader = new SQLAsyncRead();
        SQLAsyncReader.execute(container, orderIdList, numberOfItems, metrics);
    }

    private static void sqlSyncReadWorkload(CosmosContainer container, List<String> orderIdList, int numberOfItems, MetricRegistry metrics ) {
        SQLSyncRead sqlSyncRead = new SQLSyncRead();
        sqlSyncRead.execute(container, orderIdList, numberOfItems, metrics);
    }

    private static void sqlSyncPointReadWorkload(CosmosContainer container, List<String> orderIdList, int numberOfItems, MetricRegistry metrics ) {
        SQLSyncPointRead pointRead = new SQLSyncPointRead();
        pointRead.execute(container, orderIdList, numberOfItems, metrics);
    }

    private static void sqlAsyncPointReadWorkload(CosmosAsyncContainer container, List<String> orderIdList, int numberOfItems, MetricRegistry metrics ) {
        SQLAsyncPointRead pointRead = new SQLAsyncPointRead();
        pointRead.execute(container, orderIdList, numberOfItems, metrics);
    }

    private static void sqlSyncDeleteWorkload(CosmosContainer container, List<String> orderIdList, int numberOfItems, MetricRegistry metrics ) {
        SQLSyncDelete sqlSyncDelete = new SQLSyncDelete();
        sqlSyncDelete.execute(container, orderIdList, numberOfItems, metrics);
    }

    private static void sqlAsyncDeleteWorkload(CosmosAsyncContainer container, List<String> orderIdList, int numberOfItems, MetricRegistry metrics ) {
        SQLAsyncDelete sqlAsyncDelete = new SQLAsyncDelete();
        sqlAsyncDelete.execute(container, orderIdList, numberOfItems, metrics);
    }

    private static void sqlSyncUpsertWorkload(CosmosContainer container, List<String> orderIdList, int numberOfItems, MetricRegistry metrics ) {
        SQLSyncUpsert sqlSyncUpsert = new SQLSyncUpsert();
        sqlSyncUpsert.execute(container, orderIdList, numberOfItems, metrics);
    }

    private static void sqlAsyncUpsertWorkload(CosmosAsyncContainer container, List<String> orderIdList, int numberOfItems, MetricRegistry metrics ) {
        SQLAsyncUpsert sqlAsyncUpsert = new SQLAsyncUpsert();
        sqlAsyncUpsert.execute(container, orderIdList, numberOfItems, metrics);
    }

    private static CosmosDatabase getDB(CosmosClient client, String database) {
        return client.getDatabase(database);
    }

    private static CosmosAsyncDatabase getDB(CosmosAsyncClient client, String database) {
        return client.getDatabase(database);
    }

    private static ThrottlingRetryOptions getRetryOptions(int maxRetryAttempts,
                                                          int retryWaitTimeInSeconds) {
        ThrottlingRetryOptions retryOptions = new ThrottlingRetryOptions();
        retryOptions.setMaxRetryAttemptsOnThrottledRequests(maxRetryAttempts);
        retryOptions.setMaxRetryWaitTime(Duration.ofSeconds(retryWaitTimeInSeconds));

        return retryOptions;
    }

    private static CosmosAsyncClient buildCosmosAsyncClient(ConnectionMode connectionMode, int maxPoolSize, int maxRetryAttempts,
                                                  int retryWaitTimeInSeconds, String hostName, String masterKey, ConsistencyLevel consistencyLevel ) {

        ThrottlingRetryOptions retryOptions = getRetryOptions(maxRetryAttempts, retryWaitTimeInSeconds);

        return new CosmosClientBuilder()
            .endpoint(hostName)
            .key(masterKey)
            .directMode(DirectConnectionConfig.getDefaultConfig().setIdleConnectionTimeout(Duration.ofMinutes(15)))
            .throttlingRetryOptions(retryOptions)
            .consistencyLevel( consistencyLevel )
            .buildAsyncClient();

    }

    private static CosmosClient buildCosmosClient(ConnectionMode connectionMode, int maxPoolSize, int maxRetryAttempts,
                                                            int retryWaitTimeInSeconds, String hostName, String masterKey, ConsistencyLevel consistencyLevel ) {

        ThrottlingRetryOptions retryOptions = getRetryOptions(maxRetryAttempts, retryWaitTimeInSeconds);

        return new CosmosClientBuilder()
                .endpoint(hostName)
                .key(masterKey)
                .directMode(DirectConnectionConfig.getDefaultConfig().setIdleConnectionTimeout(Duration.ofMinutes(15)))
                .throttlingRetryOptions(retryOptions)
                .consistencyLevel( consistencyLevel )
                .buildClient();

    }

    private static void deleteContainer(CosmosAsyncDatabase db, String collection) {

        CosmosAsyncContainer container;

        try {
            container = db.getContainer(collection);
            container.delete().block();
        } catch(Exception e) {
            System.out.println("Container " + collection + " doesn't exist");
        }

    }

    private static void deleteContainer(CosmosDatabase db, String collection) {

        CosmosContainer container;

        try {
            container = db.getContainer(collection);
            container.delete();
        } catch(Exception e) {
            System.out.println("Container " + collection + " doesn't exist");
        }

    }

    private static CosmosContainer setupContainer(CosmosDatabase db, String collection, int provisionedRUs) {

        CosmosContainer container;
        CosmosContainerProperties cosmosContainerProperties = getCosmosContainerProperties(collection);
        CosmosContainerResponse cosmosContainerResponse = db.createContainerIfNotExists(cosmosContainerProperties,
                ThroughputProperties.createManualThroughput(provisionedRUs));
        container = db.getContainer(collection);

        return container;

    }

    private static CosmosAsyncContainer setupContainer(CosmosAsyncDatabase db, String collection, int provisionedRUs) {

        CosmosAsyncContainer container;
        CosmosContainerProperties cosmosContainerProperties = getCosmosContainerProperties(collection);
        CosmosContainerResponse cosmosContainerResponse = db.createContainerIfNotExists(cosmosContainerProperties, ThroughputProperties.createManualThroughput(provisionedRUs)).block();

        container = db.getContainer(collection);

        return container;

    }

    private static CosmosContainerProperties getCosmosContainerProperties(String collection) {
        CosmosContainerProperties cosmosContainerProperties = new CosmosContainerProperties(collection, Constants.CONST_PARTITION_KEY);
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.setIndexingMode(IndexingMode.CONSISTENT);

        List<IncludedPath> includedPaths = new ArrayList<>();
        IncludedPath includedPath = new IncludedPath(Constants.CONST_PARTITION_KEY + "/*");
        includedPaths.add(includedPath);
        indexingPolicy.setIncludedPaths(includedPaths);

        List<ExcludedPath> excludedPaths = new ArrayList<>();
        ExcludedPath excludedPath2 = new ExcludedPath("/*");
        excludedPaths.add(excludedPath2);
        indexingPolicy.setExcludedPaths(excludedPaths);

        cosmosContainerProperties.setIndexingPolicy(indexingPolicy);
        return cosmosContainerProperties;
    }


    private static void publishMetrics(ScheduledReporter reporter) {
        try {
            Thread.sleep(5 * 1000);
            reporter.report();
            reporter.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static ScheduledReporter startReport(String resultsReporter) {

        ScheduledReporter reporter = null;

        if (resultsReporter.equalsIgnoreCase(Constants.CONST_CONSOLEREPORTER)) {
            reporter = ConsoleReporter.forRegistry(metrics)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build();

        } else if (resultsReporter.equalsIgnoreCase(Constants.CONST_CSVREPORTER)) {

            File directory = new File(Constants.CONST_CSVFILES_LOCATION);
            if(!directory.exists()) {
                directory.mkdir();
            }

            reporter = CsvReporter.forRegistry(metrics)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build(directory);
        }

        return reporter;
    }

}
