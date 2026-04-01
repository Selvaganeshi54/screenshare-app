@echo off
REM -------------------------------------------------------------
REM Gradle start up script for Windows
REM -------------------------------------------------------------

setlocal
set DIRNAME=%~dp0
set APP_BASE_NAME=%~n0
set CLASSPATH=%DIRNAME%gradle\wrapper\gradle-wrapper.jar

if defined JAVA_HOME (
  set JAVA_EXE=%JAVA_HOME%\bin\java.exe
) else (
  set JAVA_EXE=java
)

"%JAVA_EXE%" -Dorg.gradle.appname=%APP_BASE_NAME% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
