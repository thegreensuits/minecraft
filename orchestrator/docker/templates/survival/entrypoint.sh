#!/bin/bash

CONFIG_PATH="/minecraft/plugins/core/config.yml"

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

sed -i "s/^server.id: .*/server.id: ${SERVER_ID}/" "$CONFIG_PATH"
echo "Updated server.id to ${SERVER_ID} in $CONFIG_PATH"

# Start Paper server with custom memory settings
java -Xmx${MEMORY_MAX} -Xms${MEMORY_MAX} -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions \
  -XX:MaxGCPauseMillis=50 -XX:+ParallelRefProcEnabled -XX:+AlwaysPreTouch \
  -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M \
  -XX:G1ReservePercent=20 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 \
  -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 \
  -Dpaper.playerconnection.keepalive=120 -Dfile.encoding=UTF-8 -Djline.terminal=jline.UnsupportedTerminal \
  -Daikars.new.flags=true -jar server.jar nogui
