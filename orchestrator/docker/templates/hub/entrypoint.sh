#!/bin/bash

sed -i "s/server.id=.*/server.id=${SERVER_ID}/" config.yml

# Start Paper server with custom memory settings
java -Xmx${MEMORY_MAX} -Xms${MEMORY_MAX} -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions \
  -XX:MaxGCPauseMillis=50 -XX:+ParallelRefProcEnabled -XX:+AlwaysPreTouch \
  -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M \
  -XX:G1ReservePercent=20 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 \
  -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 \
  -Dpaper.playerconnection.keepalive=120 -Dfile.encoding=UTF-8 -Djline.terminal=jline.UnsupportedTerminal \
  -Daikars.new.flags=true -jar server.jar nogui
