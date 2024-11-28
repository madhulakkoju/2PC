
REM ########  Bonus 2 - Configurable Clusters


@echo off
REM Define variables
set TOTAL_SERVERS=9

REM Number of Servers in each Cluster
set CLUSTER_SIZE=3

REM Total Data Items over all Clusters
set TOTAL_DATA_ITEMS=3000

REM Path to the JAR file
set JAR_PATH=C:\Users\mlakkoju\2pc-madhulakkoju\paxos-2pc\target\paxos-2pc-1.0-SNAPSHOT-shaded.jar

REM Loop through server numbers 1 to TOTAL_SERVERS
for /L %%i in (1, 1, %TOTAL_SERVERS%) do (
    REM Launch each Java program with the appropriate arguments
    start cmd.exe /K "java -jar %JAR_PATH% Main %%i %TOTAL_SERVERS% %CLUSTER_SIZE% %TOTAL_DATA_ITEMS%"
)

REM Launch the ViewServer with the given parameters
start cmd.exe /K "java -jar %JAR_PATH% ViewServer 0 %TOTAL_SERVERS% %CLUSTER_SIZE% %TOTAL_DATA_ITEMS%"

REM Exit the batch script (cmd.exe will remain open with each process running)
exit
