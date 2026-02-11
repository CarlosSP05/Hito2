package com.example.ecocity;

public class Incidencia {

    private long id;
    private String titulo;
    private String descripcion;
    private String urgencia;
    private String ubicacion;
    private double latitud;
    private double longitud;
    private String fotoPath;
    private String audioPath;
    private long fechaCreacion;

    public Incidencia() {
    }

    public Incidencia(long id, String titulo, String descripcion, String urgencia, String ubicacion,
                      double latitud, double longitud, String fotoPath, String audioPath, long fechaCreacion) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.urgencia = urgencia;
        this.ubicacion = ubicacion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.fotoPath = fotoPath;
        this.audioPath = audioPath;
        this.fechaCreacion = fechaCreacion;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUrgencia() {
        return urgencia;
    }

    public void setUrgencia(String urgencia) {
        this.urgencia = urgencia;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public String getFotoPath() {
        return fotoPath;
    }

    public void setFotoPath(String fotoPath) {
        this.fotoPath = fotoPath;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public long getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(long fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public boolean tieneFoto() {
        return fotoPath != null && !fotoPath.isEmpty();
    }

    public boolean tieneAudio() {
        return audioPath != null && !audioPath.isEmpty();
    }
}
