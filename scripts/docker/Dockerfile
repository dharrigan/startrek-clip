#
# Clojure Application Dockerfile
#
ARG JVM_TAG=openjdk-16-tools-deps-slim-buster
#
FROM clojure:${JVM_TAG}

#
# Add in curl as it is necessary to perform the healthcheck below
#
# Also add in a few more debugging tools that are *very* useful
# in determining if the container can ping/access a remote port.
#
RUN apt-get update \
    && apt-get install curl wget dnsutils telnet iputils-ping netcat less -y \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

#
# Add in the Prometheus JMX Exporter for remote monitoring/diagnostics
#
ARG JMX_EXPORTER_VERSION=0.15.0
#
RUN wget https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/$JMX_EXPORTER_VERSION/jmx_prometheus_javaagent-$JMX_EXPORTER_VERSION.jar -O /tmp/jmx_prometheus_javaagent.jar

# HTTP
EXPOSE 9053

# JMX EXPORTER
EXPOSE 9080

# JMX
EXPOSE 9010

# Call health status against the health endpoint
HEALTHCHECK --start-period=240s CMD curl -sS --fail http://localhost:9053/ping || exit 1

# Options that are passed to the JVM when it is launched
ENV JVM_OPTS ""

# JMX port
ENV JMX_PORT 9010

# JMX EXPORTER port
ENV JMX_EXPORTER_PORT 9080

# Options that are passed to the APP when it is launched
ENV RUN_OPTS ""

# JAR name (as named during the uberjar task)
ARG APPLICATION_JAR="app.jar"

# Export the name so that the launch script can pick it up
ENV APPLICATION_JAR $APPLICATION_JAR

# "Contain" the exec of launch.sh to user "clojure" This means that *if* someone
# was to gain access to the running container, and *if* the user was able to
# break out of the container, they would escape as the user "clojure" and thus on
# the host, they would have to perform *another* exploit to gain access as
# root.
RUN groupadd -r clojure && useradd --no-log-init -rg clojure clojure

#
# From this point onwards, become the clojure user for (mostly) all operations.
USER clojure

# Copy the launch script into place
COPY --chown=clojure:clojure scripts/docker/launch.sh launch.sh

# Copy the Uberjar into place
COPY --chown=clojure:clojure $APPLICATION_JAR $APPLICATION_JAR

# Copy the Prometheus JMX Exporter config into place
COPY --chown=clojure:clojure scripts/docker/jmxexporter/config.yml config.yml

# And awaayyy we go!
ENTRYPOINT ["sh", "./launch.sh"]
