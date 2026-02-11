package com.example.ecocity;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class IncidenciaAdapter extends RecyclerView.Adapter<IncidenciaAdapter.IncidenciaViewHolder> {

    public interface OnIncidenciaListener {
        void onIncidenciaSelected(Incidencia incidencia);
        void onIncidenciaDelete(Incidencia incidencia);
    }

    private final LayoutInflater inflater;
    private final List<Incidencia> incidencias = new ArrayList<>();
    private final OnIncidenciaListener listener;

    public IncidenciaAdapter(Context context, OnIncidenciaListener listener) {
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    public void submitList(List<Incidencia> nuevasIncidencias) {
        incidencias.clear();
        if (nuevasIncidencias != null) {
            incidencias.addAll(nuevasIncidencias);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IncidenciaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.incidencia_item, parent, false);
        return new IncidenciaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncidenciaViewHolder holder, int position) {
        Incidencia incidencia = incidencias.get(position);
        holder.bind(incidencia);
    }

    @Override
    public int getItemCount() {
        return incidencias.size();
    }

    class IncidenciaViewHolder extends RecyclerView.ViewHolder {

        private final TextView titulo;
        private final TextView descripcion;
        private final TextView metaInfo;
        private final TextView fechaText;
        private final ImageView iconFoto;
        private final ImageView iconAudio;
        private final ImageButton btnBorrar;

        IncidenciaViewHolder(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.titulo);
            descripcion = itemView.findViewById(R.id.descripcion);
            metaInfo = itemView.findViewById(R.id.meta_info);
            fechaText = itemView.findViewById(R.id.fecha_text);
            iconFoto = itemView.findViewById(R.id.icon_foto);
            iconAudio = itemView.findViewById(R.id.icon_audio);
            btnBorrar = itemView.findViewById(R.id.btn_borrar);
        }

        void bind(Incidencia incidencia) {
            titulo.setText(incidencia.getTitulo());
            descripcion.setText(incidencia.getDescripcion());
            String ubicacion = incidencia.getUbicacion() != null ? incidencia.getUbicacion() : "Sin referencia";
            metaInfo.setText("Urgencia: " + incidencia.getUrgencia() + "  •  Ubicación: " + ubicacion);

            long fecha = incidencia.getFechaCreacion() > 0 ? incidencia.getFechaCreacion() * 1000 : System.currentTimeMillis();
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(fecha, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
            fechaText.setText(relativeTime);

            iconFoto.setVisibility(incidencia.tieneFoto() ? View.VISIBLE : View.GONE);
            iconAudio.setVisibility(incidencia.tieneAudio() ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onIncidenciaSelected(incidencia);
                }
            });

            btnBorrar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onIncidenciaDelete(incidencia);
                }
            });
        }
    }
}
