package com.cosmoscalipers.workload;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.cosmoscalipers.driver.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SQLAsyncReadAllItems {

    private static Histogram requestUnits = null;
    private static Histogram readLatency = null;
    private static Meter throughput = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLAsyncReadAllItems.class);

    public void execute(CosmosAsyncContainer container, List<String> payloadIdList, int numberOfOps, MetricRegistry metrics) {
        requestUnits = metrics.histogram("Async readAllItems() RUs");
        readLatency = metrics.histogram("Async readAllItems() latency (ms)");
        throughput = metrics.meter("Async readAllItems() throughput");
        readOps(container, payloadIdList, numberOfOps);
    }

    private void readOps(CosmosAsyncContainer container, List<String> payloadIdList, int numberOfOps) {
        log("Running async readAllItems() workload for " + numberOfOps + " docs...");

        payloadIdList.stream()
                .forEach(item -> read(container, item));

    }

    private static void read(CosmosAsyncContainer container, String payloadId) {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setMaxDegreeOfParallelism(2);
        options.setQueryMetricsEnabled(true);

        try {
            long startTime = System.currentTimeMillis();
            List<FeedResponse<Payload>> payloadList = container.readAllItems(new PartitionKey(payloadId), options, Payload.class).byPage().collectList().block();

            payloadList.stream()
                    .forEach(fluxResponse -> {
                        long difference = System.currentTimeMillis()  - startTime;
                        requestUnits.update( Math.round(fluxResponse.getRequestCharge())  );
                        throughput.mark();
                        readLatency.update(difference);
//                        log("Call Latency (ms): " + difference);
//                        log("Diagnostics: " + fluxResponse.getCosmosDiagnostics().toString());

                    });

        } catch(Exception error)
        {
            if(error instanceof CosmosException) {
                CosmosException cosmosClientException = (CosmosException) error;
                cosmosClientException.printStackTrace();
                log("Error occurred while executing query");
            } else {
                error.printStackTrace();
            }
        }

    }

    private static void log(String msg, Throwable throwable){
        log(msg + ": " + ((CosmosException)throwable).getStatusCode());
    }

    private static void log(Object object) {
        System.out.println(object);
    }
}
