package com.tuempresa.pathsecure.activities; // ¡Paquete cambiado!

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Clase anidada para almacenar las coordenadas
class RoutePoint {
    private double lat;
    private double lon;

    public RoutePoint() {}

    public RoutePoint(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() { return lat; }
    public double getLon() { return lon; }

    public void setLat(double lat) { this.lat = lat; }
    public void setLon(double lon) { this.lon = lon; }
}

public class Route {
    private String id;
    private String name;
    private long timestamp;
    private List<RoutePoint> points;

    public Route() {
        this.id = UUID.randomUUID().toString();
        this.points = new ArrayList<>();
        this.timestamp = System.currentTimeMillis();
    }

    public Route(String name, List<RoutePoint> points) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.points = points;
        this.timestamp = System.currentTimeMillis();
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getName() { return name; }
    public long getTimestamp() { return timestamp; }
    public List<RoutePoint> getPoints() { return points; }

    // --- Setters ---
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setPoints(List<RoutePoint> points) { this.points = points; }

    public void addPoint(double lat, double lon) {
        this.points.add(new RoutePoint(lat, lon));
    }
}