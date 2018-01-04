package pl.asie.ynot.enums;

public enum OCNetworkMode {
    COMPONENT_AND_NETWORK,
    NETWORK_ONLY;

    @Override
    public String toString() {
        return name().toLowerCase().replaceAll("_", " ");
    }
}
