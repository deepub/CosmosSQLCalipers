package com.cosmoscalipers.workload;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.cosmoscalipers.driver.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SQLSyncUpsert {
    private static Histogram sqlSyncUpsertItemRequestUnits = null;
    private static Histogram sqlSyncUpsertItemLatency = null;
    private static Meter throughput = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLSyncUpsert.class);

    public void execute(CosmosContainer container, List<String> orderIdList, int numberOfOps, MetricRegistry metrics) {

        sqlSyncUpsertItemRequestUnits = metrics.histogram("Sync upsert RUs");
        sqlSyncUpsertItemLatency = metrics.histogram("Sync upsert latency (ms)");
        throughput = metrics.meter("Sync upsert throughput");
        updateOps(container, orderIdList, numberOfOps);

    }

    private void updateOps(CosmosContainer container, List<String> orderIdList, int numberOfOps) {
        log("Running sync upsert workload for " + numberOfOps + " docs...");

        orderIdList.stream()
                .forEach(item -> update(container, item));

    }

    private static void update(CosmosContainer container, String orderId) {

        CosmosItemResponse<Payload> cosmosItemResponse = container.readItem(orderId, new PartitionKey(orderId), Payload.class);
        assert cosmosItemResponse != null;
        Payload payload = cosmosItemResponse.getItem();
        payload.setPayload("Upserted:" + payload.getPayload());
        cosmosItemResponse = container.upsertItem(payload);

        sqlSyncUpsertItemRequestUnits.update( Math.round(cosmosItemResponse.getRequestCharge()) );
        sqlSyncUpsertItemLatency.update(cosmosItemResponse.getDuration().toMillis());
        throughput.mark();

    }

    private static void log(String msg, Throwable throwable){
        log(msg + ": " + ((CosmosException)throwable).getStatusCode());
    }

    private static void log(Object object) {
        System.out.println(object);
    }
}

