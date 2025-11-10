package com.tuempresa.pathsecure.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tuempresa.pathsecure.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmailReg, etPassReg;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmailReg = findViewById(R.id.etEmailReg);
        etPassReg = findViewById(R.id.etPassReg);
        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);
        Button btnBack = findViewById(R.id.btnBack);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnCreateAccount.setOnClickListener(v -> registrarUsuario());
        btnBack.setOnClickListener(v -> finish());
    }
   /* String txtLocationStr = HomeActivity.ultimaUbicacion;
    private String userId;
    private String Tipo;
    private String timestamp;

    String mensaje = "Alerta PathSecure\n" +
            "Usuario:" + userId + "\n" +
            "Tipo:" + Tipo + "\n" +
            "fecha:" + timestamp + "\n"+
            "Ubicación:" + txtLocationStr + "\n"+
            "Revisa la aplicación para mas detalles";

    //enviarSMS(mensaje);
    */
    private void registrarUsuario() {
        String email = etEmailReg.getText().toString();
        String pass = etPassReg.getText().toString();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> {
                    String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("email", email);
                    userData.put("uid", userId);

                    db.collection("usuarios").document(userId)
                            .set(userData)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, HomeActivity.class));
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Error guardando datos", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}