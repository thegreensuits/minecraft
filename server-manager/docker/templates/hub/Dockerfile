FROM openjdk:21-slim

# Environment variables
ENV MINECRAFT_VERSION=1.21.4
ENV PAPER_BUILD_VERSION=121

ENV MEMORY_MAX=8G

ENV PORT=25001

# Create directories
RUN mkdir -p /minecraft/plugins /minecraft/config

# https://api.papermc.io/v2/projects/paper/versions/1.21.4/builds/121/downloads/paper-1.21.4-121.jar
# https://papermc.io/api/v2/projects/paper/versions/${MINECRAFT_VERSION}/builds/${PAPER_BUILD_VERSION}/downloads/paper-${MINECRAFT_VERSION}-${PAPER_BUILD_VERSION}.jar
# Download PaperMC
RUN apt-get update && apt-get install -y wget \
  && wget https://api.papermc.io/v2/projects/paper/versions/1.21.4/builds/121/downloads/paper-1.21.4-121.jar -O /minecraft/server.jar \
  && apt-get clean && rm -rf /var/lib/apt/lists/*

# Copy configuration files
COPY bukkit.yml /minecraft/bukkit.yml
COPY eula.txt /minecraft/eula.txt
COPY paper-global.yml /minecraft/config/paper-global.yml
COPY paper-world-defaults.yml /minecraft/config/paper-world-defaults.yml
COPY server.properties /minecraft/server.properties
COPY spigot.yml /minecraft/spigot.yml

# Set working directory
WORKDIR /minecraft

# Expose Minecraft port
EXPOSE ${PORT}

# Entrypoint script for flexible startup
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Default command
CMD ["/entrypoint.sh"]