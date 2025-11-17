package com.tuempresa.pathsecure.activities; // ¡Paquete cambiado!

import java.util.UUID;

public class Contact {
    private String id;
    private String name;
    private String phone;

    // Constructor vacío requerido por Firestore
    public Contact() {
        this.id = UUID.randomUUID().toString();
    }

    public Contact(String name, String phone) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.phone = phone;
    }

    // --- Getters ---
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    // --- Setters ---
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}