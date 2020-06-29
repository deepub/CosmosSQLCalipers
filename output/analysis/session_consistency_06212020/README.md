# Analysis Objective
Provide commentary on the RU consumption pattern for this test run. Please refer to Cosmos documentation for an explanation of the terms.

## Test Setup
- Cosmos account configured with session consistency and in a single region - West US
- Client running on a local desktop in San Diego, CA
- Max provisioned RUs = 400
- MaxRetryWaitTimeInSeconds = 10, MaxRetryAttempts = 100 and MaxPoolSize = 10
- Test executed for the following payload sizes: 1000, 5000, 10000, 50000, 100000, 200000 and 400000 bytes
- Each replace and upsert test adds an additional prefix called 'replaced' and 'upserted'
- RUs are collected using Histograms and that can only be an int or a long. Hence, I am using the Math.round() function to round off the RUs
- Test run executed with operation = SQL_ALL 
- Documents created with a single partition key index.

## Observations

### CRUD operations
- As expected, RUs for CRUD operations remain the same irrespective of the execution mode - sync or async
- For the sake of this discussion, I'll present a summary of the data collected in the async run

| Operation and payload size | RUs Consumed |
| --- | --- |
| async_write_1k | 7 |
| async_write_5k |	8 |
| async_write_10k |	10 |
| async_write_50k |	24 |
| async_write_100k | 49 |
| async_write_200k | 99 |
| async_write_400k | 186 |
| async_delete_1k | 7 |
| async_delete_5k | 8 |
| async_delete_10k | 10 |
| async_delete_50k | 24 |
| async_delete_100k | 49 |
| async_delete_200k | 99 |
| async_delete_400k | 186 |
| async_replace_1k | 13 |
| async_replace_5k | 14 |
| async_replace_10k | 19 |
| async_replace_50k | 46 |
| async_replace_100k | 97 |
| async_replace_200k | 197 |
| async_replace_400k | 370 |
| async_upsert_1k | 13 |
| async_upsert_5k | 14 |
| async_upsert_10k | 19 |
| async_upsert_50k | 46 |
| async_upsert_100k | 97 |
| async_upsert_200k	| 197 |
| async_upsert_400k | 370 |

![CRUD operations RU consumption](Async%20CRUD%20op%20RUs.png)

- The size of the payload is directly proportional to the RU consumption. The bigger the document, the more the RUs you will end up being charged. Scalability and cost efficiency in Cosmos DB comes from leveraging smaller document sizes. 
- The above graph shows a very interesting pattern. While create and delete operations consume the exact same RUs for a given payload, replace and upsert operations result in a 2x RU consumption. This implies that update/replace operations are twice as expensive as an insert or delete. A typical OLTP use case will usually consist of document creation followed by multiple updates to that document during the entire data lifecycle. This RU consumption pattern directly implies in a post creation cost escalation.
Furthermore, the ![Cosmos capacity calculator](Cosmos%20Capacity%20Calculator.png) does not help in the correct cost estimation since the writes/sec/region only accounts for document creation. Given that replace/upsert is 2x the cost of a create/delete, we can make the case that replace or upsert/sec/region should be called out separately.

### Read operations
The Cosmos SQL API provides two ways to perform read operations.
- Point reads. Supposed to be more efficient both in terms of latency and RU consumption. A document of size <= 1KB will consume 1 RU. 
- Partition key based SQL queries. Undergoes multiple phases during execution so typically more expensive. A document of size <= 1KB will consume 3 RUs.
- The data gathered from the test shows a very interesting pattern

| Operation and payload size | RUs Consumed |
| --- | --- |
| async_point_read_1k | 1 |
| async_point_read_5k | 1 |
| async_point_read_10k | 2 |
| async_point_read_50k | 5 |
| async_point_read_100k | 10 |
| async_point_read_200k | 20 |
| async_point_read_400k | 41 |
| async_partition_read_1k | 3 |
| async_partition_read_5k | 3 |
| async_partition_read_10k | 3 |
| async_partition_read_50k | 4 |
| async_partition_read_100k | 7 |
| async_partition_read_200k | 10 |
| async_partition_read_400k | 17 |

![Async point read vs partition key RU consumption](Async%20point%20read%20vs%20partition%20read%20RU%20consumption.png)

- Point read operations for document sizes up to 5KB cost 1 RU. This is the lowest tier.
- Partition key read operations start at 3 RUs and remain at that level up to 10KB.
- As document sizes start increasing, point read RU costs start becoming far more expensive compared to partition key based read operations. At the max level, a point read for a 400KB size document costs 41 RUs compared to 17 RUs for the same document read using the partition key.
 
 

  
        



     




