@echo off
REM Open multiple tabs in ConEmu, each running a different Java program

REM Path to ConEmu executable (adjust as needed)
set CONEMU_PATH="C:\Program Files\ConEmu\ConEmu64.exe"

REM Path to your compiled Java classes (adjust as needed)
set JAVA_PATH=C:\Users\mlakkoju\2pc-madhulakkoju\paxos-2pc\target\classes

REM Launch ConEmu with multiple tabs running Java programs
start "" %CONEMU_PATH% -run {cmd} /B "java -cp %JAVA_PATH% Main 1 9 3 3000"
start "" %CONEMU_PATH% -run {cmd} /B "java -cp %JAVA_PATH% Main 2 9 3 3000"
start "" %CONEMU_PATH% -run {cmd} /B "java -cp %JAVA_PATH% Main 3 9 3 3000"
start "" %CONEMU_PATH% -run {cmd} /B "java -cp %JAVA_PATH% Main 4 9 3 3000"
start "" %CONEMU_PATH% -run {cmd} /B "java -cp %JAVA_PATH% Main 5 9 3 3000"
start "" %CONEMU_PATH% -run {cmd} /B "java -cp %JAVA_PATH% Main 6 9 3 3000"
start "" %CONEMU_PATH% -run {cmd} /B "java -cp %JAVA_PATH% Main 7 9 3 3000"
start "" %CONEMU_PATH% -run {cmd} /B "java -cp %JAVA_PATH% Main 8 9 3 3000"
start "" %CONEMU_PATH% -run {cmd} /B "java -cp %JAVA_PATH% Main 9 9 3 3000"
start "" %CONEMU_PATH% -run {cmd} /K "java -cp %JAVA_PATH% ViewServer 0 9 3 3000"

REM Exit the script (ConEmu will remain running with its open tabs)
exit
