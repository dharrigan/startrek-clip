#!/bin/sh

# Use "exec" to run java -jar as PID 1
#
# Reduce start up time by pointing to non blocking random number generation
# JVM options to respect docker cgroup cpu/memory limits
#
# MaxRAMPercentage is documented here: https://bugs.java.com/view_bug.do?bug_id=JDK-8186248
# It has been introduced to allow the JVM to better manage memory when running within a container.

exec java \
    -noverify \
    -Djava.security.egd=file:/dev/./urandom \
    -XX:+UnlockExperimentalVMOptions \
    -XX:MaxRAMPercentage=80.0 \
    -XX:TieredStopAtLevel=1 \
    $JVM_OPTS \
    -jar $APPLICATION_JAR \
    $RUN_OPTS
