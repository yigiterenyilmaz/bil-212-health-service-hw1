import java.io.*;
import java.util.Scanner;

public class HealthAlertNotification {
    
    private LinkedPositionalList<Watcher> watcherList;
    private LinkedQueue<HealthIncident> incidentQueue;
    private LinkedPositionalList<HealthIncident> severityOrderedList;
    private boolean allFlag;
    private static final int TIME_WINDOW = 6;
    
    public HealthAlertNotification(boolean allFlag) {
        this.watcherList = new LinkedPositionalList<>();
        this.incidentQueue = new LinkedQueue<>();
        this.severityOrderedList = new LinkedPositionalList<>();
        this.allFlag = allFlag;
    }
    
    public static void main(String[] args) {
        if (args.length < 2 || args.length > 3) {
            System.err.println("Usage: java HealthAlertNotification [--all] <watcherFile> <healthFile>");
            System.exit(1);
        }
        
        boolean allFlag = false;
        String watcherFile, healthFile;
        
        if (args.length == 3) {
            if (args[0].equals("--all")) {
                allFlag = true;
                watcherFile = args[1];
                healthFile = args[2];
            } else {
                System.err.println("Invalid flag. Use --all");
                System.exit(1);
                return;
            }
        } else {
            watcherFile = args[0];
            healthFile = args[1];
        }
        
        HealthAlertNotification system = new HealthAlertNotification(allFlag);
        system.run(watcherFile, healthFile);
    }
    
    public void run(String watcherFile, String healthFile) {
        try {
            Scanner watcherScanner = new Scanner(new File(watcherFile));
            Scanner healthScanner = new Scanner(new File(healthFile));
            
            WatcherEvent nextWatcherEvent = readNextWatcherEvent(watcherScanner);
            HealthIncident nextHealthIncident = readNextHealthIncident(healthScanner);
            
            int currentTime = 0;
            
            while (nextWatcherEvent != null || nextHealthIncident != null) {
                int nextTime = Integer.MAX_VALUE;
                
                if (nextWatcherEvent != null) {
                    nextTime = Math.min(nextTime, nextWatcherEvent.timestamp);
                }
                if (nextHealthIncident != null) {
                    nextTime = Math.min(nextTime, nextHealthIncident.timestamp);
                }
                
                currentTime = nextTime;
                
                removeOldIncidents(currentTime);
                
                while (nextWatcherEvent != null && nextWatcherEvent.timestamp == currentTime) {
                    processWatcherEvent(nextWatcherEvent);
                    nextWatcherEvent = readNextWatcherEvent(watcherScanner);
                }
                
                while (nextHealthIncident != null && nextHealthIncident.timestamp == currentTime) {
                    processHealthIncident(nextHealthIncident);
                    nextHealthIncident = readNextHealthIncident(healthScanner);
                }
            }
            
            watcherScanner.close();
            healthScanner.close();
            
        } catch (FileNotFoundException e) {
            System.err.println("File not found " + e.getMessage());
            System.exit(1);
        }
    }
    
    private WatcherEvent readNextWatcherEvent(Scanner scanner) {
        if (!scanner.hasNextLine()) {
            return null;
        }
        
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) {
            return readNextWatcherEvent(scanner);
        }
        
        String[] tokens = line.split("\\s+");
        int timestamp = Integer.parseInt(tokens[0]);
        String command = tokens[1];
        
