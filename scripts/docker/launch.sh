#!/bin/sh

# use "exec" to run java -jar as PID 1
# reduce start up time by pointing to non blocking random number generation
# JVM options to respect docker cgroup cpu/memory limits
# accepts command line java options (JAVA_OPTS) as an environment variable
# accepts command line Spring Boot app runtime options (RUN_OPTS) as an environment variable
#
# MaxRAMPercentage is documented here: https://bugs.java.com/view_bug.do?bug_id=JDK-8186248
# It has been introduced to allow the JVM to better manage memory when running within a container.
#
# For "noverify", "-XX:TieredStopAtLevel=1" and "--spring.jmx.enabled=false", see
# https://spring.io/blog/2018/11/08/spring-boot-in-a-container
#
exec java \
    -noverify \
    -Djava.security.egd=file:/dev/./urandom \
    -XX:+UnlockExperimentalVMOptions \
    -XX:MaxRAMPercentage=80.0 \
    -XX:TieredStopAtLevel=1 \
    $JAVA_OPTS \
    -cp $APPLICATION_JAR \
    clojure.main \
    -m \
    $1 \
    $RUN_OPTS
