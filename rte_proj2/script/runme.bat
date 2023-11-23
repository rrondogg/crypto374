@echo off
cd ..\
cd src
echo Welcome to the chatroom!
echo.
echo Do you want to start a server or connect as a user? Type S for server or U for user and press Enter...
set /p answer=

if /I "%answer%"=="S" (
    echo Starting the server...
    java rte_TCPServer.java
) else if /I "%answer%"=="U" (
    echo Enter your name:
) else (
    echo Invalid choice. Please choose 'S' or 'U'.
)

set /p tempusername=
echo Welcome %tempusername%! Connecting you now...
java rte_TCPClient.java -u %tempusername%
pause