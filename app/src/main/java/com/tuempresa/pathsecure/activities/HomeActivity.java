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
import android.provider.Settings;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tuempresa.pathsecure.R;


public class HomeActivity extends AppCompatActivity implements SensorEventListener {

    private Button btnRoutes, btnSettings, btnEmergency;
    private TextView txtLocation;

    private static final int REQ_LOCATION = 1001;
    private static final double SHAKE_THRESHOLD = 2.5;
    public static String ultimaUbicacion = "Sin datos";

    // Sensores
    private SensorManager sensorManager;
    private long lastShakeTime = 0;

    // Volumen
    private int contadorVolumen = 0;
    private long ultimoClick = 0;

    // GPS
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        initSensor();
        initLocation();
        initListeners();
    }

    private void initViews() {
        btnRoutes = findViewById(R.id.btnRoutes);
        btnSettings = findViewById(R.id.btnSettings);
        btnEmergency = findViewById(R.id.btnEmergency);
        txtLocation = findViewById(R.id.txtLocation);
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

    private void initLocation() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Listener que actualiza la ubicación en tiempo real
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();

                ultimaUbicacion = "Lat: " + lat + ", Lon: " + lon;
                txtLocation.setText(ultimaUbicacion);
            }
        };

        // Primero revisamos GPS encendido
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Activa el GPS", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        }

        // Luego permisos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQ_LOCATION
            );
            return;
        }

        // Inicio seguro de ubicación
        startLocation();
    }

    @SuppressLint("MissingPermission")
    private void startLocation() {

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                2000,
                5,
                locationListener
        );

        Location last = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (last != null) {
            double lat = last.getLatitude();
            double lon = last.getLongitude();
            ultimaUbicacion = "Lat: " + lat + ", Lon: " + lon;
            txtLocation.setText(ultimaUbicacion);
        }
    }

    private void initListeners() {

        btnEmergency.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, EmergencyActivity.class);
            i.putExtra("tipo", "manual");
            startActivity(i);
        });

        btnRoutes.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, RoutesActivity.class)));

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class)));
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

                if ((currentTime - lastShakeTime) > 3000) {
                    lastShakeTime = currentTime;
                    activarEmergencia("sensor");
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    // -----------------------------
    // BOTONES DE VOLUMEN
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
        Toast.makeText(this, "Emergencia activada por " + tipo, Toast.LENGTH_SHORT).show();

        Intent i = new Intent(HomeActivity.this, EmergencyActivity.class);
        i.putExtra("tipo", tipo);
        startActivity(i);
    }

    // -----------------------------
    // CICLO DE VIDA
    // -----------------------------

    @Override
    protected void onPause() {
        super.onPause();

        if (sensorManager != null)
            sensorManager.unregisterListener(this);

        if (locationManager != null && locationListener != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (SecurityException ignored) {}
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initSensor();
        initLocation();
    }
}