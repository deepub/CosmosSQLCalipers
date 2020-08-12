# Analysis Objective
Provide commentary on the latency improvements in v4.3 for this test run.

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
- The following observations are based on the session consistency level.
- Data is available for strong, bounded staleness and eventual consistency levels if needed.   

### CRUD operations
- End to end latency numbers have improved substantially between v4.1 vs v4.3. This is applicable when upgrading from v4.2 as well.
- Intermittent spikes in latency times have been eliminated

- Sync create operations

![Session sync write latency](Session%20sync%20write%20latency%20(ms)%20comparison%20between%20v4.1%20and%20v4.3.png)

- Async create operations

![Session async write latency](Session%20async%20write%20latency%20(ms)%20comparison%20between%20v4.1%20and%20v4.3.png)

- Sync upsert operations

![Session sync upsert latency](Session%20sync%20upsert%20latency%20(ms)%20comparison%20between%20v4.1%20and%20v4.3.png)

- Async upsert operations

![Session async upsert latency](Session%20async%20upsert%20latency%20(ms)%20comparison%20between%20v4.1%20and%20v4.3.png)


### Read operations

- Sync point read operation

![Session sync point read](Session%20sync%20point%20read%20latency%20(ms)%20comparison%20between%20v4.1%20and%20v4.3.png)

- Async point read operation

![Session async point read](Session%20async%20point%20read%20latency%20(ms)%20comparison%20between%20v4.1%20and%20v4.3.png)

- Sync partition key based lookup   

![Session sync partition key based lookup](Session%20sync%20partition%20key%20read%20latency%20(ms)%20comparison%20between%20v4.1%20vs%20v4.3.png)

- Async partition key based lookup        

![Session async partition key based lookup](Session%20async%20partition%20key%20read%20latency%20(ms)%20comparison%20between%20v4.1%20and%20v4.3.png)


     




