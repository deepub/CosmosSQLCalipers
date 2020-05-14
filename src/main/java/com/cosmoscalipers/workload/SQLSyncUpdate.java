package com.cosmoscalipers.workload;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosItemResponse;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.cosmoscalipers.driver.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SQLSyncUpdate {
    private static Histogram sqlSyncUpdateRequestUnits = null;
    private static Histogram sqlSyncUpdateLatency = null;
    private static Meter throughput = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLSyncUpdate.class);

    public void execute(CosmosContainer container, List<String> orderIdList, int numberOfOps, MetricRegistry metrics) {

        sqlSyncUpdateRequestUnits = metrics.histogram("Sync Update RUs");
        sqlSyncUpdateLatency = metrics.histogram("Sync update latency (ms)");
        throughput = metrics.meter("Sync update throughput");
        updateOps(container, orderIdList, numberOfOps);

    }

    private void updateOps(CosmosContainer container, List<String> orderIdList, int numberOfOps) {
        log("Running sync update workload for " + numberOfOps + " docs...");

        orderIdList.stream()
                .forEach(item -> update(container, item));

    }

    private static void update(CosmosContainer container, String orderId) {

        CosmosItemResponse cosmosItemResponse = container.getItem(orderId, orderId).read().block();
        assert cosmosItemResponse != null;
        Payload payload = new Payload(
                cosmosItemResponse.properties().get("id").toString(),
                cosmosItemResponse.properties().get("id").toString(),
                "Updated:" + cosmosItemResponse.properties().get("payload").toString()
                );

        cosmosItemResponse = container.getItem(orderId, orderId).read().block().item().replace(payload).block();

        sqlSyncUpdateRequestUnits.update( Math.round(cosmosItemResponse.requestCharge()) );
        sqlSyncUpdateLatency.update(cosmosItemResponse.requestLatency().toMillis());
        throughput.mark();

    }

    private static void log(String msg, Throwable throwable){
        log(msg + ": " + ((CosmosClientException)throwable).statusCode());
    }

    private static void log(Object object) {
        System.out.println(object);
    }
}

