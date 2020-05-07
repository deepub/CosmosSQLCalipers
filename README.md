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
CosmosSQLCalipers relies on the azure-cosmos v3 SDK at present

#### Test Results
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
1. SQL_ASYNC_UPDATE: Executes update operations asynchronously
1. SQL_SYNC_UPDATE: Executes update operations synchronously 
1. ALL_SYNC_OPS: Executes all synchronous operations serially. The execution cycle starts with partition key based reads followed by point reads.  
1. ALL_ASYNC_OPS: Executes all asynchronous operations serially. The execution cycle starts with partition key based reads followed by point reads.
1. SQL_ALL: Executes async partition key read, sync partition key read, async point read and sync point read operations serially.

## Instructions
1. Create a Cosmos DB SQL API account
    1. Assign consistency level you want to test with
    1. Select region depending on your deployment preferences
    1. Single or multiple regions
    1. Choose an active/active or active/passive topology
2. Run the benchmark test against the provisioned Cosmos account     

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
````

## Example 1

````
--hostname https://youraccount.documents.azure.com:443/ --database demo --collection orders --key <your account key> --numberofdocs 1000 --payloadSize 500 --consistencylevel SESSION --provisionedrus 400 --maxpoolsize 100 --maxretryattempts 10 --maxretrywaittimeinseconds 1 --operation SQL_ALL
````

````
********************************************************************************************
Running sync SQL bootstrap workload for 1000 docs...
Running partition key based sync SQL query workload for 1000 docs...
Running partition key based async SQL query workload for 1000 docs...
Running sync point read workload 1000 docs...
Running point read workload for 1000 docs...
Running delete workload for 1000 docs...
********************************************************************************************
4/22/20, 5:20:01 PM ============================================================

-- Counters --------------------------------------------------------------------
Write failure counter
             count = 0
Write success counter
             count = 1000

-- Histograms ------------------------------------------------------------------
SQL async partition key read RUs
             count = 1000
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
             count = 1000
               min = 50
               max = 138
              mean = 66.77
            stddev = 9.43
            median = 65.00
              75% <= 72.00
              95% <= 79.00
              98% <= 92.00
              99% <= 106.00
            99.9% <= 138.00
SQL async point read RUs
             count = 1000
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
             count = 1000
               min = 26
               max = 84
              mean = 34.24
            stddev = 5.84
            median = 33.00
              75% <= 36.00
              95% <= 44.00
              98% <= 55.00
              99% <= 61.00
            99.9% <= 82.00
SQL sync partition key select read RUs
             count = 1000
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
SQL sync partition key select read latency (ms)
             count = 1000
               min = 50
               max = 567
              mean = 67.06
            stddev = 14.93
            median = 65.00
              75% <= 72.00
              95% <= 79.00
              98% <= 83.00
              99% <= 93.00
            99.9% <= 165.00
SQL sync point read RUs
             count = 1000
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
             count = 1000
               min = 26
               max = 91
              mean = 35.76
            stddev = 7.79
            median = 34.00
              75% <= 37.00
              95% <= 52.00
              98% <= 63.00
              99% <= 68.00
            99.9% <= 77.00
Sync Write Latency (ms)
             count = 1000
               min = 30
               max = 10701
              mean = 50.14
            stddev = 356.21
            median = 35.00
              75% <= 38.00
              95% <= 43.00
              98% <= 47.00
              99% <= 51.00
            99.9% <= 7387.00
Sync delete RUs
             count = 1000
               min = 6
               max = 6
              mean = 6.00
            stddev = 0.00
            median = 6.00
              75% <= 6.00
              95% <= 6.00
              98% <= 6.00
              99% <= 6.00
            99.9% <= 6.00
Sync delete latency (ms)
             count = 1000
               min = 28
               max = 5340
              mean = 46.58
            stddev = 181.71
            median = 38.00
              75% <= 42.00
              95% <= 59.00
              98% <= 68.00
              99% <= 80.00
            99.9% <= 5340.00
Write RUs
             count = 1000
               min = 6
               max = 6
              mean = 6.00
            stddev = 0.00
            median = 6.00
              75% <= 6.00
              95% <= 6.00
              98% <= 6.00
              99% <= 6.00
            99.9% <= 6.00

-- Meters ----------------------------------------------------------------------
SQL async partition key select read throughput
             count = 1000
         mean rate = 4.39 events/second
     1-minute rate = 1.08 events/second
     5-minute rate = 9.04 events/second
    15-minute rate = 12.89 events/second
SQL async point read throughput
             count = 1000
         mean rate = 8.05 events/second
     1-minute rate = 7.00 events/second
     5-minute rate = 21.94 events/second
    15-minute rate = 26.55 events/second
SQL sync partition key select read throughput
             count = 1000
         mean rate = 3.39 events/second
     1-minute rate = 0.32 events/second
     5-minute rate = 6.34 events/second
    15-minute rate = 10.34 events/second
SQL sync point read throughput
             count = 1000
         mean rate = 6.21 events/second
     1-minute rate = 3.38 events/second
     5-minute rate = 16.98 events/second
    15-minute rate = 22.21 events/second
