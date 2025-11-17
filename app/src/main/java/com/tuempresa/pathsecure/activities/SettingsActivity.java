package com.tuempresa.pathsecure.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions; // ¡Nueva importación!
import com.tuempresa.pathsecure.R;

// Importaciones de Clases Locales
import com.tuempresa.pathsecure.activities.ContactAdapter;
import com.tuempresa.pathsecure.activities.Contact;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Collections; // ¡Nueva importación!

public class SettingsActivity extends AppCompatActivity implements ContactAdapter.OnContactActionListener {

    private Button btnBackSettings, btnAddContact;
    private RecyclerView rvContacts;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ContactAdapter contactAdapter;
    private List<Contact> contactsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();

        // CORRECCIÓN CRÍTICA: Forzar verificación de autenticación al inicio
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Debe iniciar sesión para ver los ajustes.", Toast.LENGTH_LONG).show();
            // Asumiendo que MainActivity.class es la pantalla principal o login
            // Cámbiala si tu login/registro está en otra Activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Inicialización de Vistas
        btnBackSettings = findViewById(R.id.btnBackSettings);
        btnAddContact = findViewById(R.id.btnAddContact);
        rvContacts = findViewById(R.id.rvContacts);

        db = FirebaseFirestore.getInstance();

        // 1. Configurar RecyclerView y Adaptador
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        contactAdapter = new ContactAdapter(contactsList, this);
        rvContacts.setAdapter(contactAdapter);

        btnBackSettings.setOnClickListener(v -> finish());
        btnAddContact.setOnClickListener(v -> showAddContactDialog(null));

        loadContacts();
    }

    // --- Lógica CRUD con Firestore ---

    private void loadContacts() {
        // Obtenemos el ID de forma segura ya que verificamos en onCreate
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        List<Object> fetchedList = (List<Object>) documentSnapshot.get("emergency_contacts");

                        contactsList.clear();
                        if (fetchedList != null) {
                            for (Object obj : fetchedList) {
                                if (obj instanceof Map) {
                                    Map<String, Object> map = (Map<String, Object>) obj;
                                    Contact contact = new Contact();
                                    contact.setId((String) map.get("id"));
                                    contact.setName((String) map.get("name"));
                                    contact.setPhone((String) map.get("phone"));
                                    contactsList.add(contact);
                                }
                            }
                        }

                        contactAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error cargando contactos: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }


    private void saveContact(Contact contactToSave, boolean isNew) {
        String userId = mAuth.getCurrentUser().getUid(); // Seguro, verificado en onCreate

        if (isNew) {
            if (contactToSave.getId() == null) {
                contactToSave.setId(UUID.randomUUID().toString());
            }
            contactsList.add(contactToSave);
        } else {
            for (int i = 0; i < contactsList.size(); i++) {
                if (contactsList.get(i).getId().equals(contactToSave.getId())) {
                    contactsList.set(i, contactToSave);
                    break;
                }
            }
        }

        // CORRECCIÓN: Usar set con merge para crear el documento si no existe y evitar fallos de .update()
        db.collection("users").document(userId)
                .set(Collections.singletonMap("emergency_contacts", contactsList), SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Contacto guardado exitosamente.", Toast.LENGTH_SHORT).show();
                    contactAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    loadContacts(); // Intentar recargar si falla
                });
    }

    // --- Implementación de Interface (Acciones del Adaptador) ---

    @Override
    public void onDeleteClick(Contact contact) {
        String userId = mAuth.getCurrentUser().getUid(); // Seguro, verificado en onCreate

        contactsList.removeIf(c -> c.getId().equals(contact.getId()));

        // CORRECCIÓN: Usar set con merge para actualizar la lista
        db.collection("users").document(userId)
                .set(Collections.singletonMap("emergency_contacts", contactsList), SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, contact.getName() + " eliminado.", Toast.LENGTH_SHORT).show();
                    contactAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al eliminar contacto: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    loadContacts();
                });
    }

    @Override
    public void onItemClick(Contact contact) {
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
                    finalContact = contactToEdit;
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