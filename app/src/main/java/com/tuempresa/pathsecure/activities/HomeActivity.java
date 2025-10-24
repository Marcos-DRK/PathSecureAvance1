package com.tuempresa.pathsecure.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tuempresa.pathsecure.R;

public class HomeActivity extends AppCompatActivity {

    Button btnRoutes, btnSettings, btnEmergency;
    private static final int REQUEST_LOCATION = 1;
    private TextView txtLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnRoutes = findViewById(R.id.btnRoutes);
        btnSettings = findViewById(R.id.btnSettings);
        btnEmergency = findViewById(R.id.btnEmergency);
        txtLocation = findViewById(R.id.txtLocation);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            obtenerUbicacion();
        }
    }


    @SuppressLint("SetTextI18n")
    private void obtenerUbicacion() {
        try {
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

            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                txtLocation.setText("Lat: " + lat + "\nLon: " + lon);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacion();
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
        }
    }
}
