public class Watcher {
    String name;
    double latitude;
    double longitude;
    
    public Watcher(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    public String toString() {
        return "Watcher: " + name + " at (" + latitude + ", " + longitude + ")";
    }
}
