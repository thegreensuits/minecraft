FROM openjdk:21-slim

# Environment variables
ENV MINECRAFT_VERSION=1.21.4
ENV PAPER_BUILD_VERSION=121

ENV MEMORY_MAX=8G

ENV PORT=25101-25150

# Create directories
RUN mkdir -p /minecraft/plugins /minecraft/config

# Download PaperMC
RUN apt-get update && apt-get install -y wget git \
  && wget https://api.papermc.io/v2/projects/paper/versions/1.21.4/builds/121/downloads/paper-1.21.4-121.jar -O /minecraft/server.jar \
  && apt-get clean && rm -rf /var/lib/apt/lists/*

# Compile and copy plugins
RUN git clone https://github.com/thegreensuits/minecraft.git /tmp/minecraft \
  && cd /tmp/minecraft \
  && ./gradlew shadowJar \
  && cp core/build/libs/core*.jar /minecraft/plugins/

RUN echo "eula=true" > /minecraft/eula.txt

# Copy configuration files
COPY bukkit.yml /minecraft/bukkit.yml
COPY paper-global.yml /minecraft/config/paper-global.yml
COPY server.properties /minecraft/server.properties
COPY spigot.yml /minecraft/spigot.yml

# Remove world player-specific data if exists
RUN rm -rf \
  /minecraft/world/advancements \
  /minecraft/world/playerdata \
  /minecraft/world/stats \
  /minecraft/world/session.lock \
  /minecraft/world/entities \
  /minecraft/world/region

# Set working directory
WORKDIR /minecraft

# Expose Minecraft port
EXPOSE ${PORT}

# Entrypoint script for flexible startup
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Default command
CMD ["/entrypoint.sh"]