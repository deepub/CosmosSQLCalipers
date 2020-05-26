package com.cosmoscalipers.workload;

import com.azure.data.cosmos.*;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SQLSyncRead implements WorkloadInterface {

    private static Histogram requestUnits = null;
    private static Histogram readLatency = null;
    private static Meter throughput = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLSyncRead.class);

    public void execute(CosmosContainer container, List<String> payloadIdList, int numberOfOps, MetricRegistry metrics) {
        requestUnits = metrics.histogram("SQL sync partition key read RUs");
        readLatency = metrics.histogram("SQL sync partition key read latency (ms)");
        throughput = metrics.meter("SQL sync partition key read throughput");
        readOps(container, payloadIdList, numberOfOps);
    }

    private void readOps(CosmosContainer container, List<String> payloadIdList, int numberOfOps) {
        log("Running partition key based sync SQL query workload for " + numberOfOps + " docs...");

        payloadIdList.stream()
                .forEach(item -> read(container, item));

    }

    private static void read(CosmosContainer container, String payloadId) {
        String query = "SELECT * FROM coll WHERE coll.payloadId = '" + payloadId + "'";
        FeedOptions options = new FeedOptions();
        options.maxDegreeOfParallelism(2);

        long startTime = System.currentTimeMillis();
        FeedResponse<CosmosItemProperties> feedResponse = container.queryItems(query, options).single().block();

        requestUnits.update( Math.round(feedResponse.requestCharge()) );

        List<CosmosItemProperties> itemProperties = feedResponse.results();
        itemProperties.stream()
                .forEach(cosmosItemProperties -> {
                    throughput.mark();
                    //log(cosmosItemProperties.toJson());
                });

        //ParallelDocumentQueryExecutionContext.addCompositeContinuationToken() is not assigning documentProducerFeedResponse.pageResult

//FeedResponse class in v3 is assigning default values to DefaultPartition, queryMetricsMap and feedResponseDiagnostics
//        private FeedResponse(List<T> results, Map<String, String> header, boolean useEtagAsContinuation, boolean nochanges, ConcurrentMap<String, QueryMetrics > queryMetricsMap) {
//            this.DefaultPartition = "0";
//            this.results = results;
//            this.header = header;
//            this.usageHeaders = new HashMap();
//            this.quotaHeaders = new HashMap();
//            this.useEtagAsContinuation = useEtagAsContinuation;
//            this.nochanges = nochanges;
//            this.queryMetricsMap = new ConcurrentHashMap(queryMetricsMap);
//            this.feedResponseDiagnostics = new FeedResponseDiagnostics(queryMetricsMap);
//        }
        //log(feedResponse.feedResponseDiagnostics().toString());
        long difference = System.currentTimeMillis()  - startTime;
        readLatency.update(difference);

    }

    private static void log(String msg, Throwable throwable){
        log(msg + ": " + ((CosmosClientException)throwable).statusCode());
    }

    private static void log(Object object) {
        System.out.println(object);
    }
}
