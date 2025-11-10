package com.tuempresa.pathsecure.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.telephony.SmsManager;
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
    private static final int REQUEST_SMS = 300;

    private TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        txtStatus = findViewById(R.id.txtStatus);
        Button btnCancelEmergency = findViewById(R.id.btnCancelEmergency);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        txtStatus.setText("Emergencia activada. Solicitando permisos...");

        solicitarPermisos();

        btnCancelEmergency.setOnClickListener(v -> finish());
    }

    private void solicitarPermisos() {
        // Primero Audio
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO);
            return;
        }

        // Luego SMS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS);
            return;
        }

        // Si ya tiene ambos permisos, iniciar todo
        iniciarEmergencia();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_AUDIO || requestCode == REQUEST_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                solicitarPermisos();
            } else {
                Toast.makeText(this, "Permisos necesarios denegados", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void iniciarEmergencia() {
        txtStatus.setText("Grabando audio y registrando alerta...");

        iniciarGrabacion();
        registrarEmergencia();
        enviarSMS("Emergencia detectada desde PathSecure");
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
        } catch (Exception e) {
            Toast.makeText(this, "Error al iniciar grabación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void registrarEmergencia() {
        String tipo = getIntent().getStringExtra("tipo");
        if (tipo == null) tipo ="manual";

        String userId = mAuth.getCurrentUser() != null
                ? mAuth.getCurrentUser().getUid()
                : "anónimo";

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String, Object> alerta = new HashMap<>();
        alerta.put("usuario", userId);
        alerta.put("fecha", timestamp);
        alerta.put("tipo", tipo);
        alerta.put("estado", "activada");
        alerta.put("grabacion", filePath);

        db.collection("emergencias").add(alerta);
    }

    private void enviarSMS(String mensaje) {
        try {
            String num = "123456789";
            SmsManager.getDefault().sendTextMessage(num, null, mensaje, null, null);
        } catch (Exception e) {
            Toast.makeText(this, "Error enviando SMS", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recorder != null) {
            recorder.stop();
            recorder.release();
        }
    }
}