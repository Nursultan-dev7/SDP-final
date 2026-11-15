@echo off
setlocal
cd /d "%~dp0"
if not exist build\classes mkdir build\classes
javac --release 17 -encoding UTF-8 -d build\classes src\wallet\*.java
if errorlevel 1 exit /b 1
jar --create --file build\BlockchainWallet.jar --main-class wallet.WalletApp -C build\classes .
if errorlevel 1 exit /b 1
java -jar build\BlockchainWallet.jar
