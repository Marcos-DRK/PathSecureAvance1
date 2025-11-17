package com.tuempresa.pathsecure.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.media.MediaPlayer; // NUEVO: Para la reproducción
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tuempresa.pathsecure.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File; // NUEVO
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
    private MediaPlayer player; // NUEVO: Para reproducir el audio
    private String filePath;
    private boolean isRecording = false; // NUEVO: Estado de la grabación

    private static final int REQUEST_AUDIO = 200;
    private static final int REQUEST_SMS = 300;
    // Se necesita permiso de lectura para reproducir el archivo
    private static final int REQUEST_STORAGE_READ = 400;

    private TextView txtStatus;
    private Button btnCancelEmergency;
    private Button btnPlayRecording; // NUEVO: Botón de reproducción

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        txtStatus = findViewById(R.id.txtStatus);
        btnCancelEmergency = findViewById(R.id.btnCancelEmergency);
        btnPlayRecording = new Button(this); // Inicialización simple (debería existir en XML, pero por ahora en código)
        // Puedes añadir el botón de reproducción a activity_emergency.xml y usar findViewById.

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        txtStatus.setText("Emergencia activada. Solicitando permisos...");

        solicitarPermisos();

        btnCancelEmergency.setOnClickListener(v -> terminarEmergencia());

        // Listener de Reproducción (AÑADIR ESTE BOTÓN AL XML DE activity_emergency)
        /*
        btnPlayRecording.setOnClickListener(v -> {
            if (filePath != null && new File(filePath).exists()) {
                reproducirGrabacion();
            } else {
                Toast.makeText(this, "Grabación no encontrada.", Toast.LENGTH_SHORT).show();
            }
        });
        */
    }

    private void solicitarPermisos() {
        // Audio
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO);
            return;
        }

        // SMS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS);
            return;
        }

        // Almacenamiento (Para lectura/reproducción posterior)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_READ);
            return;
        }


        // Si ya tiene todos los permisos, iniciar todo
        iniciarEmergencia();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_AUDIO || requestCode == REQUEST_SMS || requestCode == REQUEST_STORAGE_READ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Reintentar la solicitud de permisos hasta que todos estén OK
                solicitarPermisos();
            } else {
                Toast.makeText(this, "Permisos necesarios denegados. No se iniciará la alerta.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void iniciarEmergencia() {
        txtStatus.setText("Grabando audio y registrando alerta...");

        iniciarGrabacion();
        registrarEmergencia();
        // **PENDIENTE**: Se necesita recuperar la lista de contactos de SettingsActivity
        enviarSMS("Emergencia detectada desde PathSecure. Mi última ubicación: " + HomeActivity.ultimaUbicacion);

        isRecording = true;
    }

    private void iniciarGrabacion() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

            // Usar getExternalFilesDir(null) garantiza que la grabación se almacene en la carpeta de la app
            // y que no se borre al desinstalar, a menos que se guarde en Pictures/Music, etc.
            filePath = getExternalFilesDir(null).getAbsolutePath() + "/alerta_" + timeStamp + ".3gp";

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(filePath);

            recorder.prepare();
            recorder.start();
        } catch (Exception e) {
            Toast.makeText(this, "Error al iniciar grabación: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void reproducirGrabacion() {
        try {
            if (player == null) {
                player = new MediaPlayer();
                player.setDataSource(filePath);
                player.prepare();
                player.start();
                Toast.makeText(this, "Reproduciendo...", Toast.LENGTH_SHORT).show();

                player.setOnCompletionListener(mp -> {
                    Toast.makeText(this, "Reproducción finalizada.", Toast.LENGTH_SHORT).show();
                    player.release();
                    player = null;
                });
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error de reproducción: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        alerta.put("grabacion_path", filePath); // Nombre de campo cambiado para claridad

        db.collection("emergencias").add(alerta);
    }

    private void enviarSMS(String mensaje) {
        // **PENDIENTE**: Se debe iterar sobre la lista de contactos seguros, no solo uno fijo.
        try {
            String num = "123456789";
            SmsManager.getDefault().sendTextMessage(num, null, mensaje, null, null);
        } catch (Exception e) {
            Toast.makeText(this, "Error enviando SMS. Verifica el número.", Toast.LENGTH_SHORT).show();
        }
    }

    private void terminarEmergencia() {
        if (recorder != null && isRecording) {
            recorder.stop();
            recorder.release();
            isRecording = false;
            Toast.makeText(this, "Grabación guardada en: " + filePath, Toast.LENGTH_LONG).show();
        }
        if (player != null) {
            player.release();
            player = null;
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Es importante liberar recursos
        terminarEmergencia();
    }
}