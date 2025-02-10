FROM gradle:jdk22

# Environment variables
ENV VELOCITY_VERSION=3.4.0-SNAPSHOT
ENV VELOCITY_BUILD_NUMBER=462

ENV MEMORY_MAX=2G
ENV PORT=25565-25600

# Create directories
RUN mkdir -p /proxy/plugins

# Download Velocity
RUN apt-get update && apt-get install -y wget git \
  && wget https://api.papermc.io/v2/projects/velocity/versions/${VELOCITY_VERSION}/builds/${VELOCITY_BUILD_NUMBER}/downloads/velocity-${VELOCITY_VERSION}-${VELOCITY_BUILD_NUMBER}.jar -O /proxy/proxy.jar \
  && apt-get clean && rm -rf /var/lib/apt/lists/*

# Compile and copy plugins
RUN git clone https://github.com/thegreensuits/minecraft.git /tmp/minecraft \
  && cd /tmp/minecraft \
  && ./gradlew plugins:proxy:shadowJar \
  && cp plugins/proxy/build/libs/proxy*.jar /proxy/plugins/

# Copy configuration files
COPY velocity.toml /proxy/velocity.toml
COPY forwarding.secret /proxy/forwarding.secret

# Set working directory
WORKDIR /proxy

# Expose Proxy port
EXPOSE ${PORT}

# Entrypoint script for flexible startup
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Default command
CMD ["/entrypoint.sh"]