package com.cosmoscalipers.workload;

import com.azure.cosmos.CosmosContainer;
import com.codahale.metrics.MetricRegistry;

import java.util.List;

public interface Bootstrap {
    List<String> createDocs(CosmosContainer container, int numberOfDocs, int payloadSize, MetricRegistry metrics  );
}
