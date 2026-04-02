package com.example.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private AppDatabase db;
    private BarAdapter barAdapter;
    private SharedPreferences prefs;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        // 1. Initialisation de la Base de Données
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "app_database")
                .fallbackToDestructiveMigration()
                .build();

        // 2. Récupération de la session utilisateur
        prefs = getSharedPreferences("mon_app", Context.MODE_PRIVATE);
        userId = prefs.getInt("utilisateur_id", -1);
        String utilisateurNom = prefs.getString("utilisateur_connecte", "Anonyme");

        // 3. Liaison des composants UI (Vérifie bien les IDs dans ton activity_home.xml)
        TextView welcome = findViewById(R.id.welcome_text);
        Button logoutBtn = findViewById(R.id.logout_btn);
        Button profilBtn = findViewById(R.id.profil_btn);
        Button addBarBtn = findViewById(R.id.add_bar_btn);
        RecyclerView recyclerViewBars = findViewById(R.id.recycler_bars);

        welcome.setText("Bienvenue " + utilisateurNom);

        // --- ACTIONS DES BOUTONS ---

        // Ouvrir la page d'ajout de bar
        addBarBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AjoutBarActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        // Ouvrir le profil
        profilBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfilActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        });

        // Déconnexion
        logoutBtn.setOnClickListener(view -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear(); // Supprime l'ID et le nom pour fermer la session
            editor.apply();

            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // --- CONFIGURATION DE LA LISTE DES BARS ---

        // Initialisation de l'adapter avec la logique de clic (Affichage + Suppression)
        barAdapter = new BarAdapter(bar -> {
            // Sécurité pour le commentaire s'il est vide
            String comm = (bar.commentaire == null || bar.commentaire.isEmpty()) ? "Aucun commentaire." : bar.commentaire;

            String details = "Adresse : " + bar.adresse + "\n\n" +
                    "Commentaire : " + comm;

            new AlertDialog.Builder(HomeActivity.this)
                    .setTitle(bar.nom)
                    .setMessage(details)
                    .setPositiveButton("Supprimer", (dialog, which) -> {
                        new Thread(() -> {
                            db.barDao().supprimer(bar);
                            chargerMesBars(); // Rafraîchit la liste après suppression
                        }).start();
                    })
                    .setNegativeButton("Fermer", null)
                    .show();
        });

        // Setup du RecyclerView
        recyclerViewBars.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBars.setAdapter(barAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Rafraîchir le nom au cas où il a été changé dans le Profil
        String utilisateurMaj = prefs.getString("utilisateur_connecte", "Anonyme");
        TextView welcome = findViewById(R.id.welcome_text);
        welcome.setText("Bienvenue " + utilisateurMaj);

        // Recharger la liste des bars (utile quand on revient d'AjoutBarActivity)
        chargerMesBars();
    }

    private void chargerMesBars() {
        new Thread(() -> {
            // On récupère uniquement les bars appartenant à cet utilisateur
            List<Bar> mesBars = db.barDao().getBarsParUtilisateur(userId);
            runOnUiThread(() -> {
                if (barAdapter != null) {
                    barAdapter.setData(mesBars);
                }
            });
        }).start();
    }
}