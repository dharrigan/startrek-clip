ARG JVM_TAG=11.0.4
FROM openjdk:${JVM_TAG}

#
# Add in curl as it is necessary to perform the healthcheck below
#
# Also add in a fe more debugging tools that are *very* useful
# in determining if the container can ping/access a remote port.
#
RUN apt-get update \
    && apt-get install curl wget dnsutils telnet iputils-ping netcat less -y \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# expose port for spring boot actuators and any api
EXPOSE 8080

# call health status against spring boot actuator endpoint
HEALTHCHECK --start-period=240s CMD curl -sS --fail http://localhost:8080/api/ping || exit 1

# options that are passed to the JVM when it is launched
ENV JAVA_OPTS ""

# options that are passed to the APP when it is launched
ENV RUN_OPTS ""

# default app name
ENV APPLICATION_JAR "app.jar"

# "Contain" the exec of launch.sh to user "java" This means that *if* someone
# was to gain access to the running container, and *if* the user was able to
# break out of the container, they would escape as the user "java" and thus on
# the host, they would have to perform *another* exploit to gain access as
# root.
RUN groupadd -r java && useradd --no-log-init -r -g java java
USER java

# copy the launch script into place
COPY --chown=java:java scripts/docker/launch.sh launch.sh

# copy the UBERJAR to the expected location in the Docker image
COPY --chown=java:java startrek.jar app.jar

ENTRYPOINT ["sh", "./launch.sh", "startrek.core"]
