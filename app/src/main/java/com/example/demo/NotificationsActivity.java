package com.example.demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RequestAdapter adapter; // On va le créer juste après
    private TextView emptyText;
    private FirebaseFirestore db;
    private String monEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 1. Initialisation
        db = FirebaseFirestore.getInstance();
        SharedPreferences prefs = getSharedPreferences("mon_app", Context.MODE_PRIVATE);
        monEmail = prefs.getString("utilisateur_email", "");

        recyclerView = findViewById(R.id.rv_friend_requests);
        emptyText = findViewById(R.id.tv_empty_requests);
        ImageButton btnBack = findViewById(R.id.btn_back_notif);

        btnBack.setOnClickListener(v -> finish());

        // 2. Configuration du RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RequestAdapter();
        recyclerView.setAdapter(adapter);

        // 3. Charger les demandes
        chargerDemandes();
    }

    private void chargerDemandes() {
        // On cherche les demandes "en attente" pour MOI
        db.collection("friend_requests")
                .whereEqualTo("receiverEmail", monEmail)
                .whereEqualTo("status", "pending")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<FriendRequest> listeDemandes = new ArrayList<>();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            // On transforme le document en objet Java
                            FriendRequest fr = doc.toObject(FriendRequest.class);
                            fr.setDocumentId(doc.getId()); // On garde l'ID pour pouvoir accepter/refuser
                            listeDemandes.add(fr);
                        }
                    }

                    // Mise à jour de l'affichage
                    if (listeDemandes.isEmpty()) {
                        emptyText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.setData(listeDemandes);
                    }
                });
    }
}