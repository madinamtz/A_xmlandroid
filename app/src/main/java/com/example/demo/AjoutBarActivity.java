package com.example.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;

public class AjoutBarActivity extends AppCompatActivity {
    private AppDatabase db;
    private int userId;

    // Liste pour stocker les URIs des photos sélectionnées
    private List<String> barPhotosPaths = new ArrayList<>();

    // Outil pour ouvrir la galerie et récupérer les photos une par une
    private final ActivityResultLauncher<String> getBarPhoto = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        // Demander la permission
                        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        barPhotosPaths.add(uri.toString());
                        Toast.makeText(this, "Photo ajoutée à l'album (" + barPhotosPaths.size() + ")", Toast.LENGTH_SHORT).show();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erreur de permission image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajout_bar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Ajouter un bar");
        }

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

        // --- NOUVEAU : BOUTON POUR AJOUTER UNE PHOTO ---
        Button btnAddPhoto = findViewById(R.id.btn_add_photo);
        btnAddPhoto.setOnClickListener(v -> getBarPhoto.launch("image/*"));

        for (int i = 0; i < chipGroupAmbiance.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupAmbiance.getChildAt(i);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && chipGroupAmbiance.getCheckedChipIds().size() > 3) {
                    buttonView.setChecked(false);
                    Toast.makeText(this, "3 ambiances maximum !", Toast.LENGTH_SHORT).show();
                }
            });
        }

        btnSave.setOnClickListener(v -> {
            String nom = editNom.getText().toString().trim();

            if (nom.isEmpty()) {
                Toast.makeText(this, "Le nom du bar est obligatoire", Toast.LENGTH_SHORT).show();
                return;
            }

            List<Integer> ids = chipGroupAmbiance.getCheckedChipIds();
            List<String> selectedAmbiances = new ArrayList<>();
            for (Integer id : ids) {
                Chip chip = findViewById(id);
                selectedAmbiances.add(chip.getText().toString());
            }
            String ambiancesString = android.text.TextUtils.join(", ", selectedAmbiances);

            // CONVERSION DE LA LISTE DE PHOTOS EN STRING ---
            String allPhotosString = android.text.TextUtils.join(",", barPhotosPaths);

            Bar nouveauBar = new Bar();
            nouveauBar.nom = nom;
            nouveauBar.adresse = editAdresse.getText().toString();
            nouveauBar.commentaire = editComm.getText().toString();
            nouveauBar.note = ratingBar.getRating();
            nouveauBar.utilisateurId = userId;
            nouveauBar.ambiances = ambiancesString;
            nouveauBar.photosPaths = allPhotosString; // On enregistre la chaîne d'URIs

            new Thread(() -> {
                db.barDao().inserer(nouveauBar);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Bar ajouté avec son album !", Toast.LENGTH_SHORT).show();
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