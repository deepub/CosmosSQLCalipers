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

public class SQLSyncDelete implements WorkloadInterface{
    private static Histogram sqlSyncDeleteRequestUnits = null;
    private static Histogram sqlSyncDeleteLatency = null;
    private static Meter throughput = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLSyncDelete.class);

    public void execute(CosmosContainer container, List<String> orderIdList, int numberOfOps, MetricRegistry metrics) {

        sqlSyncDeleteRequestUnits = metrics.histogram("Sync delete RUs");
        sqlSyncDeleteLatency = metrics.histogram("Sync delete latency (ms)");
        throughput = metrics.meter("Sync delete throughput");
        deleteOps(container, orderIdList, numberOfOps);

    }

    private void deleteOps(CosmosContainer container, List<String> orderIdList, int numberOfOps) {
        log("Running sync delete workload for " + numberOfOps + " docs...");
        log("********************************************************************************************");

        orderIdList.stream()
                .forEach(item -> delete(container, item));

    }

    private static void delete(CosmosContainer container, String orderId) {

        CosmosItemResponse cosmosItemResponse = container.getItem(orderId, orderId).read().block().item().delete().block();

        sqlSyncDeleteRequestUnits.update( Math.round(cosmosItemResponse.requestCharge()) );
        sqlSyncDeleteLatency.update(cosmosItemResponse.requestLatency().toMillis());
        throughput.mark();

    }

    private static void log(String msg, Throwable throwable){
        log(msg + ": " + ((CosmosClientException)throwable).statusCode());
    }

    private static void log(Object object) {
        System.out.println(object);
    }
}
