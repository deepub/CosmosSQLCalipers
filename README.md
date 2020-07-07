# CosmosSQLCalipers
CosmosSQLCalipers is a basic Cosmos SQL API benchmarking utility. It enables developers to model and understand the impact of the following parameters:

+ Document sizes
+ Partition key based SELECT queries vs point reads
+ Impact of sync versus async APIs
+ Request unit (RUs) consumption
+ Network latencies and throughput

This enables developers to get a preview into the overall scalability, response times and cost considerations when evaluating Cosmos SQL API. 

## Building
````
mvn clean install
````

## News
This section provides the latest updates

#### Cosmos SQL client dependency
CosmosSQLCalipers relies on the azure-cosmos SDK. The current version is v4.0.1.

#### Test result links
- [Summary analysis for test run on July 1st 2020](https://github.com/deepub/CosmosSQLCalipers/blob/master/output/analysis/all_consistency_levels_07012020/README.md) 
- [Summary analysis for test run on June 21st 2020](https://github.com/deepub/CosmosSQLCalipers/blob/master/output/analysis/session_consistency_06212020/README.md) 

##### *v4 client*
Project upgraded to v4.1.0. Following consistency levels are running fine:
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
````

## Example

````
mvn exec:java -Dexec.mainClass="com.cosmoscalipers.Measure" -Dexec.cleanupDaemonThreads=false -Dexec.args="--hostname https://youraccount.documents.azure.com:443/ --database demo --collection orders --key <your account key> --numberofdocs 1000 --payloadSize 500 --consistencylevel SESSION --provisionedrus 400 --maxpoolsize 100 --maxretryattempts 10 --maxretrywaittimeinseconds 1 --operation SQL_ALL --reporter CONSOLE"
````

````
database : demo
Payload size : 1000
********************************************************************************************
Running sync SQL bootstrap workload for 100 docs...
Running sync partition key based SQL query workload for 100 docs...
Running sync point read workload 100 docs...
Running sync upsert workload for 100 docs...
Running sync replace workload for 100 docs...
Running sync delete workload for 100 docs...
********************************************************************************************
Payload size : 1000
********************************************************************************************
Running async SQL bootstrap workload for 100 docs...
Running async partition key based SQL query workload for 100 docs...
Running async point read workload for 100 docs...
Running async upsert workload for 100 docs...
Running async replace workload for 100 docs...
Running async delete workload for 100 docs...
********************************************************************************************
6/12/20, 12:22:53 PM ===========================================================

-- Counters --------------------------------------------------------------------
Async write failure counter
             count = 0
Async write success counter
             count = 100
Sync write failure counter
             count = 0
Sync write success counter
             count = 100

-- Histograms ------------------------------------------------------------------
Async delete RUs
             count = 100
               min = 7
               max = 7
              mean = 7.00
            stddev = 0.00
            median = 7.00
              75% <= 7.00
              95% <= 7.00
              98% <= 7.00
              99% <= 7.00
            99.9% <= 7.00
Async delete latency (ms)
             count = 100
               min = 23
               max = 39
              mean = 27.35
            stddev = 3.07
            median = 26.00
              75% <= 29.00
              95% <= 34.00
              98% <= 36.00
              99% <= 39.00
            99.9% <= 39.00
Async replace RUs
             count = 100
               min = 13
               max = 13
              mean = 13.00
            stddev = 0.00
            median = 13.00
              75% <= 13.00
              95% <= 13.00
              98% <= 13.00
              99% <= 13.00
            99.9% <= 13.00
Async replace latency (ms)
             count = 100
               min = 24
               max = 50
              mean = 30.77
            stddev = 3.10
            median = 30.00
              75% <= 32.00
              95% <= 35.00
              98% <= 43.00
              99% <= 50.00
            99.9% <= 50.00
Async upsert RUs
             count = 100
               min = 13
               max = 13
              mean = 13.00
            stddev = 0.00
            median = 13.00
              75% <= 13.00
              95% <= 13.00
              98% <= 13.00
              99% <= 13.00
            99.9% <= 13.00
Async upsert latency (ms)
             count = 100
               min = 26
               max = 41
              mean = 30.45
            stddev = 2.16
            median = 30.00
              75% <= 31.00
              95% <= 34.00
              98% <= 35.00
              99% <= 36.00
            99.9% <= 41.00
Async write RUs
             count = 100
               min = 7
               max = 7
              mean = 7.00
            stddev = 0.00
            median = 7.00
              75% <= 7.00
              95% <= 7.00
              98% <= 7.00
              99% <= 7.00
            99.9% <= 7.00
Async write latency (ms)
             count = 100
               min = 23
               max = 411
              mean = 32.78
            stddev = 37.62
            median = 28.00
              75% <= 31.00
              95% <= 36.00
              98% <= 37.00
              99% <= 42.00
            99.9% <= 411.00
SQL async partition key read RUs
             count = 100
               min = 3
               max = 3
              mean = 3.00
            stddev = 0.00
            median = 3.00
              75% <= 3.00
              95% <= 3.00
              98% <= 3.00
              99% <= 3.00
            99.9% <= 3.00
SQL async partition key read latency (ms)
             count = 100
               min = 0
               max = 84
              mean = 3.38
            stddev = 9.66
            median = 1.00
              75% <= 2.00
              95% <= 16.00
              98% <= 36.00
              99% <= 84.00
            99.9% <= 84.00
SQL async point read RUs
             count = 100
               min = 1
               max = 1
              mean = 1.00
            stddev = 0.00
            median = 1.00
              75% <= 1.00
              95% <= 1.00
              98% <= 1.00
              99% <= 1.00
            99.9% <= 1.00
SQL async point read latency (ms)
             count = 100
               min = 21
               max = 34
              mean = 25.32
            stddev = 2.13
            median = 25.00
              75% <= 26.00
              95% <= 29.00
              98% <= 30.00
              99% <= 31.00
            99.9% <= 34.00
SQL sync partition key read RUs
             count = 100
               min = 3
               max = 3
              mean = 3.00
            stddev = 0.00
            median = 3.00
              75% <= 3.00
              95% <= 3.00
              98% <= 3.00
              99% <= 3.00
            99.9% <= 3.00
SQL sync partition key read latency (ms)
             count = 100
               min = 49
               max = 245
              mean = 61.34
            stddev = 23.26
            median = 57.00
              75% <= 62.00
              95% <= 69.00
              98% <= 157.00
              99% <= 159.00
            99.9% <= 245.00
SQL sync point read RUs
             count = 100
               min = 1
               max = 1
              mean = 1.00
            stddev = 0.00
            median = 1.00
              75% <= 1.00
              95% <= 1.00
              98% <= 1.00
              99% <= 1.00
            99.9% <= 1.00
SQL sync point read latency (ms)
             count = 100
               min = 21
               max = 8209
              mean = 117.84
            stddev = 864.04
            median = 25.00
              75% <= 27.00
              95% <= 31.00
              98% <= 31.00
              99% <= 8209.00
            99.9% <= 8209.00
Sync delete RUs
             count = 100
               min = 7
               max = 7
              mean = 7.00
            stddev = 0.00
            median = 7.00
              75% <= 7.00
              95% <= 7.00
              98% <= 7.00
              99% <= 7.00
            99.9% <= 7.00
Sync delete latency (ms)
             count = 100
               min = 22
               max = 38
              mean = 27.64
            stddev = 3.08
            median = 27.00
              75% <= 29.00
              95% <= 33.00
              98% <= 36.00
              99% <= 37.00
            99.9% <= 38.00
Sync replace RUs
             count = 100
               min = 13
               max = 13
              mean = 13.00
            stddev = 0.00
            median = 13.00
              75% <= 13.00
              95% <= 13.00
              98% <= 13.00
              99% <= 13.00
            99.9% <= 13.00
Sync replace latency (ms)
             count = 100
               min = 26
               max = 38
              mean = 30.29
            stddev = 2.26
            median = 30.00
              75% <= 31.00
              95% <= 35.00
              98% <= 38.00
              99% <= 38.00
            99.9% <= 38.00
Sync upsert RUs
             count = 100
               min = 13
               max = 13
              mean = 13.00
            stddev = 0.00
            median = 13.00
              75% <= 13.00
              95% <= 13.00
              98% <= 13.00
              99% <= 13.00
            99.9% <= 13.00
Sync upsert latency (ms)
             count = 100
               min = 26
               max = 53
              mean = 31.38
            stddev = 3.71
            median = 31.00
              75% <= 33.00
              95% <= 38.00
              98% <= 41.00
              99% <= 53.00
            99.9% <= 53.00
Sync write RUs
             count = 100
               min = 7
               max = 7
              mean = 7.00
            stddev = 0.00
            median = 7.00
              75% <= 7.00
              95% <= 7.00
              98% <= 7.00
              99% <= 7.00
            99.9% <= 7.00
Sync write latency (ms)
             count = 100
               min = 25
               max = 10413
              mean = 131.53
            stddev = 1018.36
            median = 31.00
              75% <= 33.00
              95% <= 37.00
              98% <= 37.00
              99% <= 38.00
            99.9% <= 10413.00

-- Meters ----------------------------------------------------------------------
Async delete throughput
             count = 100
         mean rate = 12.75 events/second
     1-minute rate = 20.00 events/second
     5-minute rate = 20.00 events/second
    15-minute rate = 20.00 events/second
Async replace throughput
             count = 100
         mean rate = 7.47 events/second
     1-minute rate = 16.72 events/second
     5-minute rate = 17.74 events/second
    15-minute rate = 17.91 events/second
Async upsert throughput
             count = 100
         mean rate = 5.29 events/second
     1-minute rate = 15.38 events/second
     5-minute rate = 17.44 events/second
    15-minute rate = 17.81 events/second
Async write throughput
             count = 100
         mean rate = 3.95 events/second
     1-minute rate = 14.33 events/second
     5-minute rate = 18.71 events/second
    15-minute rate = 19.56 events/second
SQL async partition key read throughput
             count = 100
         mean rate = 4.57 events/second
     1-minute rate = 15.58 events/second
     5-minute rate = 19.02 events/second
    15-minute rate = 19.67 events/second
SQL async point read throughput
             count = 100
         mean rate = 4.64 events/second
     1-minute rate = 15.58 events/second
     5-minute rate = 19.02 events/second
    15-minute rate = 19.67 events/second
SQL sync partition key read throughput
             count = 100
         mean rate = 1.76 events/second
     1-minute rate = 7.03 events/second
     5-minute rate = 13.43 events/second
    15-minute rate = 14.97 events/second
SQL sync point read throughput
             count = 100
         mean rate = 1.97 events/second
     1-minute rate = 8.51 events/second
     5-minute rate = 15.35 events/second
    15-minute rate = 16.94 events/second
Sync delete throughput
             count = 100
         mean rate = 3.51 events/second
     1-minute rate = 14.33 events/second
     5-minute rate = 18.71 events/second
    15-minute rate = 19.56 events/second
Sync replace throughput
             count = 100
         mean rate = 2.93 events/second
     1-minute rate = 11.98 events/second
     5-minute rate = 16.59 events/second
    15-minute rate = 17.52 events/second
Sync upsert throughput
             count = 100
         mean rate = 2.51 events/second
     1-minute rate = 10.69 events/second
     5-minute rate = 15.78 events/second
    15-minute rate = 16.84 events/second
Sync write throughput
             count = 100
         mean rate = 1.42 events/second
     1-minute rate = 0.64 events/second
     5-minute rate = 0.28 events/second
    15-minute rate = 0.10 events/second


````
