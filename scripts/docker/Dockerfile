#
# Clojure Application Dockerfile
#
ARG JVM_TAG=openjdk-11-tools-deps-slim-buster
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

# HTTP
EXPOSE 8080

# Call health status against the health endpoint
HEALTHCHECK --start-period=240s CMD curl -sS --fail http://localhost:8080/api/ping || exit 1

# Options that are passed to the JVM when it is launched
ENV JVM_OPTS ""

# Options that are passed to the APP when it is launched
ENV RUN_OPTS ""

# JAR name (as named during the uberjar task)
ARG APPLICATION_JAR="startrek.jar"

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

# And awaayyy we go!
ENTRYPOINT ["sh", "./launch.sh"]
