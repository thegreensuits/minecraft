package fr.thegreensuits.api.server;

import java.net.InetSocketAddress;

import fr.thegreensuits.api.server.status.ServerStatus;
import fr.thegreensuits.api.server.type.ServerType;
import fr.thegreensuits.api.utils.Serializable;
import lombok.Getter;
import lombok.Setter;

public class Server extends Serializable {
    @Getter
    private final String id, address, port;

    @Getter
    private final ServerType type;

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

    public InetSocketAddress buildInetSocketAddress() {
        if (this.address == null || this.port == null) {
            return null;
        }

        return new InetSocketAddress(this.address, Integer.parseInt(this.port));
    }
}
