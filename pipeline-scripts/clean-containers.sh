#!/bin/bash

CONTAINER_NAME=$1

if [ -z "$CONTAINER_NAME" ]; then
  echo "Error: No container name provided."
  exit 1
fi

# Stop if running
if [ "$(docker ps -q -f name=$CONTAINER_NAME)" ]; then
  echo "Stopping running container: $CONTAINER_NAME"
  docker stop $CONTAINER_NAME
fi

# Remove if exists
if [ "$(docker ps -a -q -f name=$CONTAINER_NAME)" ]; then
  echo "Removing container: $CONTAINER_NAME"
  docker rm $CONTAINER_NAME
fi