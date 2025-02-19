package fr.thegreensuits.api.server;

import java.net.InetSocketAddress;

import fr.thegreensuits.api.server.status.ServerStatus;
import fr.thegreensuits.api.server.type.ServerType;
import fr.thegreensuits.api.utils.serialization.Serializable;
import lombok.Getter;
import lombok.Setter;

public class Server extends Serializable {
    @Getter
    private final String id, address, port;

    @Getter
    private final ServerType type;

    // - Orchestrator data
    @SuppressWarnings("unused")
    private String container_id;
    @SuppressWarnings("unused")
    private Integer replica;

    @Getter
    @Setter
    private ServerStatus status;

    public Server(String id, String address, String port, ServerType type, ServerStatus status) {
        this.id = id;
        this.address = address;
        this.port = port;
        this.type = type;
        this.status = status;
    }

    public Server(String id, String address, String port, ServerType type, ServerStatus status, String container_id,
            Integer replica) {
        this(id, address, port, type, status);
        this.container_id = container_id;
        this.replica = replica;
    }

    public InetSocketAddress buildInetSocketAddress() {
        if (this.address == null || this.port == null) {
            return null;
        }

        return new InetSocketAddress(this.address, Integer.parseInt(this.port));
    }
}
