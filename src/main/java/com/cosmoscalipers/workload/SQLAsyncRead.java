package com.cosmoscalipers.workload;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientException;
import com.azure.cosmos.models.FeedOptions;
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
        requestUnits = metrics.histogram("SQL async partition key read RUs");
        readLatency = metrics.histogram("SQL async partition key read latency (ms)");
        throughput = metrics.meter("SQL async partition key read throughput");
        readOps(container, payloadIdList, numberOfOps);
    }

    private void readOps(CosmosAsyncContainer container, List<String> payloadIdList, int numberOfOps) {
        log("Running async partition key based SQL query workload for " + numberOfOps + " docs...");

        payloadIdList.stream()
                .forEach(item -> read(container, item));

    }

    private static void read(CosmosAsyncContainer container, String payloadId) {
        String query = "SELECT * FROM coll WHERE coll.payloadId = '" + payloadId + "'";
        FeedOptions options = new FeedOptions();
        options.setMaxDegreeOfParallelism(2);
        options.setPopulateQueryMetrics(true);

        long startTime = System.currentTimeMillis();
        CosmosPagedFlux<Payload> queryFlux = container.queryItems(query, options, Payload.class);

        try {

            queryFlux.byPage().subscribe(fluxResponse -> {
                requestUnits.update( Math.round(fluxResponse.getRequestCharge())  );
                throughput.mark();
//                log(  fluxResponse.getResults()  );
//                log(  fluxResponse.getFeedResponseDiagnostics().toString());

            },
            error -> {
                if(error instanceof CosmosClientException) {
                    CosmosClientException cosmosClientException = (CosmosClientException) error;
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


        long difference = System.currentTimeMillis()  - startTime;
        readLatency.update(difference);

    }

    private static void log(String msg, Throwable throwable){
        log(msg + ": " + ((CosmosClientException)throwable).getStatusCode());
    }

    private static void log(Object object) {
        System.out.println(object);
    }
}
