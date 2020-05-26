package com.cosmoscalipers.workload;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosItemResponse;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class SQLAsyncPointRead implements WorkloadInterface {

    private static Histogram requestUnits = null;
    private static Histogram readLatency = null;
    private static Meter throughput = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLAsyncPointRead.class);

    public void execute(CosmosContainer container, List<String> payloadIdList, int numberOfOps, MetricRegistry metrics) {
        requestUnits = metrics.histogram("SQL async point read RUs");
        readLatency = metrics.histogram("SQL async point read latency (ms)");
        throughput = metrics.meter("SQL async point read throughput");
        readOps(container, payloadIdList, numberOfOps);
    }

    private void readOps(CosmosContainer container, List<String> orderIdList, int numberOfOps) {
        log("Running async point read workload for " + numberOfOps + " docs...");

        orderIdList.stream()
                .forEach(item -> read(container, item));

    }

    private static void read(CosmosContainer container, String orderId) {

        Mono<CosmosItemResponse> cosmosItemResponse = container.getItem(orderId, orderId).read();
        cosmosItemResponse.doOnSuccess( itemResponse -> {
            requestUnits.update( Math.round(itemResponse.requestCharge()) );
            readLatency.update(itemResponse.requestLatency().toMillis());
            throughput.mark();
            //log( itemResponse.properties().toJson()  );

                }
        ).publishOn(Schedulers.elastic())
        .block();

    }

    private static void log(String msg, Throwable throwable){
        log(msg + ": " + ((CosmosClientException)throwable).statusCode());
    }

    private static void log(Object object) {
        System.out.println(object);
    }
}
