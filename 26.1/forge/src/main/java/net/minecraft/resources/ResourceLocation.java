package net.minecraft.resources;

public class ResourceLocation {
    private final String namespace;
    private final String path;

    public ResourceLocation(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }

    public static ResourceLocation fromNamespaceAndPath(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    public static ResourceLocation tryParse(String location) {
        if (location == null) return null;
        int colon = location.indexOf(':');
        if (colon < 0) {
            return new ResourceLocation("minecraft", location);
        }
        return new ResourceLocation(location.substring(0, colon), location.substring(colon + 1));
    }

    public String getNamespace() {
        return namespace;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ResourceLocation other)) return false;
        return namespace.equals(other.namespace) && path.equals(other.path);
    }

    @Override
    public int hashCode() {
        return 31 * namespace.hashCode() + path.hashCode();
    }
}
