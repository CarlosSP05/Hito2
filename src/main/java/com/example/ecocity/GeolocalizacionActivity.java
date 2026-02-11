package com.example.ecocity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class GeolocalizacionActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button guardarUbicacionButton;
    private double latitud, longitud;
    private boolean ubicacionSeleccionada = false;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geolocalizacion);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        Toolbar toolbar = findViewById(R.id.geolocalizacion_toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.setNavigationOnClickListener(v -> finish());
            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_logout) {
                    logout();
                    return true;
                }
                return false;
            });
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        guardarUbicacionButton = findViewById(R.id.guardar_ubicacion_button);
        guardarUbicacionButton.setOnClickListener(v -> {
            if (!ubicacionSeleccionada) {
                Toast.makeText(this, "Selecciona un punto en el mapa", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(GeolocalizacionActivity.this, "Ubicación guardada: Lat: " + latitud + ", Long: " + longitud, Toast.LENGTH_SHORT).show();
            Intent result = new Intent();
            result.putExtra("latitud", latitud);
            result.putExtra("longitud", longitud);
            setResult(RESULT_OK, result);
            finish();
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Centrar el mapa en una ubicación por defecto (por ejemplo, Madrid)
        LatLng madrid = new LatLng(40.4168, -3.7038);
        mMap.addMarker(new MarkerOptions().position(madrid).title("Marker in Madrid"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(madrid));

        mMap.setOnMapClickListener(latLng -> {
            latitud = latLng.latitude;
            longitud = latLng.longitude;
            ubicacionSeleccionada = true;
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Ubicación seleccionada"));
        });
    }

    private void logout() {
        sessionManager.clearSession();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
