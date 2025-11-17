package com.tuempresa.pathsecure.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.tuempresa.pathsecure.R;

// Importaciones de OSMDroid
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapActivity extends AppCompatActivity {

    private MapView map;
    private Button btnVolver;


    // GPS y Localización
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Marker currentLocationMarker; // Marcador dinámico
    private final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Configuración OSMDroid (CRÍTICO)
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_map);

        // Inicializar vistas
        map = findViewById(R.id.mapView);
        btnVolver = findViewById(R.id.btnBackMap);

        // Inicializar el manejo de GPS
        initLocation();

        // 2. Manejo de Permisos (Ubicación y Almacenamiento)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Solicitar permisos
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE // Para caché OSMDroid
                    },
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permisos ya concedidos: Iniciar ubicación
            startLocationUpdates();
        }

        // 3. CONFIGURAR MAPA
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        // 4. Crear el marcador inicial (Ej: Chillán)
        GeoPoint initialPoint = new GeoPoint(-36.6063, -72.1034);

        currentLocationMarker = new Marker(map);
        currentLocationMarker.setPosition(initialPoint);
        currentLocationMarker.setTitle("Mi Ubicación Actual");
        // Asegúrate de tener un recurso llamado 'ic_mylocation' o usa 'R.drawable.ic_menu_mylocation'
        currentLocationMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_mylocation));

        map.getOverlays().add(currentLocationMarker); // Agregar el marcador a la capa
        map.getController().setZoom(15.0);
        map.getController().setCenter(initialPoint);

        // 5. Listener del Botón Volver
        btnVolver.setOnClickListener(v -> finish());
    }

    // --- LÓGICA DE UBICACIÓN ---

    private void initLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Listener que recibe y procesa las nuevas ubicaciones
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                GeoPoint newLocation = new GeoPoint(location.getLatitude(), location.getLongitude());

                // Mover el marcador a la nueva posición
                currentLocationMarker.setPosition(newLocation);

                // Opcional: Centrar el mapa en la nueva ubicación (quita el comentario para activar)
                // map.getController().animateTo(newLocation);

                // Forzar el redibujado del mapa para mostrar el marcador actualizado
                map.invalidate();
            }
        };
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        // Verificar si el GPS está encendido
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Activa el GPS para ver tu posición", Toast.LENGTH_LONG).show();
            // Abrir configuración de ubicación
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        }

        // Iniciar solicitud de actualizaciones: cada 3 segundos o cada 5 metros
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                3000,
                5,
                locationListener
        );
    }

    // --- MANEJO DE PERMISOS ---

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido: iniciamos la ubicación
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Se necesita permiso de ubicación para mostrar tu posición", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // --- CICLO DE VIDA ---

    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) map.onResume();
        // Si ya teníamos permisos antes de onPause, se inicia de nuevo el GPS al volver (opcional)
        // Revisar si ya tienes el permiso antes de llamar a startLocationUpdates()
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (map != null) map.onPause();

        // CRÍTICO: Detener las actualizaciones de ubicación para ahorrar batería
        if (locationManager != null && locationListener != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (SecurityException ignored) {
                // Se ignora si los permisos se perdieron
            }
        }
    }
}