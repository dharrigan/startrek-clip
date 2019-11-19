#!/bin/bash

docker-compose -f scripts/docker/docker-compose-${1:-local}.yml up
