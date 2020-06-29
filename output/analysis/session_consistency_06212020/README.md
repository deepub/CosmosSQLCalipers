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


## Observations
-  



