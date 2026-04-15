package com.example.demo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns; // Import pour la validation email
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

public class ProfilActivity extends AppCompatActivity {

    private AppDatabase db;
    private int userId;
    private String currentPhotoPath = "";
    private ImageView profileImg;

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                    try {
                        getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                    currentPhotoPath = uri.toString();
                    profileImg.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "app_database").build();
        userId = getIntent().getIntExtra("USER_ID", -1);

        // --- LIAISON DES COMPOSANTS ---
        EditText editEmail = findViewById(R.id.edit_email); // Nouveau champ Email
        EditText editPseudo = findViewById(R.id.edit_pseudo);
        EditText editDob = findViewById(R.id.edit_dob);
        EditText editOldPass = findViewById(R.id.edit_old_password);
        EditText editNewPass = findViewById(R.id.edit_new_password);
        Button saveBtn = findViewById(R.id.save_btn);
        profileImg = findViewById(R.id.profil_image);

        profileImg.setOnClickListener(v -> mGetContent.launch("image/*"));

        // 1. CHARGER LES DONNÉES ACTUELLES
        new Thread(() -> {
            User user = db.utilisateurDao().getUserById(userId);
            if (user != null) {
                runOnUiThread(() -> {
                    editEmail.setText(user.email);
                    editPseudo.setText(user.nom);
                    editDob.setText(user.dateNaissance);
                    if (user.imagePath != null && !user.imagePath.isEmpty()) {
                        currentPhotoPath = user.imagePath;
                        profileImg.setImageURI(Uri.parse(user.imagePath));
                    }
                });
            }
        }).start();

        // 2. SAUVEGARDER LES MODIFICATIONS
        saveBtn.setOnClickListener(v -> {
            String newEmail = editEmail.getText().toString().trim();
            String newPseudo = editPseudo.getText().toString().trim();
            String newDob = editDob.getText().toString().trim();
            String oldPassInput = editOldPass.getText().toString().trim();
            String newPassInput = editNewPass.getText().toString().trim();

            // Validation de base
            if (newEmail.isEmpty() || newPseudo.isEmpty() || newDob.isEmpty()) {
                Toast.makeText(this, "Email, Pseudo et Date obligatoires", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validation format Email
            if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                Toast.makeText(this, "Format d'email invalide", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validation Format Date (JJ/MM/AAAA)
            if (!newDob.matches("^[0-9]{2}/[0-9]{2}/[0-9]{4}$")) {
                Toast.makeText(this, "Format date invalide (JJ/MM/AAAA)", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                User user = db.utilisateurDao().getUserById(userId);
                if (user != null) {

                    // --- VÉRIFICATION SI L'EMAIL A CHANGÉ ---
                    if (!user.email.equalsIgnoreCase(newEmail)) {
                        User doublon = db.utilisateurDao().verifierEmailExiste(newEmail);
                        if (doublon != null) {
                            runOnUiThread(() -> Toast.makeText(this, "Cet email est déjà utilisé", Toast.LENGTH_SHORT).show());
                            return;
                        }
                    }

                    // --- LOGIQUE CHANGEMENT MOT DE PASSE ---
                    if (!oldPassInput.isEmpty() || !newPassInput.isEmpty()) {
                        if (!user.password.equals(oldPassInput)) {
                            runOnUiThread(() -> Toast.makeText(this, "Ancien mot de passe incorrect", Toast.LENGTH_SHORT).show());
                            return;
                        }
                        if (newPassInput.length() < 4) {
                            runOnUiThread(() -> Toast.makeText(this, "Nouveau mot de passe trop court", Toast.LENGTH_SHORT).show());
                            return;
                        }
                        user.password = newPassInput;
                    }

                    // Mise à jour de l'objet User
                    user.email = newEmail;
                    user.nom = newPseudo;
                    user.dateNaissance = newDob;
                    user.imagePath = currentPhotoPath;

                    db.utilisateurDao().modifier(user);

                    // Mise à jour de la session SharedPreferences
                    SharedPreferences.Editor editor = getSharedPreferences("mon_app", MODE_PRIVATE).edit();
                    editor.putString("utilisateur_connecte", newPseudo);
                    editor.putString("utilisateur_email", newEmail);
                    editor.apply();

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Profil mis à jour !", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            }).start();
        });
    }
}