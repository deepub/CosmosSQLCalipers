package com.cosmoscalipers.workload;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientException;
import com.azure.cosmos.models.CosmosAsyncItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.cosmoscalipers.driver.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class SQLAsyncUpdate {
    private static Histogram sqlAsyncUpdateRequestUnits = null;
    private static Histogram sqlAsyncUpdateLatency = null;
    private static Meter throughput = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLSyncUpdate.class);

    public void execute(CosmosAsyncContainer container, List<String> orderIdList, int numberOfOps, MetricRegistry metrics) {

        sqlAsyncUpdateRequestUnits = metrics.histogram("Async update RUs");
        sqlAsyncUpdateLatency = metrics.histogram("Async update latency (ms)");
        throughput = metrics.meter("Async update throughput");
        updateOps(container, orderIdList, numberOfOps);

    }

    private void updateOps(CosmosAsyncContainer container, List<String> orderIdList, int numberOfOps) {
        log("Running async update workload for " + numberOfOps + " docs...");

        orderIdList.stream()
                .forEach(item -> update(container, item));

    }

    private static void update(CosmosAsyncContainer container, String orderId) {

        CosmosAsyncItemResponse<Payload> cosmosItemResponse = container.readItem(orderId, new PartitionKey(orderId), Payload.class).block();
        assert cosmosItemResponse != null;

        Payload payload = cosmosItemResponse.getItem();
        payload.setPayload("Updated:" + payload.getPayload());

        Mono<CosmosAsyncItemResponse<Payload>> itemResponseMono = container.upsertItem(payload);

        itemResponseMono.doOnError(throwable -> {
            log("Error doing async update for payloadId = " + payload.getId());
            log(throwable.getMessage());
        }).doOnSuccess(result -> {
            sqlAsyncUpdateRequestUnits.update( Math.round(result.getRequestCharge()) );
            sqlAsyncUpdateLatency.update(result.getRequestLatency().toMillis());
            throughput.mark();
        }).publishOn(Schedulers.elastic()).block();


    }

    private static void log(String msg, Throwable throwable){
        log(msg + ": " + ((CosmosClientException)throwable).getStatusCode());
    }

    private static void log(Object object) {
        System.out.println(object);
    }
}
