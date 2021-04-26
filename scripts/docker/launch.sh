#!/bin/sh

# Use "exec" to run java -jar as PID 1
#
# Reduce start up time by pointing to non blocking random number generation
# JVM options to respect docker cgroup cpu/memory limits
#
# MaxRAMPercentage is documented here: https://bugs.java.com/view_bug.do?bug_id=JDK-8186248
# It has been introduced to allow the JVM to better manage memory when running within a container.

exec java \
    -Dcom.sun.management.jmxremote.local.only=false \
    -Dcom.sun.management.jmxremote.port=$JMX_PORT \
    -Dcom.sun.management.jmxremote.rmi.port=$JMX_PORT \
    -Dcom.sun.management.jmxremote.host=0.0.0.0 \
    -Dcom.sun.management.jmxremote.ssl=false \
    -Dcom.sun.management.jmxremote.authenticate=false \
    -Djava.security.egd=file:/dev/./urandom \
    -Djava.rmi.server.hostname=0.0.0.0 \
    -XX:+UseZGC \
    -XX:TieredStopAtLevel=1 \
    -javaagent:jmx_prometheus_javaagent.jar=$JMX_EXPORTER_PORT:config.yml \
    $JVM_OPTS \
    -jar $APPLICATION_JAR \
    $RUN_OPTS
