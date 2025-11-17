package com.tuempresa.pathsecure.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent; // ¡Nueva importación!
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tuempresa.pathsecure.R;

// Importaciones de Clases Locales
import com.tuempresa.pathsecure.activities.Route;
import com.tuempresa.pathsecure.activities.RouteAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class RoutesActivity extends AppCompatActivity
        implements RouteAdapter.OnRouteActionListener, LocationListener {

    private static final int PERMISSION_REQUEST_CODE = 101;
    private static final long MIN_TIME_MS = 5000;
    private static final float MIN_DISTANCE_M = 10;

    private Button btnBackRoutes, btnAddRoute;
    private RecyclerView rvRoutes;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RouteAdapter routeAdapter;
    private List<Route> routesList = new ArrayList<>();

    // Lógica de Grabación
    private LocationManager locationManager;
    private boolean isRecording = false;
    private Route currentRecordingRoute = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);

        mAuth = FirebaseAuth.getInstance();

        // CORRECCIÓN CRÍTICA: Forzar verificación de autenticación al inicio
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Debe iniciar sesión para ver las rutas.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        btnBackRoutes = findViewById(R.id.btnBackRoutes);
        btnAddRoute = findViewById(R.id.btnAddRoute);
        rvRoutes = findViewById(R.id.rvRoutes);

        db = FirebaseFirestore.getInstance();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Configurar RecyclerView
        rvRoutes.setLayoutManager(new LinearLayoutManager(this));
        routeAdapter = new RouteAdapter(routesList, this);
        rvRoutes.setAdapter(routeAdapter);

        btnBackRoutes.setOnClickListener(v -> finish());

        btnAddRoute.setOnClickListener(v -> {
            if (isRecording) {
                showStopRecordingDialog();
            } else {
                startRouteRecording();
            }
        });

        loadRoutes();
        updateRecordingButton(false);
    }

    // --- Lógica de Botón y Estado ---

    private void updateRecordingButton(boolean recording) {
        isRecording = recording;
        if (recording) {
            btnAddRoute.setText("DETENER GRABACIÓN DE RUTA");
            btnAddRoute.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        } else {
            btnAddRoute.setText("Añadir Nueva Ruta / Iniciar Grabación");
            // Usamos un color fijo si no se define en colors.xml
            btnAddRoute.setBackgroundColor(ContextCompat.getColor(this, R.color.teal_700));
        }
    }

    // --- Lógica de Grabación de GPS ---

    private void startRouteRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
            return;
        }

        if (isRecording) return;

        // Crear nueva ruta
        currentRecordingRoute = new Route("Ruta grabada el " + new Date().toString(), new ArrayList<>());

        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_MS,
                    MIN_DISTANCE_M,
                    this
            );
            updateRecordingButton(true);
            Toast.makeText(this, "Grabación de ruta iniciada. ¡Camina ahora!", Toast.LENGTH_LONG).show();

        } catch (SecurityException e) {
            Toast.makeText(this, "Error de permisos de ubicación.", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRouteRecording() {
        if (!isRecording) return;

        locationManager.removeUpdates(this);
        updateRecordingButton(false);

        if (currentRecordingRoute != null && !currentRecordingRoute.getPoints().isEmpty()) {
            showSaveRouteDialog(currentRecordingRoute);
        } else {
            Toast.makeText(this, "Grabación detenida. No se registraron puntos de ubicación.", Toast.LENGTH_SHORT).show();
            currentRecordingRoute = null;
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (isRecording && currentRecordingRoute != null) {
            currentRecordingRoute.addPoint(location.getLatitude(), location.getLongitude());
        }
    }

    // Manejo de Permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRouteRecording();
            } else {
                Toast.makeText(this, "Permiso de ubicación es necesario para grabar rutas.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // --- Diálogos y CRUD de Firestore ---

    private void showStopRecordingDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Detener Grabación")
                .setMessage("¿Deseas detener y guardar la ruta actual?")
                .setPositiveButton("Guardar", (dialog, which) -> stopRouteRecording())
                .setNegativeButton("Cancelar", null)
                .setNeutralButton("Descartar", (dialog, which) -> {
                    locationManager.removeUpdates(this);
                    updateRecordingButton(false);
                    currentRecordingRoute = null;
                    Toast.makeText(this, "Ruta descartada.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showSaveRouteDialog(final Route routeToSave) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Guardar Ruta");
        builder.setMessage("Ingresa un nombre para tu ruta:");

        final EditText input = new EditText(this);
        input.setText(routeToSave.getName());
        builder.setView(input);

        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            String name = input.getText().toString();
            routeToSave.setName(name.isEmpty() ? routeToSave.getName() : name);
            saveRoute(routeToSave);
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            Toast.makeText(this, "Ruta descartada.", Toast.LENGTH_SHORT).show();
            currentRecordingRoute = null;
        });
        builder.show();
    }

    private void saveRoute(Route route) {
        // ID seguro: verificado en onCreate
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("routes")
                .document(route.getId())
                .set(route)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Ruta '" + route.getName() + "' guardada.", Toast.LENGTH_SHORT).show();
                    loadRoutes();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar la ruta: " + e.getMessage(), Toast.LENGTH_LONG).show());

        currentRecordingRoute = null;
    }

    private void loadRoutes() {
        // ID seguro: verificado en onCreate
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).collection("routes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        routesList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // toObject() funciona si Route.java y RoutePoint.java están correctos
                            Route route = document.toObject(Route.class);
                            routesList.add(route);
                        }
                        routeAdapter.updateList(routesList);
                    } else {
                        Toast.makeText(this, "Error cargando rutas: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ... (Implementación de Interface y Métodos de Ciclo de Vida restantes) ...

    @Override
    public void onDeleteClick(Route route) {
        // ID seguro: verificado en onCreate
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("routes")
                .document(route.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, route.getName() + " eliminada.", Toast.LENGTH_SHORT).show();
                    loadRoutes();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al eliminar ruta: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    @Override
    public void onItemClick(Route route) {
        Toast.makeText(this, "Visualizando ruta: " + route.getName() + " (" + route.getPoints().size() + " puntos)", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isRecording) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRecording) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_MS,
                        MIN_DISTANCE_M,
                        this
                );
            }
        }
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {}
    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Toast.makeText(this, "GPS deshabilitado. No se pueden grabar rutas.", Toast.LENGTH_LONG).show();
    }
}