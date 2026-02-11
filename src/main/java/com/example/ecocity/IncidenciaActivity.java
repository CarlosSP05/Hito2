package com.example.ecocity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class IncidenciaActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 2;
    private static final int REQUEST_MAP_LOCATION = 3;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 4;
    private static final int REQUEST_LOCATION_PERMISSION = 5;

    private EditText tituloEditText, descripcionEditText, ubicacionEditText;
    private Spinner urgenciaSpinner;
    private Button guardarButton, fotoButton, mapaButton, ubicacionActualButton;
    private Button audioRecordButton, audioPlayButton, audioDeleteButton;
    private ImageView fotoImageView;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private long incidenciaId = -1;
    private String fotoPath = "";
    private String audioPath = "";
    private double latitud = 0;
    private double longitud = 0;
    private boolean wantsToRecordAudio = false;
    private boolean wantsLocation = false;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private boolean isRecording = false;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incidencia);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        dbHelper = new DatabaseHelper(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        Toolbar toolbar = findViewById(R.id.incidencia_toolbar);
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

        tituloEditText = findViewById(R.id.titulo);
        descripcionEditText = findViewById(R.id.descripcion);
        ubicacionEditText = findViewById(R.id.ubicacion);
        urgenciaSpinner = findViewById(R.id.urgencia_spinner);
        guardarButton = findViewById(R.id.guardar_button);
        fotoButton = findViewById(R.id.foto_button);
        mapaButton = findViewById(R.id.abrir_mapa_button);
        ubicacionActualButton = findViewById(R.id.ubicacion_actual_button);
        fotoImageView = findViewById(R.id.foto_image_view);
        audioRecordButton = findViewById(R.id.audio_record_button);
        audioPlayButton = findViewById(R.id.audio_play_button);
        audioDeleteButton = findViewById(R.id.audio_delete_button);

        ArrayAdapter<CharSequence> urgenciaAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.niveles_urgencia,
                android.R.layout.simple_spinner_item
        );
        urgenciaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        urgenciaSpinner.setAdapter(urgenciaAdapter);

        incidenciaId = getIntent().getLongExtra(ListaIncidenciasActivity.EXTRA_INCIDENCIA_ID, -1);
        if (incidenciaId > 0) {
            cargarIncidenciaExistente(incidenciaId);
            if (toolbar != null) {
                toolbar.setTitle("Editar incidencia");
            }
        }

        if (savedInstanceState != null) {
            fotoPath = savedInstanceState.getString("fotoPath", "");
            audioPath = savedInstanceState.getString("audioPath", "");
            latitud = savedInstanceState.getDouble("latitud", 0);
            longitud = savedInstanceState.getDouble("longitud", 0);
            if (!fotoPath.isEmpty()) {
                mostrarFoto();
            }
            updateAudioButtonsState();
        }

        guardarButton.setOnClickListener(v -> guardarIncidencia());

        fotoButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else {
                openCamera();
            }
        });

        mapaButton.setOnClickListener(v -> openMapPicker());
        ubicacionActualButton.setOnClickListener(v -> solicitarUbicacionActual());

        audioRecordButton.setOnClickListener(v -> toggleRecording());
        audioPlayButton.setOnClickListener(v -> togglePlayback());
        audioDeleteButton.setOnClickListener(v -> borrarAudio());

        updateAudioButtonsState();
    }

    private void guardarIncidencia() {
        String titulo = tituloEditText.getText().toString().trim();
        String descripcion = descripcionEditText.getText().toString().trim();
        String ubicacion = ubicacionEditText.getText().toString().trim();
        String urgencia = (urgenciaSpinner.getSelectedItem() != null)
                ? urgenciaSpinner.getSelectedItem().toString()
                : "";

        if (titulo.isEmpty() || descripcion.isEmpty() || urgencia.isEmpty()) {
            Toast.makeText(this, "Completa título, descripción y urgencia", Toast.LENGTH_SHORT).show();
            return;
        }

        Incidencia incidencia = incidenciaId > 0
                ? dbHelper.getIncidenciaById(incidenciaId)
                : new Incidencia();

        if (incidencia == null) {
            incidencia = new Incidencia();
        }

        incidencia.setTitulo(titulo);
        incidencia.setDescripcion(descripcion);
        incidencia.setUrgencia(urgencia);
        incidencia.setUbicacion(ubicacion);
        incidencia.setLatitud(latitud);
        incidencia.setLongitud(longitud);
        incidencia.setFotoPath(fotoPath);
        incidencia.setAudioPath(audioPath);
        if (incidencia.getFechaCreacion() == 0) {
            incidencia.setFechaCreacion(System.currentTimeMillis() / 1000);
        }

        if (incidenciaId > 0) {
            dbHelper.updateIncidencia(incidencia);
            Toast.makeText(this, "Incidencia actualizada", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.insertIncidencia(incidencia);
            incidenciaId = incidencia.getId();
            Toast.makeText(this, "Incidencia registrada", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void cargarIncidenciaExistente(long id) {
        Incidencia incidencia = dbHelper.getIncidenciaById(id);
        if (incidencia == null) return;

        tituloEditText.setText(incidencia.getTitulo());
        descripcionEditText.setText(incidencia.getDescripcion());
        ubicacionEditText.setText(incidencia.getUbicacion());
        latitud = incidencia.getLatitud();
        longitud = incidencia.getLongitud();
        fotoPath = incidencia.getFotoPath() != null ? incidencia.getFotoPath() : "";
        audioPath = incidencia.getAudioPath() != null ? incidencia.getAudioPath() : "";

        String[] urgencias = getResources().getStringArray(R.array.niveles_urgencia);
        for (int i = 0; i < urgencias.length; i++) {
            if (urgencias[i].equalsIgnoreCase(incidencia.getUrgencia())) {
                urgenciaSpinner.setSelection(i);
                break;
            }
        }

        if (!fotoPath.isEmpty()) {
            mostrarFoto();
        }
        updateAudioButtonsState();
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                if (photoFile != null) {
                    Uri photoUri = FileProvider.getUriForFile(this, "com.example.ecocity.fileprovider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            } catch (IOException e) {
                Toast.makeText(this, "No se pudo crear el archivo de foto", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        fotoPath = image.getAbsolutePath();
        return image;
    }

    private File createAudioFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String audioFileName = "AUDIO_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }
        File audio = File.createTempFile(audioFileName, ".m4a", storageDir);
        audioPath = audio.getAbsolutePath();
        return audio;
    }

    private void mostrarFoto() {
        Uri imageUri = Uri.fromFile(new File(fotoPath));
        fotoImageView.setImageURI(imageUri);
        fotoImageView.setVisibility(View.VISIBLE);
    }

    private void toggleRecording() {
        if (isRecording) {
            stopRecording();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            wantsToRecordAudio = true;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            startRecording();
        }
    }

    private void startRecording() {
        try {
            if (audioPath != null && !audioPath.isEmpty()) {
                File previous = new File(audioPath);
                if (previous.exists()) previous.delete();
            }
            File audioFile = createAudioFile();
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setOutputFile(audioFile.getAbsolutePath());
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            audioRecordButton.setText("Detener grabación");
            audioPlayButton.setEnabled(false);
            audioDeleteButton.setEnabled(false);
        } catch (IOException e) {
            Toast.makeText(this, "No se pudo iniciar la grabación", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        try {
            mediaRecorder.stop();
        } catch (RuntimeException ignored) {
        } finally {
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
            audioRecordButton.setText("Grabar nota");
            updateAudioButtonsState();
        }
    }

    private void togglePlayback() {
        if (isPlaying) {
            stopPlayback();
            return;
        }
        if (audioPath == null || audioPath.isEmpty()) {
            Toast.makeText(this, "No hay nota de voz guardada", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
            audioPlayButton.setText("Detener reproducción");
            audioRecordButton.setEnabled(false);
            mediaPlayer.setOnCompletionListener(mp -> stopPlayback());
        } catch (IOException e) {
            Toast.makeText(this, "No se pudo reproducir el audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPlaying = false;
        audioPlayButton.setText("Reproducir nota");
        audioRecordButton.setEnabled(true);
    }

    private void borrarAudio() {
        if (audioPath == null || audioPath.isEmpty()) return;
        File audioFile = new File(audioPath);
        if (audioFile.exists()) {
            audioFile.delete();
        }
        audioPath = "";
        updateAudioButtonsState();
        Toast.makeText(this, "Nota de voz eliminada", Toast.LENGTH_SHORT).show();
    }

    private void updateAudioButtonsState() {
        boolean hasAudio = audioPath != null && !audioPath.isEmpty();
        audioPlayButton.setEnabled(hasAudio && !isRecording);
        audioDeleteButton.setEnabled(hasAudio && !isRecording);
        audioRecordButton.setEnabled(!isPlaying);
    }

    private void solicitarUbicacionActual() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            wantsLocation = true;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener(this, this::actualizarUbicacion)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show());
    }

    private void actualizarUbicacion(Location location) {
        if (location == null) {
            Toast.makeText(this, "Activa el GPS para obtener la ubicación", Toast.LENGTH_SHORT).show();
            return;
        }
        latitud = location.getLatitude();
        longitud = location.getLongitude();
        String texto = String.format(Locale.getDefault(), "Lat: %.5f, Lng: %.5f", latitud, longitud);
        ubicacionEditText.setText(texto);
        Toast.makeText(this, "Ubicación actualizada", Toast.LENGTH_SHORT).show();
    }

    private void openMapPicker() {
        Intent intent = new Intent(this, GeolocalizacionActivity.class);
        startActivityForResult(intent, REQUEST_MAP_LOCATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            mostrarFoto();
        } else if (requestCode == REQUEST_MAP_LOCATION && resultCode == RESULT_OK && data != null) {
            latitud = data.getDoubleExtra("latitud", 0);
            longitud = data.getDoubleExtra("longitud", 0);
            String ubicacion = String.format(Locale.getDefault(), "Lat: %.5f, Lng: %.5f", latitud, longitud);
            ubicacionEditText.setText(ubicacion);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Se requiere el permiso de cámara", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && wantsToRecordAudio) {
                wantsToRecordAudio = false;
                startRecording();
            } else {
                Toast.makeText(this, "Se requiere el permiso de micrófono", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && wantsLocation) {
                wantsLocation = false;
                solicitarUbicacionActual();
            } else {
                Toast.makeText(this, "Activa el permiso de ubicación para continuar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("fotoPath", fotoPath);
        outState.putString("audioPath", audioPath);
        outState.putDouble("latitud", latitud);
        outState.putDouble("longitud", longitud);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isRecording) {
            stopRecording();
        }
        if (isPlaying) {
            stopPlayback();
        }
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
