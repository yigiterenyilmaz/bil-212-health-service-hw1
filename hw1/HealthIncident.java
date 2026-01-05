public class HealthIncident {
    int timestamp;
    String disease;
    double latitude;
    double longitude;
    String location;
    double infectionRate;
    int populationAffected;
    double severity;
    String reportingAgency;
    Position<HealthIncident> positionInSeverityList;
    
    public HealthIncident(int timestamp, String disease, double latitude, double longitude,
                         String location, double infectionRate, int populationAffected,
                         double severity, String reportingAgency) {
        this.timestamp = timestamp;
        this.disease = disease;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = location;
        this.infectionRate = infectionRate;
        this.populationAffected = populationAffected;
        this.severity = severity;
        this.reportingAgency = reportingAgency;
        this.positionInSeverityList = null;
    }
    
    public String toString() {
        return "HealthIncident: " + disease + " at " + location + 
               " (Severity: " + severity + ", Time: " + timestamp + ")";
    }
}