        return new WatcherEvent(timestamp, command, tokens);
    }
    
    private HealthIncident readNextHealthIncident(Scanner scanner) {
        if (!scanner.hasNextLine()) {
            return null;
        }
        
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) {
            return readNextHealthIncident(scanner);
        }
        
        String[] tokens = line.split("\\s+");
        
        int timestamp = Integer.parseInt(tokens[0]);
        String disease = tokens[1];
        double latitude = Double.parseDouble(tokens[2]);
        double longitude = Double.parseDouble(tokens[3]);
        String location = tokens[4];
        double infectionRate = Double.parseDouble(tokens[5]);
        int populationAffected = Integer.parseInt(tokens[6]);
        double severity = Double.parseDouble(tokens[7]);
        String reportingAgency = tokens[8];
        
        return new HealthIncident(timestamp, disease, latitude, longitude, location,
                                  infectionRate, populationAffected, severity, reportingAgency);
    }
    
    private void removeOldIncidents(int currentTime) {
        while (!incidentQueue.isEmpty()) {
            HealthIncident oldest = incidentQueue.first();
            if (currentTime - oldest.timestamp > TIME_WINDOW) {
                incidentQueue.dequeue();
                if (oldest.positionInSeverityList != null) {
                    severityOrderedList.remove(oldest.positionInSeverityList);
                }
            } else {
                break;
            }
        }
    }
    
    private void processWatcherEvent(WatcherEvent event) {
        if (event.command.equals("add")) {
            addWatcher(event);
        } else if (event.command.equals("delete")) {
            deleteWatcher(event);
        } else if (event.command.equals("query-highest")) {
            queryHighest();
        } else if (event.command.equals("query-disease")) {
            queryDisease(event);
        } else if (event.command.equals("query-region")) {
            queryRegion(event);
        }
    }
    
    private void addWatcher(WatcherEvent event) {
        double latitude = Double.parseDouble(event.tokens[2]);
        double longitude = Double.parseDouble(event.tokens[3]);
        String name = event.tokens[4];
        
        Watcher watcher = new Watcher(name, latitude, longitude);
        watcherList.addLast(watcher);
        
        System.out.println(name + " is added to the watcherlist");
    }
    
    private void deleteWatcher(WatcherEvent event) {
        String name = event.tokens[2];
        
        Position<Watcher> pos = watcherList.first();
        while (pos != null) {
            if (pos.getElement().name.equals(name)) {
                watcherList.remove(pos);
                System.out.println(name + " is removed from the watcher-list");
                return;
            }
            pos = watcherList.after(pos);
        }
    }
    
    private void queryHighest() {
        System.out.println("Most severe health incident in past 6 hours:");
        
        if (severityOrderedList.isEmpty()) {
            System.out.println("No records");
        } else {
            HealthIncident mostSevere = severityOrderedList.first().getElement();
            System.out.println("(Disease: " + mostSevere.disease + ") Severity: " + 
                             mostSevere.severity + " at " + mostSevere.location);
        }
    }
    
    private void queryDisease(WatcherEvent event) {
        String disease = event.tokens[2];
        
        Position<HealthIncident> pos = severityOrderedList.first();
        while (pos != null) {
            HealthIncident incident = pos.getElement();
            if (incident.disease.equals(disease)) {
                System.out.println("(Disease: " + incident.disease + ") Severity: " + 
                                 incident.severity + " at " + incident.location);
            }
            pos = severityOrderedList.after(pos);
        }
    }
    
    private void queryRegion(WatcherEvent event) {
        double latitude = Double.parseDouble(event.tokens[2]);
        double longitude = Double.parseDouble(event.tokens[3]);
        double radius = Double.parseDouble(event.tokens[4]);
        
        Position<HealthIncident> pos = severityOrderedList.first();
        while (pos != null) {
            HealthIncident incident = pos.getElement();
            double distance = calculateDistance(latitude, longitude, 
                                              incident.latitude, incident.longitude);
            if (distance <= radius) {
                System.out.println("(Disease: " + incident.disease + ") Severity: " + 
                                 incident.severity + " at " + incident.location);
            }
            pos = severityOrderedList.after(pos);
        }
    }
    
    private void processHealthIncident(HealthIncident incident) {
        incidentQueue.enqueue(incident);
        
        insertIntoSeverityList(incident);
        
        if (allFlag) {
            System.out.println("(Disease: " + incident.disease + ") at " + 
                             incident.location + " is inserted into incident-queue");
        }
        
        notifyWatchers(incident);
    }
    
    private void insertIntoSeverityList(HealthIncident incident) {
        if (severityOrderedList.isEmpty()) {
            incident.positionInSeverityList = severityOrderedList.addFirst(incident);
            return;
        }
        
        Position<HealthIncident> pos = severityOrderedList.first();
        while (pos != null) {
            if (incident.severity >= pos.getElement().severity) {
                incident.positionInSeverityList = severityOrderedList.addBefore(pos, incident);
                return;
            }
            pos = severityOrderedList.after(pos);
        }
        
        incident.positionInSeverityList = severityOrderedList.addLast(incident);
    }
    
    private void notifyWatchers(HealthIncident incident) {
        Position<Watcher> pos = watcherList.first();
        while (pos != null) {
            Watcher watcher = pos.getElement();
            double distance = calculateDistance(watcher.latitude, watcher.longitude,
                                              incident.latitude, incident.longitude);
            
            if (distance < 2 * incident.severity) {
                System.out.println("(Disease: " + incident.disease + ") at " + 
                                 incident.location + " is close to " + watcher.name);
            }
            
            pos = watcherList.after(pos);
        }
    }
    
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        return Math.sqrt(Math.pow(lat1 - lat2, 2) + Math.pow(lon1 - lon2, 2));
    }
    
    private static class WatcherEvent {
        int timestamp;
        String command;
        String[] tokens;
        
        WatcherEvent(int timestamp, String command, String[] tokens) {
            this.timestamp = timestamp;
            this.command = command;
            this.tokens = tokens;
        }
    }
}