package com.tuempresa.pathsecure.activities;

import java.util.UUID;

public class Contact {
    private String id;
    private String name;
    private String phone;

    // CORRECCIÓN CLAVE: Constructor vacío (OBLIGATORIO) que no debe generar ID
    public Contact() {
    }


    public Contact(String name, String phone) {
        // La generación de ID ocurre aquí (al crear un nuevo objeto)
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