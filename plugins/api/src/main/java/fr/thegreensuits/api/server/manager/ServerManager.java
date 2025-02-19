package fr.thegreensuits.api.server.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import fr.thegreensuits.api.TheGreenSuits;
import fr.thegreensuits.api.redis.pubsub.Channels;
import fr.thegreensuits.api.redis.pubsub.listener.ServerCreatedListener;
import fr.thegreensuits.api.redis.pubsub.listener.ServerUpdatedListener;
import fr.thegreensuits.api.server.Server;
import fr.thegreensuits.api.server.status.ServerStatus;
import fr.thegreensuits.api.utils.helper.RedisHelper;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;
import org.slf4j.Logger;

public class ServerManager {
    private final TheGreenSuits thegreensuits;
    private final Logger logger;
    private final RedisHelper redisHelper;
    private final ExecutorService executorService;

    @Getter
    private final Map<String, Server> servers;

    public ServerManager(TheGreenSuits thegreensuits) {
        // - Initialize TheGreenSuits
        this.thegreensuits = thegreensuits;
        this.logger = thegreensuits.getLogger();
        this.redisHelper = new RedisHelper(this.thegreensuits.getJedisPool());
        this.executorService = thegreensuits.getExecutorService();

        // - Initialize servers
        this.servers = new HashMap<>();

        // - Load servers from redis
        this.init();
    }

    private void init() {
        this.logger.info("Loading servers from Redis");

        this.redisHelper.executeVoid(this::registerServers);

        this.logger.info("Loaded " + this.servers.size() + " servers from Redis");

        // - Register listeners
        this.logger.info("Registering Redis channel listeners");

        this.redisHelper.executeVoid(jedis -> {
            jedis.subscribe(new ServerCreatedListener(this), Channels.SERVERS_CREATED.getChannel());
            jedis.subscribe(new ServerUpdatedListener(this), Channels.SERVERS_UPDATED.getChannel());
            return null;
        });
    }

    private Void registerServers(Jedis jedis) {
        ScanParams scanParams = new ScanParams().match("server:*").count(1000);
        String cursor = "0";

        do {
            ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
            cursor = scanResult.getCursor();

            List<String> keys = scanResult.getResult();
            for (String key : keys) {
                String value = jedis.get(key);
                Server server = Server.deserialize(value, Server.class);

                this.servers.put(server.getId(), server);
            }
        } while (!cursor.equals("0"));
        return null;
    }

    public void addServer(Server server) {
        this.servers.put(server.getId(), server);
    }

    public void removeServer(Server server) {
        this.servers.remove(server.getId());
    }

    public void removeServer(String id) {
        this.servers.remove(id);
    }

    public boolean hasServer(String id) {
        return this.servers.containsKey(id);
    }

    public Server getServer(String id) {
        return this.servers.get(id);
    }

    public void updateServer(Server server) {
        this.servers.put(server.getId(), server);

        this.redisHelper.executeVoid(jedis -> {
            jedis.set("server:" + server.getId(), server.serialize());
            return null;
        });
    }

    public void updateServer(Server server, Boolean broadcastChanges) {
        this.updateServer(server);

        // - Broadcast server update to Redis channel
        if (broadcastChanges) {
            this.redisHelper.executeVoid(jedis -> {
                jedis.publish(Channels.SERVERS_UPDATED.getChannel(), server.serialize());
                return null;
            });
        }
    }

    public void updateServer(String id, ServerStatus status) {
        Server server = this.servers.get(id);
        server.setStatus(status);

        // - Save value
        this.updateServer(server);
    }

    public void updateServer(String id, ServerStatus status, Boolean broadcastChanges) {
        Server server = this.servers.get(id);
        server.setStatus(status);

        // - Save value
        this.updateServer(server, broadcastChanges);
    }
}
