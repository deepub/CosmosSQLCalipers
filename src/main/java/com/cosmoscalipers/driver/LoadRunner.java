package com.cosmoscalipers.driver;

import com.azure.data.cosmos.*;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import org.apache.commons.cli.CommandLine;
import com.cosmoscalipers.workload.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LoadRunner {

    private static final MetricRegistry metrics = new MetricRegistry();
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadRunner.class);

    public void execute(BenchmarkConfig config) {

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
        String operation = config.getOperation();

        CosmosClient client = buildCosmosClient(ConnectionMode.DIRECT, maxPoolSize, maxRetryAttempts,
                retryWaitTimeInSeconds, hostName, masterKey, consistencyLevel);
        CosmosDatabase database = getDB(client, databaseName);
        CosmosContainer container = null;

        try {
            container = setupContainer(database, collection, provisionedRUs);
        } catch( Exception e) {
            throw e;
        }

        ConsoleReporter consoleReporter = startReport();
        SyncBootstrap syncBootstrap = new SyncBootstrap();
        AsyncBootstrap asyncBootstrap = new AsyncBootstrap();
        List<String> orderIdList = null;

        switch(operation) {
            case Constants.CONST_OPERATION_SQL_ASYNC_PARTITION_KEY_READ:
                orderIdList = asyncBootstrap.createDocs(container, numberOfItems, payloadSize, metrics);
                sqlAsyncReadWorkload(container, orderIdList, numberOfItems, metrics);
                break;

            case Constants.CONST_OPERATION_SQL_SYNC_PARTITION_KEY_READ:
                orderIdList = syncBootstrap.createDocs(container, numberOfItems, payloadSize, metrics);
                sqlSyncReadWorkload(container, orderIdList, numberOfItems, metrics);
                break;

            case Constants.CONST_OPERATION_SQL_SYNC_POINT_READ:
                orderIdList = syncBootstrap.createDocs(container, numberOfItems, payloadSize, metrics);
                sqlSyncPointReadWorkload(container, orderIdList, numberOfItems, metrics);
                break;

            case Constants.CONST_OPERATION_SQL_ASYNC_POINT_READ:
                orderIdList = asyncBootstrap.createDocs(container, numberOfItems, payloadSize, metrics);
                sqlAsyncPointReadWorkload(container, orderIdList, numberOfItems, metrics);
                break;

            case Constants.CONST_OPERATION_SQL_ASYNC_UPDATE:
                orderIdList = asyncBootstrap.createDocs(container, numberOfItems, payloadSize, metrics);
                sqlAsyncUpdateWorkload(container, orderIdList, numberOfItems, metrics);
                break;

            case Constants.CONST_OPERATION_SQL_SYNC_UPDATE:
                orderIdList = syncBootstrap.createDocs(container, numberOfItems, payloadSize, metrics);
                sqlSyncUpdateWorkload(container, orderIdList, numberOfItems, metrics);
                break;

            case Constants.CONST_OPERATION_ALL_SYNC_OPS:
                orderIdList = syncBootstrap.createDocs(container, numberOfItems, payloadSize, metrics);
                sqlSyncReadWorkload(container, orderIdList, numberOfItems, metrics);
                sqlSyncPointReadWorkload(container, orderIdList, numberOfItems, metrics);
                sqlSyncUpdateWorkload(container, orderIdList, numberOfItems, metrics);
                break;

            case Constants.CONST_OPERATION_ALL_ASYNC_OPS:
                orderIdList = asyncBootstrap.createDocs(container, numberOfItems, payloadSize, metrics);
                sqlAsyncReadWorkload(container, orderIdList, numberOfItems, metrics);
                sqlAsyncPointReadWorkload(container, orderIdList, numberOfItems, metrics);
                sqlAsyncUpdateWorkload(container, orderIdList, numberOfItems, metrics);
                break;

            case Constants.CONST_OPERATION_SQL_ALL:
            default:
                orderIdList = syncBootstrap.createDocs(container, numberOfItems, payloadSize, metrics);
                sqlSyncReadWorkload(container, orderIdList, numberOfItems, metrics);
                sqlAsyncReadWorkload(container, orderIdList, numberOfItems, metrics);
                sqlSyncPointReadWorkload(container, orderIdList, numberOfItems, metrics);
                sqlAsyncPointReadWorkload(container, orderIdList, numberOfItems, metrics);
                sqlSyncUpdateWorkload(container, orderIdList, numberOfItems, metrics);
                sqlAsyncUpdateWorkload(container, orderIdList, numberOfItems, metrics);
        }

        sqlSyncDeleteWorkload(container, orderIdList, numberOfItems, metrics);
        publishMetrics(consoleReporter);
        client.close();
    }


    private static void sqlAsyncReadWorkload(CosmosContainer container, List<String> orderIdList, int numberOfItems, MetricRegistry metrics ) {
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

    private static void sqlAsyncPointReadWorkload(CosmosContainer container, List<String> orderIdList, int numberOfItems, MetricRegistry metrics ) {
        SQLAsyncPointRead pointRead = new SQLAsyncPointRead();
        pointRead.execute(container, orderIdList, numberOfItems, metrics);
    }

    private static void sqlSyncDeleteWorkload(CosmosContainer container, List<String> orderIdList, int numberOfItems, MetricRegistry metrics ) {
        SQLSyncDelete sqlSyncDelete = new SQLSyncDelete();
        sqlSyncDelete.execute(container, orderIdList, numberOfItems, metrics);
    }

    private static void sqlSyncUpdateWorkload(CosmosContainer container, List<String> orderIdList, int numberOfItems, MetricRegistry metrics ) {
        SQLSyncUpdate sqlSyncUpdate = new SQLSyncUpdate();
        sqlSyncUpdate.execute(container, orderIdList, numberOfItems, metrics);
    }

    private static void sqlAsyncUpdateWorkload(CosmosContainer container, List<String> orderIdList, int numberOfItems, MetricRegistry metrics ) {
        SQLAsyncUpdate sqlAsyncUpdate = new SQLAsyncUpdate();
        sqlAsyncUpdate.execute(container, orderIdList, numberOfItems, metrics);
    }

    private static CosmosDatabase getDB(CosmosClient client, String database) {
        return client.getDatabase(database);
    }

    private static CosmosClient buildCosmosClient(ConnectionMode connectionMode, int maxPoolSize, int maxRetryAttempts,
                                                  int retryWaitTimeInSeconds, String hostName, String masterKey, ConsistencyLevel consistencyLevel ) {

        ConnectionPolicy connectionPolicy = new ConnectionPolicy();
        connectionPolicy.connectionMode(connectionMode);
        connectionPolicy.preferredLocations( Arrays.asList("South Central US","North Central US"));
        connectionPolicy.usingMultipleWriteLocations(true);
        connectionPolicy.maxPoolSize(maxPoolSize);
        RetryOptions retryOptions = new RetryOptions();
        retryOptions.maxRetryAttemptsOnThrottledRequests(maxRetryAttempts);
        retryOptions.maxRetryWaitTimeInSeconds(retryWaitTimeInSeconds);
        connectionPolicy.retryOptions(retryOptions);

        return CosmosClient.builder()
                .endpoint(hostName)
                .key(masterKey)
                .connectionPolicy(connectionPolicy)
                .consistencyLevel( consistencyLevel )
                .build();

    }

    private static CosmosContainer setupContainer(CosmosDatabase db, String collection, int provisionedRUs) {

        CosmosContainer container;

        try {
            container = db.getContainer(collection);
            container.delete().block();
        } catch(Exception e) {
            System.out.println("Container " + collection + " doesn't exist");
        }

        CosmosContainerProperties cosmosContainerProperties = new CosmosContainerProperties(collection, Constants.CONST_PARTITION_KEY);
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.indexingMode(IndexingMode.CONSISTENT);

        List<IncludedPath> includedPaths = new ArrayList<>();
        IncludedPath includedPath = new IncludedPath();
        includedPath.path(Constants.CONST_PARTITION_KEY + "/*");
        includedPaths.add(includedPath);
        indexingPolicy.setIncludedPaths(includedPaths);

        List<ExcludedPath> excludedPaths = new ArrayList<>();
        ExcludedPath excludedPath2 = new ExcludedPath();
        excludedPath2.path("/*");
        excludedPaths.add(excludedPath2);
        indexingPolicy.excludedPaths(excludedPaths);

        cosmosContainerProperties.indexingPolicy(indexingPolicy);
        CosmosContainerResponse cosmosContainerResponse = db.createContainerIfNotExists(cosmosContainerProperties, provisionedRUs).block();

        container = cosmosContainerResponse.container();

        return container;

    }



    private static void publishMetrics(ConsoleReporter consoleReporter) {
        try {
            Thread.sleep(5 * 1000);
            consoleReporter.report();
            consoleReporter.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static ConsoleReporter startReport() {
        ConsoleReporter consoleReporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();

        return consoleReporter;
    }

}
