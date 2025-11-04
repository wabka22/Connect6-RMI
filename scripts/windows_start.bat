@echo off
chcp 65001 >nul
cd /d "%~dp0..\target\classes"

start "Server" java connect6.server.GameServer

start "Player1" java connect6.client.GameClient

start "Player2" java connect6.client.GameClient

