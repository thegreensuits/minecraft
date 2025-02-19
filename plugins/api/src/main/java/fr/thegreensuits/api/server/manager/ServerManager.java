package fr.thegreensuits.api.server.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.thegreensuits.api.TheGreenSuits;
import fr.thegreensuits.api.redis.pubsub.Channels;
import fr.thegreensuits.api.redis.pubsub.listener.ServerCreatedListener;
import fr.thegreensuits.api.redis.pubsub.listener.ServerUpdatedListener;
import fr.thegreensuits.api.server.Server;
import fr.thegreensuits.api.server.status.ServerStatus;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

public class ServerManager {
    private final TheGreenSuits thegreensuits;
    private final Jedis jedis;

    @Getter
    private final Map<String, Server> servers;

    public ServerManager(TheGreenSuits thegreensuits) {
        // - Initialize TheGreenSuits
        this.thegreensuits = thegreensuits;
        this.jedis = thegreensuits.getJedisPool().getResource();

        // - Initialize servers
        this.servers = new HashMap<>();

        // - Load servers from redis
        this.init();
    }

    private void init() {
        String cursor = "0";
        ScanParams params = new ScanParams().match("server:*").count(100);

        do {
            ScanResult<String> scanResult = this.jedis.scan(cursor, params);
            List<String> keys = scanResult.getResult();
            cursor = scanResult.getCursor();

            for (String key : keys) {
                String json = this.jedis.get(key);
                if (json != null) {
                    Server server = Server.deserialize(json, Server.class);
                    this.addServer(server);
                }
            }
        } while (!cursor.equals("0"));

        jedis.subscribe(new ServerCreatedListener(), Channels.SERVERS_CREATED.getChannel());
        jedis.subscribe(new ServerUpdatedListener(), Channels.SERVERS_UPDATED.getChannel());

        this.jedis.close();
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

        Jedis jedis = this.thegreensuits.getJedisPool().getResource();

        jedis.set("server:" + server.getId(), server.serialize());
        jedis.close();
    }

    public void updateServer(Server server, Boolean broadcastChanges) {
        this.updateServer(server);

        // - Broadcast server update to Redis channel
        if (broadcastChanges) {
            this.jedis.publish(Channels.SERVERS_UPDATED.getChannel(), server.serialize());
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
