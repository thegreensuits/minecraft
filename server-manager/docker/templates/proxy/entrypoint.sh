#!/bin/bash

# Download custom plugins (replace with your actual plugin URLs)
#RUN wget https://example.com/plugins/essentialsx.jar -O /minecraft/plugins/EssentialsX.jar \
#  && wget https://example.com/plugins/worldedit.jar -O /minecraft/plugins/WorldEdit.jar \
#  && wget https://example.com/plugins/placeholderapi.jar -O /minecraft/plugins/PlaceholderAPI.jar

# Start Velocity proxy with custom memory settings
java -Xmx${MEMORY_MAX} -Xms${MEMORY_MAX} -XX:+UseG1GC \
  -XX:G1HeapRegionSize=4M -XX:+UnlockExperimentalVMOptions \
  -XX:+ParallelRefProcEnabled -XX:+AlwaysPreTouch \
  -XX:MaxInlineLevel=15 -jar proxy.jar
