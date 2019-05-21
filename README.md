# DBStressTestingUtility
Utility for Stress testing DB for any given connection pool setting

Following parameters and the "dataSourceForStress" in LoadtestApplicationTests.java needs to be configured before running the Utility.
Currently "dataSourceForStress" is configured to H2 database.

1. maxPoolSize - is the max connection objects to be created in the pool
2. numberofUsers - is the number of concurrent users sharing the connection pool
3. queryExecutionTimeMs - query execution time for each user.

A "Read timeout" exception will be thrown if the users are starving for the connection objects in the pool.


