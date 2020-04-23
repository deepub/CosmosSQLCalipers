package com.cosmoscalipers.workload;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosItemResponse;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SQLSyncPointRead  implements WorkloadInterface {

    private static Histogram requestUnits = null;
    private static Histogram readLatency = null;
    private static Meter throughput = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLSyncPointRead.class);

    public void execute(CosmosContainer container, List<String> payloadIdList, int numberOfOps, MetricRegistry metrics) {
        requestUnits = metrics.histogram("SQL sync point read RUs");
        readLatency = metrics.histogram("SQL sync point read latency (ms)");
        throughput = metrics.meter("SQL sync point read throughput");
        readOps(container, payloadIdList, numberOfOps);
    }

    private void readOps(CosmosContainer container, List<String> payloadIdList, int numberOfOps) {
        log("Running sync point read workload " + numberOfOps + " docs...");

        payloadIdList.stream()
                .forEach(item -> read(container, item));

    }

    private static void read(CosmosContainer container, String payloadId) {

        CosmosItemResponse cosmosItemResponse = container.getItem(payloadId, payloadId).read().block();
        requestUnits.update( Math.round(cosmosItemResponse.requestCharge()) );
        readLatency.update(cosmosItemResponse.requestLatency().toMillis());
        throughput.mark();
        //log( cosmosItemResponse.properties().toJson()  );
        //log( cosmosItemResponse.cosmosResponseDiagnosticsString() );

    }

    private static void log(String msg, Throwable throwable){
        log(msg + ": " + ((CosmosClientException)throwable).statusCode());
    }

    private static void log(Object object) {
        System.out.println(object);
    }
}
