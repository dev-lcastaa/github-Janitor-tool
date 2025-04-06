#!/bin/bash

CONTAINER_NAME=$1

# If no name is provided, exit with error
if [ -z "$CONTAINER_NAME" ]; then
  echo "Error: No container name provided."
  exit 1
fi

# Check if *any* containers exist at all
ALL_CONTAINERS=$(docker container ls -a -q)
if [ -z "$ALL_CONTAINERS" ]; then
  echo "No containers exist on the system. Skipping cleanup."
  exit 0
fi

# Stop if container is running
if [ "$(docker ps -q -f name=$CONTAINER_NAME)" ]; then
  echo "Stopping running container: $CONTAINER_NAME"
  docker stop $CONTAINER_NAME
fi

# Remove if container exists (stopped or running)
if [ "$(docker ps -a -q -f name=$CONTAINER_NAME)" ]; then
  echo "Removing container: $CONTAINER_NAME"
  docker rm $CONTAINER_NAME
fi

echo "✅ Cleanup complete for container: $CONTAINER_NAME"
exit 0