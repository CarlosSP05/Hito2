package com.example.ecocity;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SessionManager {

    private static final String PREF_NAME = "eco_city_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_PASSWORD_HASH = "user_password_hash";
    private static final String KEY_PASSWORD_SALT = "user_password_salt";

    private final SharedPreferences preferences;
    private final SecureRandom secureRandom = new SecureRandom();

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setLoggedIn(boolean isLoggedIn) {
        preferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
                .apply();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void saveCredentials(String email, String password) {
        String salt = generateSalt();
        String hash = hashPassword(password, salt);
        preferences.edit()
                .putString(KEY_EMAIL, email)
                .putString(KEY_PASSWORD_HASH, hash)
                .putString(KEY_PASSWORD_SALT, salt)
                .apply();
    }

    public boolean hasCredentials() {
        return preferences.contains(KEY_EMAIL)
                && preferences.contains(KEY_PASSWORD_HASH)
                && preferences.contains(KEY_PASSWORD_SALT);
    }

    public boolean validateCredentials(String email, String password) {
        String storedEmail = preferences.getString(KEY_EMAIL, "");
        String storedSalt = preferences.getString(KEY_PASSWORD_SALT, "");
        String storedHash = preferences.getString(KEY_PASSWORD_HASH, "");
        String providedHash = hashPassword(password, storedSalt);
        return storedEmail.equals(email) && storedHash.equals(providedHash);
    }

    public String getUserEmail() {
        return preferences.getString(KEY_EMAIL, "");
    }

    public void clearSession() {
        preferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .apply();
    }

    private String generateSalt() {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }

    private String hashPassword(String password, String salt) {
        if (password == null || salt == null || salt.isEmpty()) {
            return "";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(Base64.decode(salt, Base64.NO_WRAP));
            byte[] hashed = digest.digest(password.getBytes());
            return Base64.encodeToString(hashed, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }
}
