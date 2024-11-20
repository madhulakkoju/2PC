
@echo off
REM Path to the JAR file (adjust as needed)
set JAR_PATH=C:\Users\mlakkoju\2pc-madhulakkoju\paxos-2pc\target\paxos-2pc-1.0-SNAPSHOT-shaded.jar

REM Launch Java programs in separate terminals with cmd.exe /K
start cmd.exe /K "java -jar %JAR_PATH% Main 1 9 3 3000"
start cmd.exe /K "java -jar %JAR_PATH% Main 2 9 3 3000"
start cmd.exe /K "java -jar %JAR_PATH% Main 3 9 3 3000"
start cmd.exe /K "java -jar %JAR_PATH% Main 4 9 3 3000"
start cmd.exe /K "java -jar %JAR_PATH% Main 5 9 3 3000"
start cmd.exe /K "java -jar %JAR_PATH% Main 6 9 3 3000"
start cmd.exe /K "java -jar %JAR_PATH% Main 7 9 3 3000"
start cmd.exe /K "java -jar %JAR_PATH% Main 8 9 3 3000"
start cmd.exe /K "java -jar %JAR_PATH% Main 9 9 3 3000"
start cmd.exe /K "java -jar %JAR_PATH% ViewServer 0 9 3 3000"

REM Exit the batch script (cmd.exe will remain open with each process running)
exit


