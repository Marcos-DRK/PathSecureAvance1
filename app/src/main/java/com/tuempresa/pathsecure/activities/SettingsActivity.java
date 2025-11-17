package com.tuempresa.pathsecure.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout; // Necesario para el Layout del diálogo
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tuempresa.pathsecure.R;

// IMPORTACIONES NUEVAS Y NECESARIAS
import com.tuempresa.pathsecure.adapters.ContactAdapter;
import com.tuempresa.pathsecure.models.Contact;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements ContactAdapter.OnContactActionListener {

    private Button btnBackSettings, btnAddContact;
    private RecyclerView rvContacts;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ContactAdapter contactAdapter;
    private List<Contact> contactsList = new ArrayList<>(); // Lista de objetos Contact

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Inicialización de Vistas
        btnBackSettings = findViewById(R.id.btnBackSettings);
        btnAddContact = findViewById(R.id.btnAddContact);
        rvContacts = findViewById(R.id.rvContacts);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // 1. Configurar RecyclerView y Adaptador
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        // Pasamos la lista y 'this' (SettingsActivity) como listener para manejar acciones
        contactAdapter = new ContactAdapter(contactsList, this);
        rvContacts.setAdapter(contactAdapter);

        btnBackSettings.setOnClickListener(v -> finish());
        btnAddContact.setOnClickListener(v -> showAddContactDialog(null));

        loadContacts();
    }

    // --- Lógica CRUD con Firestore ---

    private void loadContacts() {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Firestore lee la lista y la convierte a objetos Contact si es posible
                        List<Contact> fetchedContacts = (List<Contact>) documentSnapshot.get("emergency_contacts");
                        if (fetchedContacts != null) {
                            // Debemos iterar para asegurar que se deserializan correctamente
                            List<Contact> tempContacts = new ArrayList<>();
                            for (Object item : fetchedContacts) {
                                if (item instanceof Contact) {
                                    tempContacts.add((Contact) item);
                                } else if (item instanceof java.util.Map) {
                                    // Mapeo manual si Firestore lo devuelve como Map
                                    java.util.Map<String, String> map = (java.util.Map<String, String>) item;
                                    Contact contact = new Contact();
                                    contact.setId(map.get("id"));
                                    contact.setName(map.get("name"));
                                    contact.setPhone(map.get("phone"));
                                    tempContacts.add(contact);
                                }
                            }

                            contactsList.clear();
                            contactsList.addAll(tempContacts);
                            contactAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error cargando contactos.", Toast.LENGTH_SHORT).show());
    }

    private void saveContact(Contact contactToSave, boolean isNew) {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        if (isNew) {
            // Asignar un ID si es nuevo (aunque ya se asigna en el constructor)
            if (contactToSave.getId() == null) {
                contactToSave.setId(java.util.UUID.randomUUID().toString());
            }
            contactsList.add(contactToSave);
        } else {
            // Lógica de edición: encontrar y reemplazar
            for (int i = 0; i < contactsList.size(); i++) {
                if (contactsList.get(i).getId().equals(contactToSave.getId())) {
                    contactsList.set(i, contactToSave);
                    break;
                }
            }
        }

        // Guardar la lista completa de nuevo en Firestore
        db.collection("users").document(userId)
                .update("emergency_contacts", contactsList)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Contacto guardado.", Toast.LENGTH_SHORT).show();
                    contactAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar contacto.", Toast.LENGTH_SHORT).show();
                    loadContacts(); // Recargar para revertir cambios fallidos
                });
    }

    // --- Implementación de Interface (Acciones del Adaptador) ---

    @Override
    public void onDeleteClick(Contact contact) {
        // Implementación del borrado
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) return;

        contactsList.removeIf(c -> c.getId().equals(contact.getId()));

        db.collection("users").document(userId)
                .update("emergency_contacts", contactsList)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, contact.getName() + " eliminado.", Toast.LENGTH_SHORT).show();
                    contactAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al eliminar contacto.", Toast.LENGTH_SHORT).show();
                    loadContacts(); // Recargar para revertir
                });
    }

    @Override
    public void onItemClick(Contact contact) {
        // Implementación de la edición
        showAddContactDialog(contact);
    }

    // --- Diálogo de Creación/Edición ---

    private void showAddContactDialog(final Contact contactToEdit) {

        final boolean isNew = contactToEdit == null;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isNew ? "Añadir Contacto" : "Editar Contacto");

        final EditText inputName = new EditText(this);
        inputName.setHint("Nombre del Contacto");
        inputName.setText(isNew ? "" : contactToEdit.getName());

        final EditText inputPhone = new EditText(this);
        inputPhone.setHint("Número de Teléfono");
        inputPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        inputPhone.setText(isNew ? "" : contactToEdit.getPhone());

        // Usar LinearLayout para organizar los EditText en el diálogo
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        layout.addView(inputName);
        layout.addView(inputPhone);
        builder.setView(layout);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String name = inputName.getText().toString();
            String phone = inputPhone.getText().toString();

            if (!name.isEmpty() && !phone.isEmpty()) {
                Contact finalContact;
                if (isNew) {
                    finalContact = new Contact(name, phone);
                } else {
                    finalContact = contactToEdit; // Usar el objeto existente
                    finalContact.setName(name);
                    finalContact.setPhone(phone);
                }
                saveContact(finalContact, isNew);
            } else {
                Toast.makeText(this, "Nombre y teléfono son obligatorios.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}