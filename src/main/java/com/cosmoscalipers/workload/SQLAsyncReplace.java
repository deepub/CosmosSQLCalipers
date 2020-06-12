package com.cosmoscalipers.workload;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
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

public class SQLAsyncReplace {
    private static Histogram sqlAsyncReplaceRequestUnits = null;
    private static Histogram sqlAsyncReplaceLatency = null;
    private static Meter throughput = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLAsyncReplace.class);

    public void execute(CosmosAsyncContainer container, List<String> orderIdList, int numberOfOps, MetricRegistry metrics) {

        sqlAsyncReplaceRequestUnits = metrics.histogram("Async replace RUs");
        sqlAsyncReplaceLatency = metrics.histogram("Async replace latency (ms)");
        throughput = metrics.meter("Async replace throughput");
        updateOps(container, orderIdList, numberOfOps);

    }

    private void updateOps(CosmosAsyncContainer container, List<String> orderIdList, int numberOfOps) {
        log("Running async replace workload for " + numberOfOps + " docs...");

        orderIdList.stream()
                .forEach(item -> update(container, item));

    }

    private static void update(CosmosAsyncContainer container, String orderId) {

        CosmosItemResponse<Payload> cosmosItemResponse = container.readItem(orderId, new PartitionKey(orderId), Payload.class).block();
        assert cosmosItemResponse != null;

        Payload payload = cosmosItemResponse.getItem();
        payload.setPayload("Replaced:" + payload.getPayload());

        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        Mono<CosmosItemResponse<Payload>> itemResponseMono = container.replaceItem(payload, orderId, new PartitionKey(orderId), requestOptions);

        itemResponseMono.doOnError(throwable -> {
            log("Error doing async replace for payloadId = " + payload.getId());
            log(throwable.getMessage());
        }).doOnSuccess(result -> {
            sqlAsyncReplaceRequestUnits.update( Math.round(result.getRequestCharge()) );
            sqlAsyncReplaceLatency.update(result.getDuration().toMillis());
            throughput.mark();
        }).publishOn(Schedulers.elastic()).block();


    }

    private static void log(String msg, Throwable throwable){
        log(msg + ": " + ((CosmosException)throwable).getStatusCode());
    }

    private static void log(Object object) {
        System.out.println(object);
    }
}
