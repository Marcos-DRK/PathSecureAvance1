package com.tuempresa.pathsecure.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Route {
    private String id;
    private String name;
    private long timestamp;

    private List<RoutePoint> points; // Asegúrate de importar RoutePoint

    // CORRECCIÓN: Constructor vacío (OBLIGATORIO) que inicializa solo la lista
    public Route() {
        this.points = new ArrayList<>();
        // No generar el ID ni el timestamp aquí.
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
        if (this.points == null) {
            this.points = new ArrayList<>();
        }
        this.points.add(new RoutePoint(lat, lon));
    }
}