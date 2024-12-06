@ECHO off
CALL .\mvnw.cmd -B package -DskipTests
ECHO The application has been built
PAUSE
START .\run.bat
ECHO Please wait...
TIMEOUT /T 30
START http://localhost:8080/
PAUSE
