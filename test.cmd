@echo off
setlocal
cd /d "%~dp0"
if not exist build\test-classes mkdir build\test-classes
javac --release 17 -encoding UTF-8 -Xlint:all -d build\test-classes src\wallet\*.java tests\wallet\*.java
if errorlevel 1 exit /b 1
java -Djava.awt.headless=true -cp build\test-classes wallet.WalletSmokeTest %*
