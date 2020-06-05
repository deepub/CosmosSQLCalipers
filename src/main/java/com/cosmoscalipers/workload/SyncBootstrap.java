package com.cosmoscalipers.workload;

import com.azure.cosmos.CosmosClientException;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.cosmoscalipers.driver.Payload;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.LongStream;

public class SyncBootstrap {

    private static Counter successCounter = null;
    private static Counter failureCounter = null;
    private static Histogram requestUnits = null;
    private static Histogram syncWriteLatency = null;
    private static Meter syncThroughput = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SyncBootstrap.class);

    public List<String> createDocs(CosmosContainer container, int numberOfDocs, int payloadSize, MetricRegistry metrics  ) {

        List<String> payloadIdList = new ArrayList<String>();
        successCounter = metrics.counter("Sync write success counter");
        failureCounter = metrics.counter("Sync write failure counter");
        requestUnits = metrics.histogram("Sync write RUs");
        syncWriteLatency = metrics.histogram("Sync write latency (ms)");
        syncThroughput = metrics.meter("Sync write throughput");

        log("********************************************************************************************");
        log("Running sync SQL bootstrap workload for " + numberOfDocs + " docs...");

        LongStream.range(1, numberOfDocs + 1)
                .forEach(counter -> syncWrite(counter, payloadSize, payloadIdList, container));

        return payloadIdList;
    }

    private static void log(String msg, Throwable throwable){
        log(msg + ": " + ((CosmosClientException)throwable).getStatusCode());
    }

    private static void log(Object object) {
        System.out.println(object);
    }

    private static void syncWrite(long counter, int payloadSize, List<String> payloadIdList, CosmosContainer container) {
        String payloadId = "ORD101" + counter;
        String payload = StringUtils.rightPad(Long.valueOf(counter).toString(), payloadSize, "*");
        Payload aPayload = new Payload(payloadId, payloadId, payload);
        payloadIdList.add(payloadId);
        CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
        CosmosItemResponse itemResponse = container.createItem(aPayload, new PartitionKey(aPayload.getPayloadId()), cosmosItemRequestOptions);
        successCounter.inc();
        requestUnits.update( Math.round(itemResponse.getRequestCharge()) );
        syncWriteLatency.update( itemResponse.getRequestLatency().toMillis() );
        syncThroughput.mark();

    }
}
