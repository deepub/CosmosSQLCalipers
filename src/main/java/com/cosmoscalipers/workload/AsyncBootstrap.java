package com.cosmoscalipers.workload;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosContainer;
import com.azure.data.cosmos.CosmosItemResponse;
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

public class AsyncBootstrap implements Bootstrap{

    private static Counter successCounter = null;
    private static Counter failureCounter = null;
    private static Histogram requestUnits = null;
    private static Histogram asyncWriteLatency = null;
    private static Meter asyncThroughput = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncBootstrap.class);

    public List<String> createDocs(CosmosContainer container, int numberOfDocs, int payloadSize, MetricRegistry metrics  ) {

        List<String> payloadIdList = new ArrayList<String>();
        successCounter = metrics.counter("Write success counter");
        failureCounter = metrics.counter("Write failure counter");
        requestUnits = metrics.histogram("Write RUs");
        asyncWriteLatency = metrics.histogram("Async write latency (ms)");
        asyncThroughput = metrics.meter("Async write throughput");

        log(container.getDatabase().toString());
        log("********************************************************************************************");
        log("Running async SQL bootstrap workload for " + numberOfDocs + " docs...");

        LongStream.range(1, numberOfDocs + 1)
                .parallel()
                .forEach( counter -> asyncWrite(counter, payloadSize, payloadIdList, container));

        return payloadIdList;
    }

    private static void log(String msg, Throwable throwable){
        log(msg + ": " + ((CosmosClientException)throwable).statusCode());
    }

    private static void log(Object object) {
        System.out.println(object);
    }

    private static void asyncWrite(long counter, int payloadSize, List<String> payloadIdList, CosmosContainer container) {
        String payloadId = "ORD101" + counter;
        String payload = StringUtils.rightPad( Long.valueOf(counter).toString() , payloadSize, "*");
        payloadIdList.add(payloadId);
        Mono<CosmosItemResponse> responseMono = container.createItem(new Payload(payloadId, payloadId, payload));

        responseMono.doOnError(throwable -> {
            failureCounter.inc();
        })
        .doOnSuccess(result -> {
            successCounter.inc();
            requestUnits.update( Math.round(result.requestCharge()) );
            asyncWriteLatency.update( result.requestLatency().toMillis() );
            asyncThroughput.mark();
        })
        .publishOn(Schedulers.elastic())
        .block();

    }
}
