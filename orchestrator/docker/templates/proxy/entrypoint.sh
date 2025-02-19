#!/bin/bash

sed -i "s/server.id=.*/server.id=${SERVER_ID}/" config.yml

# Start Velocity proxy with custom memory settings
java -Xmx${MEMORY_MAX} -Xms${MEMORY_MAX} -XX:+UseG1GC \
  -XX:G1HeapRegionSize=4M -XX:+UnlockExperimentalVMOptions \
  -XX:+ParallelRefProcEnabled -XX:+AlwaysPreTouch \
  -XX:MaxInlineLevel=15 -jar proxy.jar