Sync Write Throughput
             count = 1000
         mean rate = 2.85 events/second
     1-minute rate = 0.09 events/second
     5-minute rate = 1.17 events/second
    15-minute rate = 0.78 events/second
Sync delete throughput
             count = 1000
         mean rate = 11.21 events/second
     1-minute rate = 11.88 events/second
     5-minute rate = 12.98 events/second
    15-minute rate = 13.25 events/second

````

## Example 2

````
--hostname https://xxx.documents.azure.com:443/ --database demo --collection orders --key xxx --numberofdocs 5000 --payloadSize 500 --consistencylevel SESSION --provisionedrus 400 --maxpoolsize 100 --maxretryattempts 10 --maxretrywaittimeinseconds 1 --operation SQL_ALL
````

````

********************************************************************************************
Running sync SQL bootstrap workload for 5000 docs...
Running partition key based sync SQL query workload for 5000 docs...
Running partition key based async SQL query workload for 5000 docs...
Running sync point read workload 5000 docs...
Running point read workload for 5000 docs...
Running delete workload for 5000 docs...
********************************************************************************************
4/22/20, 6:23:41 PM ============================================================

-- Counters --------------------------------------------------------------------
Write failure counter
             count = 0
Write success counter
             count = 5000

-- Histograms ------------------------------------------------------------------
SQL async partition key read RUs
             count = 5000
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
             count = 5000
               min = 43
               max = 187
              mean = 54.16
            stddev = 7.85
            median = 53.00
              75% <= 55.00
              95% <= 61.00
              98% <= 68.00
              99% <= 84.00
            99.9% <= 187.00
SQL async point read RUs
             count = 5000
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
             count = 5000
               min = 19
               max = 81
              mean = 22.44
            stddev = 3.61
            median = 22.00
              75% <= 23.00
              95% <= 28.00
              98% <= 31.00
              99% <= 38.00
            99.9% <= 74.00
SQL sync partition key select read RUs
             count = 5000
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
SQL sync partition key select read latency (ms)
             count = 5000
               min = 43
               max = 1421
              mean = 56.75
            stddev = 17.49
            median = 55.00
              75% <= 59.00
              95% <= 67.00
              98% <= 81.00
              99% <= 89.00
            99.9% <= 260.00
SQL sync point read RUs
             count = 5000
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
             count = 5000
               min = 18
               max = 67
              mean = 23.22
            stddev = 3.71
            median = 22.00
              75% <= 24.00
              95% <= 31.00
              98% <= 34.00
              99% <= 39.00
            99.9% <= 52.00
Sync Write Latency (ms)
             count = 5000
               min = 22
               max = 56
              mean = 26.17
            stddev = 3.09
            median = 25.00
              75% <= 27.00
              95% <= 32.00
              98% <= 35.00
              99% <= 39.00
            99.9% <= 56.00
Sync delete RUs
             count = 5000
               min = 6
               max = 6
              mean = 6.00
            stddev = 0.00
            median = 6.00
              75% <= 6.00
              95% <= 6.00
              98% <= 6.00
              99% <= 6.00
            99.9% <= 6.00
Sync delete latency (ms)
             count = 5000
               min = 21
               max = 68
              mean = 25.91
            stddev = 3.16
            median = 25.00
              75% <= 26.00
              95% <= 32.00
              98% <= 35.00
              99% <= 36.00
            99.9% <= 52.00
Write RUs
             count = 5000
               min = 6
               max = 6
              mean = 6.00
            stddev = 0.00
            median = 6.00
              75% <= 6.00
              95% <= 6.00
              98% <= 6.00
              99% <= 6.00
            99.9% <= 6.00

-- Meters ----------------------------------------------------------------------
SQL async partition key select read throughput
             count = 5000
         mean rate = 6.47 events/second
     1-minute rate = 0.00 events/second
     5-minute rate = 3.44 events/second
    15-minute rate = 10.16 events/second
SQL async point read throughput
             count = 5000
         mean rate = 13.23 events/second
     1-minute rate = 0.61 events/second
     5-minute rate = 17.27 events/second
    15-minute rate = 30.03 events/second
SQL sync partition key select read throughput
             count = 5000
         mean rate = 4.74 events/second
     1-minute rate = 0.00 events/second
     5-minute rate = 1.31 events/second
    15-minute rate = 6.89 events/second
SQL sync point read throughput
             count = 5000
         mean rate = 10.06 events/second
     1-minute rate = 0.08 events/second
     5-minute rate = 11.15 events/second
    15-minute rate = 25.12 events/second
Sync Write Throughput
             count = 5000
         mean rate = 4.16 events/second
     1-minute rate = 0.00 events/second
     5-minute rate = 0.40 events/second
    15-minute rate = 1.60 events/second
Sync delete throughput
             count = 5000
         mean rate = 19.44 events/second
     1-minute rate = 18.93 events/second
     5-minute rate = 19.87 events/second
    15-minute rate = 20.19 events/second


````
