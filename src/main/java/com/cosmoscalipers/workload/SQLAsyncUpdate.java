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
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class SQLAsyncUpdate {
    private static Histogram sqlAsyncUpdateRequestUnits = null;
    private static Histogram sqlAsyncUpdateLatency = null;
    private static Meter throughput = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLSyncUpdate.class);

    public void execute(CosmosContainer container, List<String> orderIdList, int numberOfOps, MetricRegistry metrics) {

        sqlAsyncUpdateRequestUnits = metrics.histogram("Async Update RUs");
        sqlAsyncUpdateLatency = metrics.histogram("Async update latency (ms)");
        throughput = metrics.meter("Async update throughput");
        updateOps(container, orderIdList, numberOfOps);

    }

    private void updateOps(CosmosContainer container, List<String> orderIdList, int numberOfOps) {
        log("Running async update workload for " + numberOfOps + " docs...");

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

        Mono<CosmosItemResponse> itemResponseMono = container.getItem(orderId, orderId).replace(payload);

        itemResponseMono.doOnError(throwable -> {
            log("Error doing async update for payloadId = " + payload.getId());
            log(throwable.getMessage());
        }).doOnSuccess(result -> {
            sqlAsyncUpdateRequestUnits.update( Math.round(result.requestCharge()) );
            sqlAsyncUpdateLatency.update(result.requestLatency().toMillis());
            throughput.mark();
        }).publishOn(Schedulers.elastic()).block();


    }

    private static void log(String msg, Throwable throwable){
        log(msg + ": " + ((CosmosClientException)throwable).statusCode());
    }

    private static void log(Object object) {
        System.out.println(object);
    }
}
