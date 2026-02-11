package com.example.ecocity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListaIncidenciasActivity extends AppCompatActivity implements IncidenciaAdapter.OnIncidenciaListener {

    public static final String EXTRA_INCIDENCIA_ID = "extra_incidencia_id";

    private DatabaseHelper dbHelper;
    private IncidenciaAdapter adapter;
    private SessionManager sessionManager;
    private RecyclerView recyclerView;
    private Button nuevaIncidenciaButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_incidencias);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.lista_incidencias_recycler);
        nuevaIncidenciaButton = findViewById(R.id.volver_a_inicio_button);
        Toolbar toolbar = findViewById(R.id.lista_toolbar);

        adapter = new IncidenciaAdapter(this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        if (toolbar != null) {
            toolbar.setSubtitle(sessionManager.getUserEmail());
            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_logout) {
                    logout();
                    return true;
                } else if (item.getItemId() == R.id.action_support) {
                    startActivity(new Intent(this, SupportChatActivity.class));
                    return true;
                }
                return false;
            });
        }

        nuevaIncidenciaButton.setOnClickListener(v -> openIncidenciaForm(-1));

        cargarIncidencias();
    }

    private void cargarIncidencias() {
        List<Incidencia> incidencias = dbHelper.getAllIncidencias();
        adapter.submitList(incidencias);

        if (incidencias.isEmpty()) {
            Toast.makeText(this, "No hay incidencias registradas todavía", Toast.LENGTH_SHORT).show();
        }
    }

    private void openIncidenciaForm(long incidenciaId) {
        Intent intent = new Intent(this, IncidenciaActivity.class);
        if (incidenciaId > 0) {
            intent.putExtra(EXTRA_INCIDENCIA_ID, incidenciaId);
        }
        startActivity(intent);
    }

    @Override
    public void onIncidenciaSelected(Incidencia incidencia) {
        openIncidenciaForm(incidencia.getId());
    }

    @Override
    public void onIncidenciaDelete(Incidencia incidencia) {
        showDeleteDialog(incidencia);
    }

    private void showDeleteDialog(Incidencia incidencia) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar incidencia")
                .setMessage("¿Deseas eliminar \"" + incidencia.getTitulo() + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    dbHelper.deleteIncidencia(incidencia.getId());
                    cargarIncidencias();
                    Toast.makeText(this, "Incidencia eliminada", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sessionManager.isLoggedIn()) {
            cargarIncidencias();
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
