package fr.thegreensuits.api.server.type;

public enum ServerType {
    PROXY("proxy"),
    HUB("hub"),
    SURVIVAL("survival");

    private final String type;

    ServerType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
