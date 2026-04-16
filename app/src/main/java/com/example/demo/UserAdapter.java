package com.example.demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList = new ArrayList<>();

    public void setData(List<User> users) {
        this.userList = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User targetUser = userList.get(position);
        holder.name.setText(targetUser.nom);

        // --- LOGIQUE D'AJOUT D'AMI ---
        holder.btnAdd.setOnClickListener(v -> {
            Context context = v.getContext();

            // 1. Récupérer MON email (celui connecté) depuis les SharedPreferences
            // Correction ici : on utilise la clé "utilisateur_email"
            SharedPreferences prefs = context.getSharedPreferences("mon_app", Context.MODE_PRIVATE);
            String monEmail = prefs.getString("utilisateur_email", "");

            // 2. Sécurité : Empêcher de s'ajouter soi-même
            if (monEmail.equals(targetUser.email)) {
                Toast.makeText(context, "Vous ne pouvez pas vous ajouter vous-même !", Toast.LENGTH_SHORT).show();
                return;
            }

            // Sécurité supplémentaire si l'email n'a pas été trouvé dans les prefs
            if (monEmail.isEmpty()) {
                Toast.makeText(context, "Erreur : Session utilisateur introuvable", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3. Préparer la donnée pour Firebase
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> demande = new HashMap<>();
            demande.put("senderEmail", monEmail);
            demande.put("receiverEmail", targetUser.email);
            demande.put("status", "pending"); // En attente

            // Création d'un ID unique pour la demande (ex: moi_lui)
            String docId = monEmail + "_" + targetUser.email;

            // 4. Envoi à Firestore
            db.collection("friend_requests")
                    .document(docId)
                    .set(demande)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Demande envoyée à " + targetUser.nom, Toast.LENGTH_SHORT).show();
                        holder.btnAdd.setText("Envoyé");
                        holder.btnAdd.setEnabled(false); // Désactive le bouton après envoi
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        Button btnAdd;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.user_name);
            btnAdd = itemView.findViewById(R.id.btn_add_friend);
        }
    }
}