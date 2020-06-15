package com.cosmoscalipers.workload;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.cosmoscalipers.driver.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SQLSyncRead {

    private static Histogram requestUnits = null;
    private static Histogram readLatency = null;
    private static Meter throughput = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLSyncRead.class);

    public void execute(CosmosContainer container, List<String> payloadIdList, int numberOfOps, MetricRegistry metrics) {
        requestUnits = metrics.histogram("Sync partition key read RUs");
        readLatency = metrics.histogram("Sync partition key read latency (ms)");
        throughput = metrics.meter("Sync partition key read throughput");
        readOps(container, payloadIdList, numberOfOps);
    }

    private void readOps(CosmosContainer container, List<String> payloadIdList, int numberOfOps) {
        log("Running sync partition key based SQL query workload for " + numberOfOps + " docs...");

        payloadIdList.stream()
                .forEach(item -> read(container, item));

    }

    private static void read(CosmosContainer container, String payloadId) {
        String query = "SELECT * FROM coll WHERE coll.payloadId = '" + payloadId + "'";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setMaxDegreeOfParallelism(2);
        options.setQueryMetricsEnabled(true);

        long startTime = System.currentTimeMillis();
        CosmosPagedIterable<Payload> pagedIterable = container.queryItems(query, options, Payload.class);

        pagedIterable.iterableByPage().forEach(payloadFeedResponse -> {
            requestUnits.update(Math.round(payloadFeedResponse.getRequestCharge()));
            payloadFeedResponse.getResults()
                    .stream()
                    .forEach(payload -> {
                        throughput.mark();
                            }

                    );
        });

        long difference = System.currentTimeMillis()  - startTime;
        readLatency.update(difference);

    }

    private static void log(String msg, Throwable throwable){
        log(msg + ": " + ((CosmosException)throwable).getStatusCode());
    }

    private static void log(Object object) {
        System.out.println(object);
    }
}
