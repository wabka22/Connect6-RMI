@echo off
cd /d "C:\Users\alexe\Desktop\Connect6-RMI"
chcp 65001 >nul
title Connect6 RMI Game Launcher

echo Starting Connect6 RMI Game...
echo.

echo Step 1: Compiling project...
call mvn clean compile -q
if %errorlevel% neq 0 (
    echo Compilation failed! Check your code.
    pause
    exit /b 1
)

echo.
echo Step 2: Starting RMI Server...
start "Connect6 Server" cmd /c "mvn exec:java -Dexec.mainClass=\"connect6.server.GameServer\""

echo Waiting 5 seconds for server to start...
timeout /t 5 /nobreak >nul

echo.
echo Step 3: Starting Player 1...
start "Connect6 - Player 1" cmd /c "mvn exec:java -Dexec.mainClass=\"connect6.client.GameClient\""

echo.
echo Step 4: Starting Player 2...
timeout /t 3 /nobreak >nul
start "Connect6 - Player 2" cmd /c "mvn exec:java -Dexec.mainClass=\"connect6.client.GameClient\""

echo.
echo ========================================
echo All components started!
echo - Server: Connect6 Server window
echo - Player 1: Connect6 - Player 1 window
echo - Player 2: Connect6 - Player 2 window
echo.
echo In each client window, click "Connect" button
echo ========================================
echo.
pause