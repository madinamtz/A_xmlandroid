package com.example.demo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
// On garde l'import de la Toolbar au cas où, mais on va privilégier la barre du thème
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

public class AjoutBarActivity extends AppCompatActivity {
    private AppDatabase db;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajout_bar);

        // --- ON SUPPRIME LES LIGNES TOOLBAR ---
        // Toolbar toolbar = findViewById(R.id.toolbar); <- SUPPRIMER
        // setSupportActionBar(toolbar); <- SUPPRIMER

        // On utilise uniquement la barre déjà présente grâce au thème
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Ajouter un bar");
        }

        // --- 2. INITIALISATION ---
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "app_database")
                .fallbackToDestructiveMigration() // Évite les crashs si tu modifies la structure de la DB
                .build();

        userId = getIntent().getIntExtra("USER_ID", -1);

        EditText editNom = findViewById(R.id.nom_bar);
        EditText editAdresse = findViewById(R.id.adresse_bar);
        EditText editComm = findViewById(R.id.comm_bar);
        RatingBar ratingBar = findViewById(R.id.rating_bar);
        Button btnSave = findViewById(R.id.save_bar_btn);

        // --- 3. SAUVEGARDE ---
        btnSave.setOnClickListener(v -> {
            String nom = editNom.getText().toString().trim();

            if (nom.isEmpty()) {
                Toast.makeText(this, "Le nom du bar est obligatoire", Toast.LENGTH_SHORT).show();
                return;
            }

            Bar nouveauBar = new Bar();
            nouveauBar.nom = nom;
            nouveauBar.adresse = editAdresse.getText().toString();
            nouveauBar.commentaire = editComm.getText().toString();
            nouveauBar.note = ratingBar.getRating();
            nouveauBar.utilisateurId = userId;

            new Thread(() -> {
                db.barDao().inserer(nouveauBar);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Bar ajouté !", Toast.LENGTH_SHORT).show();
                    finish(); // Ferme l'activité et revient à HomeActivity
                });
            }).start();
        });
    }

    // --- 4. ACTION DE LA FLÈCHE RETOUR ---
    @Override
    public boolean onSupportNavigateUp() {
        // Cette méthode est appelée quand on clique sur la flèche en haut à gauche
        finish();
        return true;
    }
}