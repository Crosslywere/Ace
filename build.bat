@ECHO off
CALL .\mvnw.cmd -B package -DskipTests
START .\run.bat
ECHO "Waiting for the application to start..."
@REM Waits for the specified amount of time as seconds
TIMEOUT /T 30
ECHO "Attempting to start application..."
START http://localhost:8080/
PAUSE
