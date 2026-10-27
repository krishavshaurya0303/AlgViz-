@echo off
REM Double-click to start AlgoViz on Windows. Needs Java 17+ (JDK) installed.
cd /d "%~dp0"
where java >nul 2>nul
if errorlevel 1 (
  echo Java is not installed. Install JDK 17 or newer from https://adoptium.net and run this again.
  pause
  exit /b 1
)
echo Starting AlgoViz... the first start downloads JavaFX and can take a few minutes.
call mvnw.cmd -q javafx:run
if errorlevel 1 pause
