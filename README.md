# CosmosSQLCalipers
CosmosSQLCalipers is a basic Cosmos SQL API benchmarking utility. It enables developers to model and understand the impact of the following parameters:

+ Document sizes
+ Partition key based SELECT queries vs point reads
+ Impact of sync versus async APIs
+ Request unit (RUs) consumption
+ Network latencies and throughput

This enables developers to get a preview into the overall scalability, response times and cost considerations when evaluating Cosmos SQL API. 

## News
This section provides the latest updates

#### Cosmos SQL client dependency
CosmosSQLCalipers relies on the [azure-cosmos](https://mvnrepository.com/artifact/com.azure/azure-cosmos) SDK.

#### Test result links
- [Summary analysis for test run on August 10th 2020](https://github.com/deepub/CosmosSQLCalipers/blob/master/output/analysis/all_consistency_levels_08102020/README.md)
- [Summary analysis for test run on July 1st 2020](https://github.com/deepub/CosmosSQLCalipers/blob/master/output/analysis/all_consistency_levels_07012020/README.md) 
- [Summary analysis for test run on June 21st 2020](https://github.com/deepub/CosmosSQLCalipers/blob/master/output/analysis/session_consistency_06212020/README.md) 

##### *v4 client*
Project upgraded to v4.5.0. Following consistency levels are running fine:
* STRONG
* BOUNDED_STALENESS
* SESSION
* CONSISTENCY_PREFIX
* EVENTUAL

##### *v3 client*

Following consistency levels are running fine:
* STRONG
* SESSION
* EVENTUAL

Following consistency levels are failing:
* BOUNDED_STALENESS (Creates fail with timeout exceptions)
* CONSISTENCY_PREFIX (Creates fail with timeout exceptions)

## Overview
The utility executes the following workflow:
* Provisions a collection based on input parameters
* Allocates the provisioned RUs
* Creates and inserts documents into the collection
* Executes the query workload based on the operation specified in the input parameters
* Deletes the documents explicitly at the end of the exercise 

Indexing policy has a significant impact on RU consumption. So for the sake of simplicity, CosmosSQLCalipers deploys a single index for partition key lookups. Collected metrics include:
* Throughput
* Network latencies
* RUs consumed

Operations that can be exercised using this tool includes:
1. SQL_ASYNC_PARTITION_KEY_READ: Executes partition key based queries asynchronously  
1. SQL_SYNC_PARTITION_KEY_READ: Executes partition key based queries synchronously
1. SQL_ASYNC_POINT_READ: Executes point read operations asynchronously
1. SQL_SYNC_POINT_READ: Executes point read operations synchronously
1. SQL_ASYNC_UPSERT: Executes upsert operations asynchronously
1. SQL_SYNC_UPSERT: Executes upsert operations synchronously
1. SQL_ASYNC_REPLACE: Executes replace operations asynchronously
1. SQL_SYNC_REPLACE: Executes replace operations synchronously 
1. ALL_SYNC_OPS: Executes all synchronous operations serially. The execution cycle starts with partition key based reads followed by point reads.  
1. ALL_ASYNC_OPS: Executes all asynchronous operations serially. The execution cycle starts with partition key based reads followed by point reads.
1. SQL_ALL: Executes async partition key read, sync partition key read, async point read and sync point read operations serially.

SQL API workflows executed
1. SQL_ASYNC_PARTITION_KEY_READ and SQL_SYNC_PARTITION_KEY_READ options
    1. createItem()
    1. queryItem()
    1. deleteItem()
1. SQL_ASYNC_POINT_READ and SQL_SYNC_POINT_READ options
    1. createItem()
    1. readItem()
    1. deleteItem()
1. SQL_ASYNC_READ_ALL_ITEMS and SQL_SYNC_READ_ALL_ITEMS options
    1. createItem()
    1. readAllItems()
    1. deleteItem()
1. SQL_ASYNC_UPSERT and SQL_SYNC_UPSERT options
    1. createItem()
    1. upsertItem()
    1. deleteItem()
1. SQL_ASYNC_REPLACE and SQL_SYNC_REPLACE options 
    1. createItem()
    1. replaceItem()
    1. deleteItem()
1. ALL_SYNC_OPS and ALL_ASYNC_OPS options
    1. createItem()
    1. queryItem()
    1. readItem()
    1. readAllItems()
    1. upsertItem()
    1. replaceItem()
    1. deleteItem()
1. SQL_ALL
    1. Invokes all async and sync ops


## Instructions
1. Create a Cosmos DB SQL API account
    1. Assign consistency level you want to test with
    1. Select region depending on your deployment preferences
    1. Single or multiple regions
    1. Choose an active/active or active/passive topology
2. Run the benchmark test against the provisioned Cosmos account.     

## Program arguments
````
usage: Cosmos DB SQL Benchmark
    --collection <arg>                  Cosmos collection name
    --consistencylevel <arg>            Consistency level to be exercised
    --database <arg>                    Enter Cosmos Database Name
    --hostname <arg>                    Cosmos Service endpoint
    --key <arg>                         Cosmos key for authentication
    --maxpoolsize <arg>                 Max pool size
    --maxretryattempts <arg>            Max retry attempts when
                                        backpressure is encountered
    --maxretrywaittimeinseconds <arg>   Max retry wait time in seconds
    --numberofdocs <arg>                Number of documents to be tested
    --operation <arg>                   Primary operation being exercised
    --payloadSize <arg>                 Document size
    --provisionedrus <arg>              RUs to be provisioned when Cosmos
                                        container is created
    --reporter                          CONSOLE or CSV. The results of the test will
                                        be sent to the appropriate reporter
    --deleteContainer                   Optional. Defaults to false. Delete and recreate
                                        the container or not(Y/N) or (true/false)                                    
````

## Building and running

````
mvn clean package
mvn exec:java -Dexec.mainClass="com.cosmoscalipers.Measure" -Dexec.cleanupDaemonThreads=false -Dexec.args="--hostname https://youraccount.documents.azure.com:443/ --database demo --collection orders --key <your account key> --numberofdocs 1000 --payloadSize 500 --consistencylevel SESSION --provisionedrus 400 --maxpoolsize 100 --maxretryattempts 10 --maxretrywaittimeinseconds 1 --operation SQL_ALL --reporter CONSOLE"
````

