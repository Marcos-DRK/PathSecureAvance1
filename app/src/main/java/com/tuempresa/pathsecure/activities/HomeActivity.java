package com.tuempresa.pathsecure.activities;

import androidx.annotation.NonNull;
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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tuempresa.pathsecure.R;

public class HomeActivity extends AppCompatActivity implements SensorEventListener {

    Button btnRoutes, btnSettings, btnEmergency;
    private static final int REQUEST_LOCATION = 1;
    private TextView txtLocation;

    // Sensor
    private SensorManager sensorManager;
    private long lastShakeTime = 0;
    private static final double SHAKE_THRESHOLD = 2.5;

    // Volumen
    private int contadorVolumen = 0;
    private long ultimoClick = 0;

    public static String ultimaUbicacion = "Sin datos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnRoutes = findViewById(R.id.btnRoutes);
        btnSettings = findViewById(R.id.btnSettings);
        btnEmergency = findViewById(R.id.btnEmergency);
        txtLocation = findViewById(R.id.txtLocation);

        // Configurar sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        // Permiso de ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            obtenerUbicacion();
        }

        // Botón emergencia manual
        btnEmergency.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, EmergencyActivity.class);
            i.putExtra("tipo", "manual");
            startActivity(i);
        });
    }

    // GPS
    @SuppressLint("SetTextI18n")
    private void obtenerUbicacion() throws SecurityException {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                2000, 5, new LocationListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();
                        txtLocation.setText("Lat: " + lat + "\nLon: " + lon);
                    }
                });
    }

    // --- Sensor: detección de movimiento ---
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            double acceleration = Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;

            if (acceleration > SHAKE_THRESHOLD) {
                long currentTime = System.currentTimeMillis();
                if ((currentTime - lastShakeTime) > 3000) {
                    lastShakeTime = currentTime;
                    activarEmergencia("sensor");
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    // --- Botones de volumen ---
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            detectarSecuencia();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void detectarSecuencia() {
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
        Toast.makeText(this, "Emergencia activada por " + tipo, Toast.LENGTH_SHORT).show();
        Intent i = new Intent(HomeActivity.this, EmergencyActivity.class);
        i.putExtra("tipo", tipo);
        startActivity(i);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    public void onLocationChaged(@NonNull Location location){
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        ultimaUbicacion = "lat:"+ lat + ",lon:"+ lon;txtLocation.setText(ultimaUbicacion);
    }
}
