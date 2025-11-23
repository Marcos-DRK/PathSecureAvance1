package com.tuempresa.pathsecure.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri; // Nueva importación para Settings
import android.os.Bundle;
import android.provider.Settings; // Nueva importación
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tuempresa.pathsecure.R;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements SensorEventListener {

    private Button btnRoutes, btnSettings, btnPanic;
    private TextView txtLocation; // Este TextView no se usa en el código, pero se mantiene.

    // Permisos y Constantes
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] CRITICAL_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECORD_AUDIO
    };


    private static final double SHAKE_THRESHOLD = 2.5;
    public static String ultimaUbicacion = "Sin datos";

    // Sensores
    private SensorManager sensorManager;
    private long lastShakeTime = 0;

    // Volumen
    private int contadorVolumen = 0;
    private long ultimoClick = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        // Llamada a la verificación de permisos
        checkAndRequestPermissions();
        initSensor();
        initListeners();
    }

    @SuppressLint("WrongViewCast")
    private void initViews() {
        btnRoutes = findViewById(R.id.btnRoutes);
        btnSettings = findViewById(R.id.btnSettings);
        btnPanic = findViewById(R.id.btnPanic);
        // Asegúrate de que este TextView exista en tu XML o elimínalo
        // txtLocation = findViewById(R.id.txtLocation);
    }

    private void initSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometer != null) {
            sensorManager.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL
            );
        } else {
            Toast.makeText(this, "Acelerómetro no disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void initListeners() {
        // CORRECCIÓN: Se envuelve la lógica de emergencia para chequear permisos
        btnPanic.setOnClickListener(v -> activarEmergencia("manual"));

        btnRoutes.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, RoutesActivity.class)));

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class)));
    }

    // -----------------------------
    // GESTIÓN DE PERMISOS
    // -----------------------------

    private void checkAndRequestPermissions() {
        List<String> neededPermissions = new ArrayList<>();

        for (String permission : CRITICAL_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(permission);
            }
        }

        if (!neededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    neededPermissions.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        }
        // No se muestra Toast si están concedidos, para evitar spam.
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (!allGranted) {
                // Si el usuario deniega, le ofrecemos ir a Ajustes
                showSettingsDialog();
            } else {
                Toast.makeText(this, "Permisos esenciales concedidos.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permisos Requeridos")
                .setMessage("La alerta necesita permisos de Ubicación, SMS y Micrófono. Por favor, actívelos manualmente en Ajustes.")
                .setPositiveButton("Ir a Ajustes", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel())
                .setCancelable(false)
                .show();
    }

    // -----------------------------
    // SENSOR: SHAKE DETECTION
    // -----------------------------

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double acceleration = Math.sqrt(x*x + y*y + z*z)
                    - SensorManager.GRAVITY_EARTH;

            if (acceleration > SHAKE_THRESHOLD) {
                long currentTime = System.currentTimeMillis();

                if ((currentTime - lastShakeTime) > 12000) {
                    lastShakeTime = currentTime;
                    activarEmergencia("sensor");
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    // -----------------------------
    // BOTONES DE VOLUMEN y EMERGENCIA
    // -----------------------------

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            detectarSecuenciaVolumen();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void detectarSecuenciaVolumen() {
        long tiempoActual = System.currentTimeMillis();

        if (tiempoActual - ultimoClick < 500) {
            contadorVolumen++;
            if (contadorVolumen >= 4) {
                activarEmergencia("volumen");
                contadorVolumen = 0;
            }
        } else {
            contadorVolumen = 1;
        }

        ultimoClick = tiempoActual;
    }

    private void activarEmergencia(String tipo) {
        // CORRECCIÓN CRÍTICA: Verificar permisos antes de activar la emergencia
        if (!checkAllPermissionsGranted()) {
            Toast.makeText(this, "ERROR: Debe conceder todos los permisos para activar la alerta.", Toast.LENGTH_LONG).show();
            // Si falta algún permiso, volvemos a solicitarlos (o mostramos el diálogo de ajustes)
            checkAndRequestPermissions();
            return;
        }

        Toast.makeText(this, "Emergencia activada por " + tipo, Toast.LENGTH_SHORT).show();

        Intent i = new Intent(HomeActivity.this, EmergencyActivity.class);
        i.putExtra("tipo", tipo);
        startActivity(i);
    }

    private boolean checkAllPermissionsGranted() {
        for (String permission : CRITICAL_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    // -----------------------------
    // CICLO DE VIDA
    // -----------------------------

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null)
            sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initSensor();
        // Volvemos a chequear y solicitar permisos cada vez que la app se enfoca
        checkAndRequestPermissions();
    }
}