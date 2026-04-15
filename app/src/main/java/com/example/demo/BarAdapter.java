package com.example.demo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BarAdapter extends RecyclerView.Adapter<BarAdapter.BarViewHolder> {

    private List<Bar> bars;
    private final OnBarClickListener listener;

    public interface OnBarClickListener {
        void onBarClick(Bar bar);
    }

    public BarAdapter(OnBarClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<Bar> bars) {
        this.bars = bars;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bar, parent, false);
        return new BarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BarViewHolder holder, int position) {
        Bar bar = bars.get(position);

        // Affichage des informations de base
        holder.nom.setText(bar.nom);
        holder.adresse.setText(bar.adresse);
        holder.rating.setRating(bar.note);
        holder.commentaire.setText(bar.commentaire);

        // --- LOGIQUE POUR LES AMBIANCES ---
        if (bar.ambiances != null && !bar.ambiances.isEmpty()) {
            holder.ambiances.setText("Ambiances : " + bar.ambiances);
            holder.ambiances.setVisibility(View.VISIBLE); // On l'affiche
        } else {
            holder.ambiances.setVisibility(View.GONE); // On le cache s'il n'y a rien
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBarClick(bar);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bars != null ? bars.size() : 0;
    }

    static class BarViewHolder extends RecyclerView.ViewHolder {
        TextView nom, adresse, ambiances, commentaire;
        RatingBar rating;

        public BarViewHolder(@NonNull View itemView) {
            super(itemView);
            nom = itemView.findViewById(R.id.item_nom_bar);
            adresse = itemView.findViewById(R.id.item_adresse_bar);
            rating = itemView.findViewById(R.id.item_rating_bar);
            ambiances = itemView.findViewById(R.id.item_ambiances_bar);
            commentaire = itemView.findViewById(R.id.item_commentaire_bar);
        }
    }
}