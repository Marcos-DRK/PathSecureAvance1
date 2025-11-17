package com.tuempresa.pathsecure.activities;

// ¡CORRECCIÓN! Debe ser PUBLIC y NO debe llevar STATIC
public class RoutePoint {
    private double lat;
    private double lon;

    // OBLIGATORIO: Constructor vacío
    public RoutePoint() {}

    public RoutePoint(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    // Getters y Setters públicos
    public double getLat() { return lat; }
    public double getLon() { return lon; }

    public void setLat(double lat) { this.lat = lat; }
    public void setLon(double lon) { this.lon = lon; }
}