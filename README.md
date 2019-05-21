# DBStressTestingUtility
Utility for Stress testing DB for any given connection pool setting

Following parameters in LoadtestApplicationTests.java needs to be configured before running this Utility.
LoadtestApplicationTests.java is JUNIT written in Spring. Currently bean "dataSourceForStress" is datasource and configured to point to H2 database.

1. maxPoolSize - is the max connection objects to be created in the pool
2. numberofUsers - is the number of concurrent users sharing the connection pool
3. queryExecutionTimeMs - query execution time for each user.

A "Read timeout" exception will be thrown if the users are starving for more time then specified in the configuration(setIdleTimeout) for the connection objects in the pool.This exception indicates the connection pool setting is not enough and needs to be refined for the requirement of the system.


