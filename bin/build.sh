#!/bin/bash

clojure -Spom
clojure -A:uberjar
chmod +r startrek.jar
docker build -f scripts/docker/Dockerfile -t startrek .
