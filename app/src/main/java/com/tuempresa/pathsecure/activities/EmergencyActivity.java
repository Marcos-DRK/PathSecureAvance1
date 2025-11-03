package com.tuempresa.pathsecure.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tuempresa.pathsecure.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EmergencyActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        Button btnCancelEmergency = findViewById(R.id.btnCancelEmergency);
        TextView txtStatus = findViewById(R.id.txtStatus);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        txtStatus.setText("⚠️ Emergencia activada. Enviando alerta...⚠️");

        registrarEmergencia();

        btnCancelEmergency.setOnClickListener(v -> finish());
    }

    private void registrarEmergencia() {
        String tipo = getIntent().getStringExtra("tipo");
        if (tipo == null) tipo ="manual";
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "anónimo";
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String, Object> alerta = new HashMap<>();
        alerta.put("usuario", userId);
        alerta.put("fecha", timestamp);
        alerta.put("tipo", "manual");
        alerta.put("estado", "activada");

        db.collection("emergencias").add(alerta)
                .addOnSuccessListener(ref -> Toast.makeText(this, "Emergencia registrada", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error al registrar emergencia", Toast.LENGTH_SHORT).show());
    }
}