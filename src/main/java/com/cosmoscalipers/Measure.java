package com.cosmoscalipers;

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
        LoadRunner loadRunner = new LoadRunner();
        loadRunner.execute(commandLine);
    }


    private CommandLine setupOptions(String[] args) {

        /**
         * Usage:
         * --hostname https://dbcosmosdb.documents.azure.com:443/
         * --database demo
         * --collection orders
         * --key vx2airQoJf8UU7UPogflnnxnqu8cdqPrCGCo1MnZQtbXfqYgBz1Tn4mnrzBSFux2hFI0Ci5IEx6BAfTPkQorhg==
         * --numberofdocs 100
         * --payloadSize 1000
         * --consistencylevel SESSION
         * --provisionedrus 400
         * --maxpoolsize 1000
         * --maxretryattempts 10
         * --maxretrywaittimeinseconds 1
         * --operation test
         * Examples
         * --hostname https://dbcosmosdemodb.documents.azure.com:443/ --database demo --collection orders --key t33UBPbsMY67mYy8vIvbPE16p3KlD0E6D3av8UczQcxURiYfKesciE22mcLgdQio6D4rqQSP16oWVwdKxwWfnw== --numberofdocs 1000 --payloadSize 500 --consistencylevel SESSION --provisionedrus 400 --maxpoolsize 100 --maxretryattempts 10 --maxretrywaittimeinseconds 1 --operation SQL_ALL
         */

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

        System.out.println("host name : " + commandLine.getOptionValue(Constants.CONST_OPTION_HOSTNAME));
        System.out.println("database : " + commandLine.getOptionValue(Constants.CONST_OPTION_DATABASE));

        return commandLine;
    }

}
