package com.cosmoscalipers.workload;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class SQLAsyncDelete {
    private static Histogram sqlAsyncDeleteRequestUnits = null;
    private static Histogram sqlAsyncDeleteLatency = null;
    private static Meter throughput = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLAsyncDelete.class);

    public void execute(CosmosAsyncContainer container, List<String> orderIdList, int numberOfOps, MetricRegistry metrics) {

        sqlAsyncDeleteRequestUnits = metrics.histogram("Async delete RUs");
        sqlAsyncDeleteLatency = metrics.histogram("Async delete latency (ms)");
        throughput = metrics.meter("Async delete throughput");
        deleteOps(container, orderIdList, numberOfOps);

    }

    private void deleteOps(CosmosAsyncContainer container, List<String> orderIdList, int numberOfOps) {
        log("Running async delete workload for " + numberOfOps + " docs...");
        log("********************************************************************************************");

        orderIdList.stream()
                .forEach(item -> delete(container, item));

    }

    private static void delete(CosmosAsyncContainer container, String payloadId) {
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        Mono<CosmosItemResponse<Object>> cosmosItemResponse = container.deleteItem(payloadId, new PartitionKey(payloadId), options);

        cosmosItemResponse.doOnError(throwable -> {
                log(throwable.getMessage());
            })
            .doOnSuccess(itemResponse -> {
                sqlAsyncDeleteRequestUnits.update( Math.round(itemResponse.getRequestCharge()) );
                sqlAsyncDeleteLatency.update(itemResponse.getDuration().toMillis());
                throughput.mark();

            })
            .publishOn(Schedulers.elastic())
            .block();


    }

    private static void log(String msg, Throwable throwable){
        log(msg + ": " + ((CosmosException)throwable).getStatusCode());
    }

    private static void log(Object object) {
        System.out.println(object);
    }
}
