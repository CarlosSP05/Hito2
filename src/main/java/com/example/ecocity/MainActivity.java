package com.example.ecocity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // Asegúrate de que esto apunta al XML correcto

        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            navigateToHome();
            finish();
            return;
        }

        // Inicializamos los elementos de la interfaz
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);  // Corregido

        // Acción al hacer clic en el botón de login
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Por favor, ingrese ambos campos", Toast.LENGTH_SHORT).show();
                } else if (!sessionManager.hasCredentials()) {
                    Toast.makeText(MainActivity.this, "Regístrate antes de iniciar sesión", Toast.LENGTH_SHORT).show();
                } else if (sessionManager.validateCredentials(email, password)) {
                    sessionManager.setLoggedIn(true);
                    navigateToHome();
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Acción al hacer clic en el botón de registrar
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a la actividad de registro (debe existir RegisterActivity)
                Toast.makeText(MainActivity.this, "Registrando...", Toast.LENGTH_SHORT).show();

                // Aquí se navega a la actividad de registro
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void navigateToHome() {
        Intent intent = new Intent(MainActivity.this, ListaIncidenciasActivity.class);
        startActivity(intent);
    }
}
