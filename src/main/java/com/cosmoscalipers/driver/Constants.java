package com.cosmoscalipers.driver;

public class Constants {

    public static final String CONST_OPTION_HOSTNAME = "hostname";
    public static final String CONST_OPTION_DATABASE = "database";
    public static final String CONST_OPTION_COLLECTION = "collection";
    public static final String CONST_OPTION_KEY = "key";
    public static final String CONST_OPTION_NUMBER_OF_DOCS = "numberofdocs";
    public static final String CONST_OPTION_CONSISTENCY_LEVEL = "consistencylevel";
    public static final String CONST_OPTION_PROVISIONED_RUS = "provisionedrus";
    public static final String CONST_OPTION_MAX_POOL_SIZE = "maxpoolsize";
    public static final String CONST_OPTION_MAX_RETRY_ATTEMPTS = "maxretryattempts";
    public static final String CONST_OPTION_MAX_RETRY_WAIT_TIME_IN_SECONDS = "maxretrywaittimeinseconds";
    public static final String CONST_OPTION_OPERATION = "operation";
    public static final String CONST_OPTION_PAYLOAD_SIZE = "payloadSize";

    public static final int CONST_MINIMUM_PAYLOAD_SIZE = 800;
    public static final String CONST_PARTITION_KEY = "/payloadId";

    public static final String CONST_OPERATION_SQL_ASYNC_PARTITION_KEY_READ = "SQL_ASYNC_PARTITION_KEY_READ";
    public static final String CONST_OPERATION_SQL_SYNC_PARTITION_KEY_READ = "SQL_SYNC_PARTITION_KEY_READ";
    public static final String CONST_OPERATION_SQL_SYNC_POINT_READ = "SQL_SYNC_POINT_READ";
    public static final String CONST_OPERATION_SQL_ASYNC_POINT_READ = "SQL_ASYNC_POINT_READ";
    public static final String CONST_OPERATION_SQL_ALL = "SQL_ALL";
    public static final String CONST_OPERATION_ALL_SYNC_OPS = "ALL_SYNC_OPS";
    public static final String CONST_OPERATION_ALL_ASYNC_OPS = "ALL_ASYNC_OPS";
    public static final String CONST_OPERATION_SQL_SYNC_UPDATE = "SQL_SYNC_UPDATE";
    public static final String CONST_OPERATION_SQL_ASYNC_UPDATE = "SQL_ASYNC_UPDATE";

    public static final String CONST_CONSISTENCY_LEVEL_STRONG = "STRONG";
    public static final String CONST_CONSISTENCY_LEVEL_BOUNDED_STALENESS = "BOUNDED_STALENESS";
    public static final String CONST_CONSISTENCY_LEVEL_SESSION = "SESSION";
    public static final String CONST_CONSISTENCY_LEVEL_CONSISTENT_PREFIX = "CONSISTENT_PREFIX";
    public static final String CONST_CONSISTENCY_LEVEL_EVENTUAL = "EVENTUAL";

}
