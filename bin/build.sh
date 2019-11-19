#!/bin/bash

clojure -J-XX:-OmitStackTraceInFastThrow -J--illegal-access=deny -A:base:uberjar startrek.jar
chmod +r startrek.jar
docker build -f scripts/docker/Dockerfile -t startrek .
