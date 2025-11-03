package com.tuempresa.pathsecure.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tuempresa.pathsecure.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EmergencyActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private MediaRecorder recorder;
    private String filePath;
    private static final int REQUEST_AUDIO = 200;


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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO);
        } else {
            iniciarGrabacion();
        }

        registrarEmergencia();

        btnCancelEmergency.setOnClickListener(v -> finish());
    }
    private void iniciarGrabacion() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            filePath = getExternalFilesDir(null).getAbsolutePath() + "/alerta_" + timeStamp + ".3gp";

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(filePath);

            recorder.prepare();
            recorder.start();

            Toast.makeText(this, "Grabación iniciada", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error iniciando grabación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void detenerGrabacion() {
        try {
            if (recorder != null) {
                recorder.stop();
                recorder.release();
                recorder = null;
                Toast.makeText(this, "Grabación finalizada y guardada", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al detener grabación", Toast.LENGTH_SHORT).show();
        }
        finish();
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
        alerta.put("grabacion", filePath);

        db.collection("emergencias").add(alerta)
                .addOnSuccessListener(ref -> Toast.makeText(this, "Emergencia registrada", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error al registrar emergencia", Toast.LENGTH_SHORT).show());

    }
    protected void onDestroy(){
        super.onDestroy();
        detenerGrabacion();
    }
}