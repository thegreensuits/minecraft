package fr.thegreensuits.api.server.status;

public enum ServerStatus {
    IDLE("idle"),
    STARTING("starting"),
    RUNNING("running"),
    STOPPING("stopping"),
    STOPPED("stopped");

    private final String status;

    ServerStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
