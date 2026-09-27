@REM Maven Wrapper startup script for Windows
@echo off
setlocal
set "MVNW_CACHE=%~dp0.mvn\wrapper\dists\apache-maven-3.9.9-bin"
set "MVNW_ZIP=%MVNW_CACHE%\apache-maven-3.9.9-bin.zip"
set "MVNW_HOME=%MVNW_CACHE%\apache-maven-3.9.9"
if not exist "%MVNW_HOME%\bin\mvn.cmd" (
  if not exist "%MVNW_CACHE%" mkdir "%MVNW_CACHE%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip' -OutFile '%MVNW_ZIP%'; Expand-Archive -Force '%MVNW_ZIP%' '%MVNW_CACHE%'"
  if errorlevel 1 exit /b 1
)
call "%MVNW_HOME%\bin\mvn.cmd" %*
endlocal
