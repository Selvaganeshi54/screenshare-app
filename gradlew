#!/bin/sh
# ------------------------------
# Gradle start up script for UN*X
# ------------------------------

if [ -z "$JAVA_HOME" ]; then
  JAVA_EXE=java
else
  JAVA_EXE="$JAVA_HOME/bin/java"
fi

CLASSPATH=$PWD/gradle/wrapper/gradle-wrapper.jar
exec "$JAVA_EXE" -Dorg.gradle.appname="$0" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
