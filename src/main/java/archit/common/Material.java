package archit.common;

public record Material(String namespace, String id) {
    public static final String DEFAULT_NAMESPACE = "minecraft";

    public Material(String id) {
        this(DEFAULT_NAMESPACE, id);
    }
    
    @Override
    public String toString() {
        return namespace + ":" + id;
    }
}
