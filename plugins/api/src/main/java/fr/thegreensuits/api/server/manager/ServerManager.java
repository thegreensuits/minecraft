package fr.thegreensuits.api.server.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.thegreensuits.api.TheGreenSuits;
import fr.thegreensuits.api.server.Server;
import fr.thegreensuits.api.server.status.ServerStatus;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

public class ServerManager {
    private final TheGreenSuits thegreensuits;

    @Getter
    private final Map<String, Server> servers;

    public ServerManager(TheGreenSuits thegreensuits) {
        // - Initialize TheGreenSuits
        this.thegreensuits = thegreensuits;

        // - Initialize servers
        this.servers = new HashMap<>();

        // - Load servers from redis
        this.init();
    }

    private void init() {
        Jedis jedis = this.thegreensuits.getJedisPool().getResource();

        // - Load redis keys
        String cursor = "0";
        ScanParams params = new ScanParams().match("server:*").count(100);

        do {
            ScanResult<String> scanResult = jedis.scan(cursor, params);
            List<String> keys = scanResult.getResult();
            cursor = scanResult.getCursor();

            for (String key : keys) {
                String json = jedis.get(key);
                if (json != null) {
                    Server server = Server.deserialize(json, Server.class);
                    this.addServer(server);
                }
            }
        } while (!cursor.equals("0"));
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

    public Server getServer(String id) {
        return this.servers.get(id);
    }

    public void updateServer(Server server) {
        this.servers.put(server.getId(), server);
    }

    public void updateServer(String id, ServerStatus status) {
        Server server = this.servers.get(id);
        server.setStatus(status);
        this.servers.put(id, server);
    }
}
