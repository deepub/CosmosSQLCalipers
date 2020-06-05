package com.cosmoscalipers.workload;

import com.azure.cosmos.CosmosContainer;
import com.codahale.metrics.MetricRegistry;

import java.util.List;

public interface WorkloadInterface {
    void execute(CosmosContainer container, List<String> orderIdList, int numberOfOps, MetricRegistry metrics);
}
