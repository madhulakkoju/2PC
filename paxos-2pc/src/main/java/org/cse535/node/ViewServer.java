package org.cse535.node;

import org.cse535.Main;
import org.cse535.configs.GlobalConfigs;
import org.cse535.configs.Utils;

import org.cse535.proto.*;
import org.cse535.threadimpls.CrossShardTnxProcessingThread;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class ViewServer extends NodeServer {


    public int TestSetNumber;

    public static class TnxLine{
        public TransactionInputConfig transactionInputConfig;
        public HashMap<Integer, String> clusterContactServermapping;

        public TnxLine(TransactionInputConfig transactionInputConfig, List<String> contactServers){
            this.transactionInputConfig = transactionInputConfig;
            clusterContactServermapping = new HashMap<>();

            for(String server : contactServers){
                clusterContactServermapping.put( Utils.FindMyCluster( server ), server);
            }
        }

    }




    public enum Command {
        PrintDB,
        PrintLog,
        Performance,
        PrintDataStore,
        PrintBalance
    }


    public long startTime = System.currentTimeMillis();
    public long endTime = System.currentTimeMillis();
    public long transactionsCount = 0;




    HashMap<Integer, Boolean> activeServersStatusMap = new HashMap<>();


    public HashMap<Integer, String> transactionStatuses = new HashMap<>();

    public HashSet<Integer> participatingDataItems = new HashSet<>();



    public ViewServer(String serverName, int port) {
        super(0, port);
        try {
            this.server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static TnxLine parseTnxConfig(String line, int tnxCount) {

        if( line.trim().length() == 0){
            return null;
        }


//        String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
//
//
//        // Parse the values, trimming whitespace
//        int testCaseCount = Integer.parseInt(parts[0].replaceAll("[^0-9]", "").trim());  // Trimmed to remove any whitespace
//
//        String[] tnx = parts[1].replaceAll("\"", "").replace("(","").replace(")","").trim()
//                .split(",");  // Clean and trim
//
//        String listString = parts[2].replaceAll("[\\[\\]\"]", "").trim();  // Clean and trim
//
//        List<String> activeServers = Arrays.asList(listString.split(","));
//
//        String[] maliciousServers = parts[3].replaceAll("[\\[\\]]", "").replaceAll("\"","").trim().split(",");  // Clean and trim
//
//        Transaction transaction = Transaction.newBuilder()
//                .setSender(tnx[0])
//                .setReceiver(tnx[1])
//                .setAmount(Integer.parseInt(tnx[2].replace(" ","")))
//                .setTransactionNum(tnxCount)
//                .build();





        //String line = "2,\"(F, B, 3)\",\"[S1, S2, S3, S4, S5, S6, S7]\",\"[S4, S6]\"";

// Split by commas, respecting quoted commas.
        String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

// Parse the test case count (assuming the first part is a simple number)
        int testCaseCount = Integer.parseInt(parts[0].trim()); // No need to use replaceAll for numbers

        viewServer.TestSetNumber = testCaseCount;

// Parse the transaction details (sender, receiver, amount)
        String[] tnx = parts[1].replaceAll("[()\"]", "").trim().split(","); // Clean up and split
        String sender = tnx[0].trim();
        String receiver = tnx[1].trim();
        int amount = Integer.parseInt(tnx[2].trim());  // Parse amount as an integer

// Parse the list of active servers
        String[] activeServers = parts[2].replaceAll("[\\[\\]\"]", "").trim().split(",");

        List<String> activeServersList = new ArrayList<>();


        for(int i =0;i<activeServers.length;i++){
            if(activeServers[i].trim().length() > 0)
                activeServersList.add( activeServers[i].trim());
        }

// Parse the list of malicious servers
        String[] maliciousServers = parts[3].replaceAll("[\\[\\]\"]", "").trim().split(",");

        List<String> contactServersList = new ArrayList<>();

        for(int i =0;i<maliciousServers.length;i++){
            if(!maliciousServers[i].trim().isEmpty())
                contactServersList.add( maliciousServers[i].trim());
        }

        // Now you can create your transaction
        Transaction transaction = Transaction.newBuilder()
                .setSender(Integer.parseInt(sender))
                .setReceiver(Integer.parseInt(receiver))
                .setAmount(amount)
                .setTransactionNum(tnxCount) // Assuming you want to set this as the transaction number
                .setIsCrossShard( Utils.IsTransactionCrossCluster(Integer.parseInt(sender), Integer.parseInt(receiver)) )
                .build();

        return new TnxLine(TransactionInputConfig.newBuilder()
                .setSetNumber(testCaseCount)
                .setTransaction(transaction)
                .addAllServerNames(activeServersList)
                .build(), contactServersList);
    }


    public void PrintDataStore(){
        this.logger.log("Printing Data Store");

        this.commandLogger.log("---------------------------------------- Print Data Store -----------------------------------\n");
        CommandInput commandInput = CommandInput.newBuilder().build();
        HashMap<Integer, String> dataStore = new HashMap<>();

        activeServersStatusMap.forEach((server, isActive) -> {
            if(server == 0) return;
            CommandsGrpc.CommandsBlockingStub stub = this.serversToCommandsStub.get(server);
            CommandOutput op  = CommandOutput.newBuilder().setOutput("No Output").build() ;
            op = stub.printDatastore(commandInput);
            dataStore.put(server, op.getOutput());
        });

        this.commandLogger.log("Server \\ Data Store");
        dataStore.forEach((server, data) -> {
            this.commandLogger.log( data);
        });

        this.commandLogger.log("\n\n-----------------------------------Transaction Statuses:-----------------------------------\n");

        this.transactionStatuses.forEach((tnxNum, status) -> {
            this.commandLogger.log(" " + String.format("%3d", tnxNum) + " :: Transaction " + Utils.toDataStoreString(transactions.get(tnxNum)) + " : " + status);
        });
    }

    public void PrintBalance(){
        this.commandLogger.log("------------------------------PrintBalance---------------------------------\n");

        this.participatingDataItems.forEach( dataItem -> {
            CommandInput commandInput = CommandInput.newBuilder().setInput( String.valueOf(dataItem) ).build();
            StringBuilder balances = new StringBuilder();

            int cluster = Utils.FindClusterOfDataItem(dataItem);

            GlobalConfigs.clusterToServersMap.get(cluster).forEach( server -> {
                if(server == 0) return;
                CommandsGrpc.CommandsBlockingStub stub = this.serversToCommandsStub.get(server);
                CommandOutput op = stub.printBalance(commandInput);
                balances.append(op.getOutput()).append("; ");
            });

            this.commandLogger.log( String.format("%4d", dataItem) + " Balances: " + balances.toString());
        });

        this.commandLogger.log("----------------------------------------------------------------------------\n");

    }

    public void PrintPerformance(){
        double latency = endTime - startTime;
        double throughput = 0.0;
        if( latency != 0.0){
            throughput = (transactionsCount / latency)*1000;
        }

        this.commandLogger.log("-------------------------Performance---------------------------------\n"
                + "Total Transactions: " + transactionsCount + "\n"
                + "Latency: " + String.format("%.8f",latency) + " ms\n"
                + "Throughput: " + String.format("%.8f", throughput) + " tps\n" +
                "----------------------------------------------------------------------------\n");
    }

    public void sendCommandToServers(Command commandType) throws InterruptedException {

        if(commandType == Command.PrintDataStore){
            PrintDataStore();
            return;
        }

        if(commandType == Command.PrintBalance){
            PrintBalance();
            return;
        }

        if(commandType == Command.Performance){
            PrintPerformance();
            return;
        }

        CommandInput commandInput = CommandInput.newBuilder().build();
        Thread.sleep(10);

        activeServersStatusMap.forEach((server, isActive) -> {

            CommandsGrpc.CommandsBlockingStub stub = this.serversToCommandsStub.get(server);
            CommandOutput op  = CommandOutput.newBuilder().setOutput("No Output").build() ;

            switch (commandType) {
                case PrintDB:
                    op = stub.printDB(commandInput);
                    break;
                case PrintLog:
                    op = stub.printLog(commandInput);
                    break;
            }

            //this.logger.log("Command: " + commandType + "\n server: " + server + "\n output: \n"+ op.getOutput());

        });

    }


    public void sendTransactionToServer(TransactionInputConfig transactionInputConfig, String server){

        participatingDataItems.add(transactionInputConfig.getTransaction().getSender());
        participatingDataItems.add(transactionInputConfig.getTransaction().getReceiver());
        serversToPaxosStub.get( Integer.parseInt(server.replaceAll("S","")) ).request(transactionInputConfig);

    }

    public void sendCrossShardTransaction(TransactionInputConfig transactionInputConfig, String senderServer, String receiverServer){
        participatingDataItems.add(transactionInputConfig.getTransaction().getSender());
        participatingDataItems.add(transactionInputConfig.getTransaction().getReceiver());
        try {
            CrossShardTnxProcessingThread thread = new CrossShardTnxProcessingThread(this, transactionInputConfig, senderServer, receiverServer);
            thread.start();

            //need not wait for the thread to finish
            //thread.join();

        }
        catch (Exception e){
            this.logger.log("Error in Cross Shard Transaction Processing: "+ e.getMessage());
        }
    }




    public HashMap<Integer, Transaction> transactions = new HashMap<>();


    public static ViewServer viewServer;

    public static void main(String[] args) throws InterruptedException, IOException {

//        int viewServerNum = Integer.parseInt(args[0]);
//        GlobalConfigs.TotalServers = Integer.parseInt(args[1]);
//        GlobalConfigs.numServersPerCluster = Integer.parseInt(args[2]);
//        GlobalConfigs.TotalDataItems = Integer.parseInt(args[3]);

        int viewServerNum = 0;
        GlobalConfigs.TotalServers = 9;
        GlobalConfigs.numServersPerCluster = 3;
        GlobalConfigs.TotalDataItems = 3000;


        GlobalConfigs.LoadConfigs();

        viewServer = new ViewServer( GlobalConfigs.ViewServerName, GlobalConfigs.ViewServerPort );

        for (Integer serverNum : GlobalConfigs.ServerToPortMap.keySet()) {
            viewServer.activeServersStatusMap.put(serverNum, true);
        }

        String path = "src/main/resources/Test Cases - Lab3.csv";

        File file = new File(path);
        String line;


        int tnxCount = 1;
        int lineNum = 0;



        if (file.exists()) {
            System.out.println("File exists");

            // Read the file
            BufferedReader br = new BufferedReader(new FileReader(path));

            int prevSetNumber = 0;

            while ((line = br.readLine()) != null)   //returns a Boolean value
            {
                lineNum++;

                //Thread.sleep(5);

                // System.out.println("Line: " + line);
                viewServer.logger.log("-------------------------------------------------------------\nLine: "+ lineNum +" : "+ line);


                TnxLine tnxLine = parseTnxConfig(line, tnxCount++);

                if(tnxLine == null) {
                    continue;
                }

                TransactionInputConfig transactionInputConfig = tnxLine.transactionInputConfig;

                if (transactionInputConfig == null) {
                    //System.out.println("Invalid transaction");
                    tnxCount -- ;

                    continue;
                }

                // Trigger Inactive servers to stop accepting transactions
                if (transactionInputConfig.getServerNamesList().isEmpty()) {
                    System.out.println("No servers to send the transaction to");
                    continue;
                }

                //Activate or deactivate Servers
                if(prevSetNumber != transactionInputConfig.getSetNumber()) {
                    prevSetNumber = transactionInputConfig.getSetNumber();

                    // If the Test Set Number changes, then trigger the inactive servers to stop accepting transactions

                    // Set all servers inactive
                    for (Integer server : GlobalConfigs.ServerToPortMap.keySet()) {
                        viewServer.activeServersStatusMap.put(server, false);
                    }
                    // Set the active servers
                    for (String server : transactionInputConfig.getServerNamesList()) {
                        viewServer.activeServersStatusMap.put( Integer.parseInt( server.replaceAll("S","") ) , true);
                    }

                    Thread.sleep(10);
                    System.out.print("Press Enter to run Commands ");
                    System.console().readLine();

                    viewServer.sendCommandToServers( Command.PrintDB );
                    viewServer.sendCommandToServers( Command.PrintLog );
                    viewServer.sendCommandToServers( Command.PrintDataStore );
                    viewServer.sendCommandToServers( Command.PrintBalance );
                    viewServer.sendCommandToServers( Command.Performance );

                    System.out.print("Press Enter to continue to next Test set "+transactionInputConfig.getSetNumber() + " ");
                    System.console().readLine();

                    viewServer.participatingDataItems.clear();
                    viewServer.transactionsCount = 0;

                    for( Integer server : GlobalConfigs.ServerToPortMap.keySet()) {
                        if(server == viewServerNum) continue;
                        if( viewServer.activeServersStatusMap.get(server)) {
                            ActivateServerRequest request = ActivateServerRequest.newBuilder().setServerName("S"+ server).setTestCase(transactionInputConfig.getSetNumber()).build();
                            viewServer.serversToActivateServersStub.get(server).activateServer(request);
                        }
                        else {
                            DeactivateServerRequest request = DeactivateServerRequest.newBuilder().setServerName("S"+ server).setTestCase(transactionInputConfig.getSetNumber()).build();
                            viewServer.serversToActivateServersStub.get(server).deactivateServer(request);
                        }
                    }


                    viewServer.commandLogger.log("---------------------------------------------------------------------------------");
                    viewServer.commandLogger.log("                               Test Case: " + transactionInputConfig.getSetNumber());
                    viewServer.commandLogger.log("---------------------------------------------------------------------------------");

                    viewServer.startTime = System.currentTimeMillis();
                    viewServer.endTime = System.currentTimeMillis();
                }



                System.out.println(Utils.toString(transactionInputConfig.getTransaction()) + " ContactServers: "
                        + tnxLine.clusterContactServermapping.values() + " Active Servers: " + transactionInputConfig.getServerNamesList()
                        + " Is Cross Shard: " + transactionInputConfig.getTransaction().getIsCrossShard());


                viewServer.transactions.put(transactionInputConfig.getTransaction().getTransactionNum(), transactionInputConfig.getTransaction());

                // Check whether transaction is IntraShard or Cross Shard
                viewServer.transactionsCount++;
                if( !transactionInputConfig.getTransaction().getIsCrossShard() ){
                    //Intra Shard.. can send to contact server from the shard.
                    int cluster = Utils.FindClusterOfDataItem(transactionInputConfig.getTransaction().getSender());
                    viewServer.sendTransactionToServer(transactionInputConfig, tnxLine.clusterContactServermapping.get(cluster));
                }
                else{
                    //Cross Shard.. need to send to both servers && wait for the Prepare Responses from both leaders

                    viewServer.sendCrossShardTransaction(transactionInputConfig,
                            tnxLine.clusterContactServermapping.get(Utils.FindClusterOfDataItem(transactionInputConfig.getTransaction().getSender())),
                            tnxLine.clusterContactServermapping.get(Utils.FindClusterOfDataItem(transactionInputConfig.getTransaction().getReceiver())));

                    //Thread.sleep(100);
                }





            }

            System.out.println("All Transactions Sent for final Test set. \nPress Enter to run all commands ");
            System.console().readLine();
            viewServer.sendCommandToServers( Command.PrintDB );
            viewServer.sendCommandToServers( Command.PrintLog );
            viewServer.sendCommandToServers( Command.PrintDataStore );
            viewServer.sendCommandToServers( Command.PrintBalance );
            viewServer.sendCommandToServers( Command.Performance );


        }
        else {
            System.out.println("File does not exist");
        }

    }

}
