package com.example.demo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;

public class AjoutBarActivity extends AppCompatActivity {
    private AppDatabase db;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajout_bar);

        // --- 1. CONFIGURATION DE LA BARRE DU HAUT ---
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Ajouter un bar");
        }

        // --- 2. INITIALISATION ---
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "app_database")
                .fallbackToDestructiveMigration()
                .build();

        userId = getIntent().getIntExtra("USER_ID", -1);

        EditText editNom = findViewById(R.id.nom_bar);
        EditText editAdresse = findViewById(R.id.adresse_bar);
        EditText editComm = findViewById(R.id.comm_bar);
        RatingBar ratingBar = findViewById(R.id.rating_bar);
        ChipGroup chipGroupAmbiance = findViewById(R.id.chip_group_ambiance);
        Button btnSave = findViewById(R.id.save_bar_btn);

        // --- 3. LOGIQUE LIMITE 3 AMBIANCES ---
        for (int i = 0; i < chipGroupAmbiance.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupAmbiance.getChildAt(i);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && chipGroupAmbiance.getCheckedChipIds().size() > 3) {
                    buttonView.setChecked(false); // On décoche si on dépasse 3
                    Toast.makeText(this, "3 ambiances maximum !", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // --- 4. SAUVEGARDE ---
        btnSave.setOnClickListener(v -> {
            String nom = editNom.getText().toString().trim();

            if (nom.isEmpty()) {
                Toast.makeText(this, "Le nom du bar est obligatoire", Toast.LENGTH_SHORT).show();
                return;
            }

            // Récupération des ambiances sélectionnées
            List<Integer> ids = chipGroupAmbiance.getCheckedChipIds();
            List<String> selectedAmbiances = new ArrayList<>();
            for (Integer id : ids) {
                Chip chip = findViewById(id);
                selectedAmbiances.add(chip.getText().toString());
            }
            // On transforme la liste en une seule chaîne (ex: "Chill, House, Techno")
            String ambiancesString = android.text.TextUtils.join(", ", selectedAmbiances);

            Bar nouveauBar = new Bar();
            nouveauBar.nom = nom;
            nouveauBar.adresse = editAdresse.getText().toString();
            nouveauBar.commentaire = editComm.getText().toString();
            nouveauBar.note = ratingBar.getRating();
            nouveauBar.utilisateurId = userId;
            nouveauBar.ambiances = ambiancesString; // On enregistre la nouvelle donnée !

            new Thread(() -> {
                db.barDao().inserer(nouveauBar);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Bar ajouté !", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }).start();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}