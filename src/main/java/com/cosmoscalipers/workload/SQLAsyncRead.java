package com.cosmoscalipers.workload;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.cosmoscalipers.driver.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SQLAsyncRead{

    private static Histogram requestUnits = null;
    private static Histogram readLatency = null;
    private static Meter throughput = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLAsyncRead.class);

    public void execute(CosmosAsyncContainer container, List<String> payloadIdList, int numberOfOps, MetricRegistry metrics) {
        requestUnits = metrics.histogram("Async partition key read RUs");
        readLatency = metrics.histogram("Async partition key read latency (ms)");
        throughput = metrics.meter("Async partition key read throughput");
        readOps(container, payloadIdList, numberOfOps);
    }

    private void readOps(CosmosAsyncContainer container, List<String> payloadIdList, int numberOfOps) {
        log("Running async partition key based SQL query workload for " + numberOfOps + " docs...");

        payloadIdList.stream()
                .forEach(item -> read(container, item));

    }

    private static void read(CosmosAsyncContainer container, String payloadId) {
        String query = "SELECT * FROM coll WHERE coll.payloadId = '" + payloadId + "'";
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setMaxDegreeOfParallelism(2);
        options.setQueryMetricsEnabled(true);

        long startTime = System.currentTimeMillis();
        CosmosPagedFlux<Payload> queryFlux = container.queryItems(query, options, Payload.class);

        try {

            queryFlux.byPage().subscribe(fluxResponse -> {
                long difference = System.currentTimeMillis()  - startTime;
                requestUnits.update( Math.round(fluxResponse.getRequestCharge())  );
                throughput.mark();
                readLatency.update(difference);
//                log("Call Latency (ms): " + difference);
//                log("Diagnostics: " + fluxResponse.getCosmosDiagnostics().toString());
 
            },
            error -> {
                if(error instanceof CosmosException) {
                    CosmosException cosmosClientException = (CosmosException) error;
                    cosmosClientException.printStackTrace();
                    log("Error occurred while executing query");
                } else {
                    error.printStackTrace();
                }
            }
                );

        } catch (Exception e) {
            System.out.println("******* Error occurred while executing query *******");
            System.out.println(e.getMessage());
        }

    }

    private static void log(String msg, Throwable throwable){
        log(msg + ": " + ((CosmosException)throwable).getStatusCode());
    }

    private static void log(Object object) {
        System.out.println(object);
    }
}
