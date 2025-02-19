#!/bin/bash

CONFIG_PATH="/minecraft/plugins/proxy/config.yml"

if [ -f "$CONFIG_PATH" ]; then
  echo "Found config.yml at $CONFIG_PATH"
else
  echo "Error: config.yml not found at $CONFIG_PATH"
  exit 1
fi

if [ -z "$SERVER_ID" ]; then
  echo "Error: SERVER_ID is not set!"
  exit 1
fi

sed -i "s/^  id: .*/  id: ${SERVER_ID}/" "$CONFIG_PATH"
echo "Updated server.id to ${SERVER_ID} in $CONFIG_PATH"

# Start Velocity proxy with custom memory settings
java -Xmx${MEMORY_MAX} -Xms${MEMORY_MAX} -XX:+UseG1GC \
  -XX:G1HeapRegionSize=4M -XX:+UnlockExperimentalVMOptions \
  -XX:+ParallelRefProcEnabled -XX:+AlwaysPreTouch \
  -XX:MaxInlineLevel=15 -jar proxy.jar
