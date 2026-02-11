package com.example.ecocity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SupportChatActivity extends AppCompatActivity {

    private TextInputEditText hostInput;
    private TextInputEditText portInput;
    private TextInputEditText messageInput;
    private TextView chatLog;
    private ScrollView chatScroll;
    private Button connectButton;
    private Button sendButton;

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private boolean connected = false;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_chat);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.chat_toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.setNavigationOnClickListener(v -> finish());
            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_logout) {
                    sessionManager.clearSession();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finishAffinity();
                    return true;
                }
                return false;
            });
        }

        hostInput = findViewById(R.id.host_input);
        portInput = findViewById(R.id.port_input);
        messageInput = findViewById(R.id.message_edit_text);
        chatLog = findViewById(R.id.chat_log);
        chatScroll = findViewById(R.id.chat_scroll);
        connectButton = findViewById(R.id.connect_button);
        sendButton = findViewById(R.id.send_button);

        connectButton.setOnClickListener(v -> {
            if (connected) {
                disconnect();
            } else {
                connect();
            }
        });

        sendButton.setOnClickListener(v -> enviarMensaje());
    }

    private void connect() {
        final String host = hostInput.getText() != null ? hostInput.getText().toString().trim() : "";
        final String portText = portInput.getText() != null ? portInput.getText().toString().trim() : "";

        if (TextUtils.isEmpty(host) || TextUtils.isEmpty(portText)) {
            Toast.makeText(this, "Introduce host y puerto", Toast.LENGTH_SHORT).show();
            return;
        }

        final int port = Integer.parseInt(portText);

        appendMessage("Conectando a " + host + ":" + port + "…");
        connectButton.setEnabled(false);

        executor.execute(() -> {
            try {
                socket = new Socket(host, port);
                writer = new PrintWriter(socket.getOutputStream(), true);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                connected = true;

                mainHandler.post(() -> {
                    appendMessage("Conexión establecida");
                    connectButton.setText("Desconectar");
                    connectButton.setEnabled(true);
                    sendButton.setEnabled(true);
                });

                listenForResponses();
            } catch (IOException e) {
                mainHandler.post(() -> {
                    appendMessage("No se pudo conectar: " + e.getMessage());
                    connectButton.setEnabled(true);
                });
                disconnectInternal();
            }
        });
    }

    private void listenForResponses() {
        try {
            String line;
            while (connected && reader != null && (line = reader.readLine()) != null) {
                final String message = "Soporte: " + line;
                mainHandler.post(() -> appendMessage(message));
            }
        } catch (IOException ignored) {
        } finally {
            mainHandler.post(this::disconnectInternal);
        }
    }

    private void enviarMensaje() {
        if (!connected || writer == null) {
            Toast.makeText(this, "Conéctate al chat primero", Toast.LENGTH_SHORT).show();
            return;
        }

        final String texto = messageInput.getText() != null ? messageInput.getText().toString().trim() : "";
        if (texto.isEmpty()) {
            return;
        }

        messageInput.setText("");
        appendMessage("Tú: " + texto);

        executor.execute(() -> {
            writer.println(texto);
            writer.flush();
        });
    }

    private void appendMessage(String message) {
        chatLog.append(message + "\n");
        chatScroll.post(() -> chatScroll.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private void disconnect() {
        appendMessage("Desconectando…");
        disconnectInternal();
    }

    private void disconnectInternal() {
        connected = false;
        sendButton.setEnabled(false);
        connectButton.setText("Conectar");
        connectButton.setEnabled(true);

        try {
            if (writer != null) {
                writer.close();
            }
        } catch (Exception ignored) {
        }
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (Exception ignored) {
        }
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception ignored) {
        }

        writer = null;
        reader = null;
        socket = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectInternal();
        executor.shutdownNow();
    }
}
