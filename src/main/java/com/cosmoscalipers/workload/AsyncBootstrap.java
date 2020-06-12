package com.cosmoscalipers.workload;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.cosmoscalipers.driver.Payload;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

public class AsyncBootstrap{

    private static Counter successCounter = null;
    private static Counter failureCounter = null;
    private static Histogram requestUnits = null;
    private static Histogram asyncWriteLatency = null;
    private static Meter asyncThroughput = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncBootstrap.class);

    public List<String> createDocs(CosmosAsyncContainer container, int numberOfDocs, int payloadSize, MetricRegistry metrics  ) {

        List<String> payloadIdList = new ArrayList<String>();
        successCounter = metrics.counter("Async write success counter");
        failureCounter = metrics.counter("Async write failure counter");
        requestUnits = metrics.histogram("Async write RUs");
        asyncWriteLatency = metrics.histogram("Async write latency (ms)");
        asyncThroughput = metrics.meter("Async write throughput");

        log("********************************************************************************************");
        log("Running async SQL bootstrap workload for " + numberOfDocs + " docs...");

        LongStream.range(1, numberOfDocs + 1)
                .forEach( counter -> asyncWrite(counter, payloadSize, payloadIdList, container));

        return payloadIdList;
    }

    private static void log(String msg, Throwable throwable){
        log(msg + ": " + ((CosmosException)throwable).getStatusCode());
    }

    private static void log(Object object) {
        System.out.println(object);
    }

    private static void asyncWrite(long counter, int payloadSize, List<String> payloadIdList, CosmosAsyncContainer container) {
        String payloadId = "ORD101" + counter;
        String payload = StringUtils.rightPad( Long.valueOf(counter).toString() , payloadSize, "*");
        payloadIdList.add(payloadId);
        CosmosItemRequestOptions options = new CosmosItemRequestOptions();
        Mono<CosmosItemResponse<Payload>> responseMono = container.createItem(new Payload(payloadId, payloadId, payload), options);

        try {
            responseMono.doOnError(throwable -> {
                failureCounter.inc();
            })
                    .doOnSuccess(result -> {
                        successCounter.inc();
                        requestUnits.update( Math.round(result.getRequestCharge()) );
                        asyncWriteLatency.update( result.getDuration().toMillis() );
                        asyncThroughput.mark();
                    })
                    .publishOn(Schedulers.elastic())
                    .block();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

    }
}
