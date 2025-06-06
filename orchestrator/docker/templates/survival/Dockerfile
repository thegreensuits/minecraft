FROM openjdk:21-slim

# Environment variables
ENV MINECRAFT_VERSION=1.21.4
ENV PAPER_BUILD_VERSION=121

ENV MEMORY_MAX=8G

ENV PORT=25051-25100

# Create directories
RUN mkdir -p /minecraft/plugins /minecraft/config

# Download PaperMC
RUN apt-get update && apt-get install -y wget git \
  && wget https://api.papermc.io/v2/projects/paper/versions/1.21.4/builds/121/downloads/paper-1.21.4-121.jar -O /minecraft/server.jar \
  && apt-get clean && rm -rf /var/lib/apt/lists/*

# Compile and copy plugins
RUN git clone https://github.com/thegreensuits/minecraft.git /tmp/minecraft \
  && cd /tmp/minecraft \
  && ./gradlew plugins:core:shadowJar && ./gradlew plugins:servers:survival:shadowJar \
  && cp plugins/core/build/libs/core*.jar plugins/servers/survival/build/libs/survival*.jar /minecraft/plugins/

RUN echo "eula=true" > /minecraft/eula.txt

# Copy configuration files
COPY config.yml /minecraft/plugins/core/config.yml
COPY bukkit.yml /minecraft/bukkit.yml
COPY paper-global.yml /minecraft/config/paper-global.yml
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