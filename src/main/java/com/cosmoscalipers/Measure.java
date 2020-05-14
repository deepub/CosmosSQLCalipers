package com.cosmoscalipers;

import com.azure.data.cosmos.ConsistencyLevel;
import com.cosmoscalipers.driver.BenchmarkConfig;
import com.cosmoscalipers.driver.Constants;
import com.cosmoscalipers.driver.LoadRunner;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Measure
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Measure.class);

    public static void main( String[] args )
    {

        Measure measure = new Measure();
        CommandLine commandLine = measure.setupOptions(args);
        BenchmarkConfig config = measure.createConfig(commandLine);
        LoadRunner loadRunner = new LoadRunner();
        loadRunner.execute(config);
    }


    private CommandLine setupOptions(String[] args) {

        Options commandLineOptions = new Options();

        commandLineOptions.addOption( Option.builder().longOpt(Constants.CONST_OPTION_HOSTNAME).required(true).hasArg().desc("Cosmos Service endpoint").build()  );
        commandLineOptions.addOption( Option.builder().longOpt(Constants.CONST_OPTION_DATABASE).required(true).hasArg().desc("Enter Cosmos Database Name").build()  );
        commandLineOptions.addOption( Option.builder().longOpt(Constants.CONST_OPTION_COLLECTION).required(true).hasArg().desc("Cosmos collection name").build()  );
        commandLineOptions.addOption( Option.builder().longOpt(Constants.CONST_OPTION_KEY).required(true).hasArg().desc("Cosmos key for authentication").build()  );
        commandLineOptions.addOption( Option.builder().longOpt(Constants.CONST_OPTION_NUMBER_OF_DOCS).required(true).hasArg().desc("Number of documents to be tested").build()  );
        commandLineOptions.addOption( Option.builder().longOpt(Constants.CONST_OPTION_PAYLOAD_SIZE).required(false).hasArg().desc("Document size").build()  );
        commandLineOptions.addOption( Option.builder().longOpt(Constants.CONST_OPTION_CONSISTENCY_LEVEL).required(true).hasArg().desc("Consistency level to be exercised").build()  );
        commandLineOptions.addOption( Option.builder().longOpt(Constants.CONST_OPTION_PROVISIONED_RUS).required(true).hasArg().desc("RUs to be provisioned when Cosmos container is created").build()  );
        commandLineOptions.addOption( Option.builder().longOpt(Constants.CONST_OPTION_MAX_POOL_SIZE).required(true).hasArg().desc("Max pool size").build()  );
        commandLineOptions.addOption( Option.builder().longOpt(Constants.CONST_OPTION_MAX_RETRY_ATTEMPTS).required(true).hasArg().desc("Max retry attempts when backpressure is encountered").build()  );
        commandLineOptions.addOption( Option.builder().longOpt(Constants.CONST_OPTION_MAX_RETRY_WAIT_TIME_IN_SECONDS).required(true).hasArg().desc("Max retry wait time in seconds").build()  );
        commandLineOptions.addOption( Option.builder().longOpt(Constants.CONST_OPTION_OPERATION).required(false).hasArg().desc("Primary operation being exercised").build()  );

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = null;

        try {
            commandLine = parser.parse(commandLineOptions, args);
        } catch (ParseException | NumberFormatException e) {
            System.out.println(e.getMessage());
            new HelpFormatter().printHelp("Cosmos DB SQL Benchmark", commandLineOptions);
        }

        return commandLine;
    }

    private BenchmarkConfig createConfig(CommandLine commandLine) {

        ConsistencyLevel consistencyLevel = null;
        BenchmarkConfig config = new BenchmarkConfig();
        int maxPoolSize = Integer.parseInt(commandLine.getOptionValue(Constants.CONST_OPTION_MAX_POOL_SIZE));
        int maxRetryAttempts = Integer.parseInt(commandLine.getOptionValue(Constants.CONST_OPTION_MAX_RETRY_ATTEMPTS));
        int retryWaitTimeInSeconds = Integer.parseInt(commandLine.getOptionValue(Constants.CONST_OPTION_MAX_RETRY_WAIT_TIME_IN_SECONDS));
        int numberOfItems = Integer.parseInt(commandLine.getOptionValue(Constants.CONST_OPTION_NUMBER_OF_DOCS));
        int payloadSize = Integer.parseInt(commandLine.getOptionValue(Constants.CONST_OPTION_PAYLOAD_SIZE));
        int provisionedRUs = Integer.parseInt(commandLine.getOptionValue(Constants.CONST_OPTION_PROVISIONED_RUS));

        String hostName = commandLine.getOptionValue(Constants.CONST_OPTION_HOSTNAME);
        String databaseName = commandLine.getOptionValue(Constants.CONST_OPTION_DATABASE);
        String collection = commandLine.getOptionValue(Constants.CONST_OPTION_COLLECTION);
        String masterKey = commandLine.getOptionValue(Constants.CONST_OPTION_KEY);
        String consistencyOption = commandLine.getOptionValue(Constants.CONST_OPTION_CONSISTENCY_LEVEL);
        String operation = commandLine.getOptionValue(Constants.CONST_OPTION_OPERATION);

        if(operation == null || operation.isEmpty() || operation.isBlank()) {
            operation = Constants.CONST_OPERATION_SQL_ALL;
        }

        if(payloadSize == 0) {
            payloadSize = Constants.CONST_MINIMUM_PAYLOAD_SIZE;
        }

        switch(consistencyOption) {
            case Constants.CONST_CONSISTENCY_LEVEL_STRONG:
                consistencyLevel = ConsistencyLevel.STRONG;
                break;

            case Constants.CONST_CONSISTENCY_LEVEL_BOUNDED_STALENESS:
                consistencyLevel = ConsistencyLevel.BOUNDED_STALENESS;
                break;

            case Constants.CONST_CONSISTENCY_LEVEL_SESSION:
                consistencyLevel = ConsistencyLevel.SESSION;
                break;

            case Constants.CONST_CONSISTENCY_LEVEL_CONSISTENT_PREFIX:
                consistencyLevel = ConsistencyLevel.CONSISTENT_PREFIX;
                break;

            case Constants.CONST_CONSISTENCY_LEVEL_EVENTUAL:
                consistencyLevel = ConsistencyLevel.EVENTUAL;
                break;
        }

        config.setHost(hostName);
        config.setDatabaseId(databaseName);
        config.setCollectionId(collection);
        config.setMasterKey(masterKey);
        config.setNumberOfDocuments(numberOfItems);
        config.setPayloadSize(payloadSize);
        config.setConsistencyLevel(consistencyLevel);
        config.setProvisionedRUs(provisionedRUs);
        config.setMaxPoolSize(maxPoolSize);
        config.setMaxRetryAttempts(maxRetryAttempts);
        config.setRetryWaitTimeInSeconds(retryWaitTimeInSeconds);
        config.setOperation(operation);

        System.out.println("host name : " + config.getHost());
        System.out.println("database : " + config.getDatabaseId());

        return config;
    }
}
