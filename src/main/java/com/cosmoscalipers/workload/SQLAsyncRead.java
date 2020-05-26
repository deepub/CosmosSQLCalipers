package com.cosmoscalipers.workload;

import com.azure.data.cosmos.*;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class SQLAsyncRead implements WorkloadInterface {

    private static Histogram requestUnits = null;
    private static Histogram readLatency = null;
    private static Meter throughput = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLAsyncRead.class);

    public void execute(CosmosContainer container, List<String> payloadIdList, int numberOfOps, MetricRegistry metrics) {
        requestUnits = metrics.histogram("SQL async partition key read RUs");
        readLatency = metrics.histogram("SQL async partition key read latency (ms)");
        throughput = metrics.meter("SQL async partition key read throughput");
        readOps(container, payloadIdList, numberOfOps);
    }

    private void readOps(CosmosContainer container, List<String> payloadIdList, int numberOfOps) {
        log("Running partition key based async SQL query workload for " + numberOfOps + " docs...");

        payloadIdList.stream()
                .forEach(item -> read(container, item));

    }

    private static void read(CosmosContainer container, String payloadId) {
        String query = "SELECT * FROM coll WHERE coll.payloadId = '" + payloadId + "'";
        FeedOptions options = new FeedOptions();
        options.maxDegreeOfParallelism(2);

        long startTime = System.currentTimeMillis();
        Flux<FeedResponse<CosmosItemProperties>> queryFlux = container.queryItems(query, options);

        try {
            queryFlux.doOnError(throwable -> log("Error occurred while executing query", throwable))
                    .publishOn(Schedulers.elastic())
                    .toIterable()
                    .forEach(
                            cosmosItemFeedResponse -> {
                                requestUnits.update( Math.round(cosmosItemFeedResponse.requestCharge()) );
                                throughput.mark();
                                //log(  ( cosmosItemFeedResponse.results())  );
                                //log(  cosmosItemFeedResponse.feedResponseDiagnostics().toString());
                            }
                    );
        } catch (Exception e) {
            System.out.println("******* Error occurred while executing query *******");
            System.out.println(e.getMessage());
        }


        long difference = System.currentTimeMillis()  - startTime;
        readLatency.update(difference);

    }

    private static void log(String msg, Throwable throwable){
        log(msg + ": " + ((CosmosClientException)throwable).statusCode());
    }

    private static void log(Object object) {
        System.out.println(object);
    }
}
