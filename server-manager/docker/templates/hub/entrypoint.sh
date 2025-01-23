#!/bin/bash

# Download custom plugins (replace with your actual plugin URLs)
#RUN wget https://example.com/plugins/essentialsx.jar -O /minecraft/plugins/EssentialsX.jar \
#  && wget https://example.com/plugins/worldedit.jar -O /minecraft/plugins/WorldEdit.jar \
#  && wget https://example.com/plugins/placeholderapi.jar -O /minecraft/plugins/PlaceholderAPI.jar

# TODO: Get the env var sent by orchestrator for the attached port and edit the server.properties file

# Start Velocity proxy with custom memory settings
java -Xmx${MEMORY_MAX} -Xms${MEMORY_MAX} -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions \
  -XX:MaxGCPauseMillis=50 -XX:+ParallelRefProcEnabled -XX:+AlwaysPreTouch \
  -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M \
  -XX:G1ReservePercent=20 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 \
  -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 \
  -Dpaper.playerconnection.keepalive=120 -Dfile.encoding=UTF-8 -Djline.terminal=jline.UnsupportedTerminal \
  -Daikars.new.flags=true -jar server.jar nogui
