package com.example.ecocity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ecoCity.db";
    private static final int DATABASE_VERSION = 2;

    // Nombres de la tabla y columnas
    public static final String TABLE_INCIDENTES = "incidentes";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITULO = "titulo";
    public static final String COLUMN_DESCRIPCION = "descripcion";
    public static final String COLUMN_URGENCIA = "urgencia";
    public static final String COLUMN_UBICACION = "ubicacion";
    public static final String COLUMN_LATITUD = "latitud";
    public static final String COLUMN_LONGITUD = "longitud";
    public static final String COLUMN_FOTO_PATH = "foto_path";
    public static final String COLUMN_AUDIO_PATH = "audio_path";
    public static final String COLUMN_FECHA = "fecha_creacion";

    // Constructor de la base de datos
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear la tabla de incidencias si no existe
        String CREATE_TABLE = "CREATE TABLE " + TABLE_INCIDENTES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITULO + " TEXT NOT NULL, " +
                COLUMN_DESCRIPCION + " TEXT NOT NULL, " +
                COLUMN_URGENCIA + " TEXT NOT NULL, " +
                COLUMN_UBICACION + " TEXT, " +
                COLUMN_LATITUD + " REAL, " +
                COLUMN_LONGITUD + " REAL, " +
                COLUMN_FOTO_PATH + " TEXT, " +
                COLUMN_AUDIO_PATH + " TEXT, " +
                COLUMN_FECHA + " INTEGER DEFAULT(strftime('%s','now')))";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Si la base de datos ya existe, la eliminamos y la creamos nuevamente
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INCIDENTES);
        onCreate(db);
    }

    public long insertIncidencia(Incidencia incidencia) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = incidenciaToValues(incidencia);
        long id = db.insert(TABLE_INCIDENTES, null, values);
        incidencia.setId(id);
        return id;
    }

    public int updateIncidencia(Incidencia incidencia) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = incidenciaToValues(incidencia);
        return db.update(TABLE_INCIDENTES, values, COLUMN_ID + " = ?", new String[]{String.valueOf(incidencia.getId())});
    }

    public void deleteIncidencia(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_INCIDENTES, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public Incidencia getIncidenciaById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_INCIDENTES, null, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToIncidencia(cursor);
            }
        }
        return null;
    }

    public List<Incidencia> getAllIncidencias() {
        List<Incidencia> incidencias = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_INCIDENTES, null, null, null, null, null,
                COLUMN_FECHA + " DESC")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    incidencias.add(cursorToIncidencia(cursor));
                } while (cursor.moveToNext());
            }
        }
        return incidencias;
    }

    private ContentValues incidenciaToValues(Incidencia incidencia) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITULO, incidencia.getTitulo());
        values.put(COLUMN_DESCRIPCION, incidencia.getDescripcion());
        values.put(COLUMN_URGENCIA, incidencia.getUrgencia());
        values.put(COLUMN_UBICACION, incidencia.getUbicacion());
        values.put(COLUMN_LATITUD, incidencia.getLatitud());
        values.put(COLUMN_LONGITUD, incidencia.getLongitud());
        values.put(COLUMN_FOTO_PATH, incidencia.getFotoPath());
        values.put(COLUMN_AUDIO_PATH, incidencia.getAudioPath());
        if (incidencia.getFechaCreacion() > 0) {
            values.put(COLUMN_FECHA, incidencia.getFechaCreacion());
        }
        return values;
    }

    private Incidencia cursorToIncidencia(Cursor cursor) {
        Incidencia incidencia = new Incidencia();
        incidencia.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        incidencia.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITULO)));
        incidencia.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPCION)));
        incidencia.setUrgencia(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URGENCIA)));
        incidencia.setUbicacion(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UBICACION)));
        incidencia.setLatitud(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUD)));
        incidencia.setLongitud(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUD)));
        incidencia.setFotoPath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FOTO_PATH)));
        incidencia.setAudioPath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUDIO_PATH)));
        incidencia.setFechaCreacion(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FECHA)));
        return incidencia;
    }
}
