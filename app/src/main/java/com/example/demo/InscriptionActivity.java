package com.example.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import java.util.Calendar;

public class InscriptionActivity extends AppCompatActivity {

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscription);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "app_database"
        ).build();

        Button inscriptionBtn = findViewById(R.id.inscription_btn);
        EditText emailInput = findViewById(R.id.inscription_email);
        EditText pseudoInput = findViewById(R.id.inscription_pseudo);
        EditText dobInput = findViewById(R.id.inscription_dob);
        EditText passwordInput = findViewById(R.id.inscription_password);
        EditText confirmInput = findViewById(R.id.inscription_confirm);
        TextView error = findViewById(R.id.inscription_error);
        TextView goLogin = findViewById(R.id.go_login);

        goLogin.setOnClickListener(view -> finish());

        inscriptionBtn.setOnClickListener(view -> {
            String email = emailInput.getText().toString().trim();
            String pseudo = pseudoInput.getText().toString().trim();
            String dob = dobInput.getText().toString().trim();
            String pass = passwordInput.getText().toString();
            String confirm = confirmInput.getText().toString();

            // 1. Vérification des champs vides
            if (email.isEmpty() || pseudo.isEmpty() || dob.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                showError(error, "Veuillez remplir tous les champs");
                return;
            }

            // 2. Validation du format Email
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showError(error, "Format d'email invalide");
                return;
            }

            // 3. Validation de la Date (Format JJ/MM/AAAA)
            // Regex : 2 chiffres / 2 chiffres / 4 chiffres
            if (!dob.matches("^[0-9]{2}/[0-9]{2}/[0-9]{4}$")) {
                showError(error, "Date invalide (Format: JJ/MM/AAAA)");
                return;
            }

            // 4. Vérification logique de l'année (ex: pas avant 1900, pas dans le futur)
            try {
                int year = Integer.parseInt(dob.substring(6));
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                if (year < 1900 || year > currentYear) {
                    showError(error, "Année de naissance non valide");
                    return;
                }
            } catch (Exception e) {
                showError(error, "Erreur dans le format de la date");
                return;
            }

            // 5. Correspondance des mots de passe
            if (!pass.equals(confirm)) {
                showError(error, "Les mots de passe ne correspondent pas");
                return;
            }

            // Tout est OK -> On lance la vérification en base
            error.setVisibility(View.INVISIBLE);

            new Thread(() -> {
                User existant = db.utilisateurDao().verifierEmailExiste(email);

                runOnUiThread(() -> {
                    if (existant != null) {
                        showError(error, "Cet email est déjà utilisé");
                    } else {
                        creerNouvelUtilisateur(email, pseudo, dob, pass);
                    }
                });
            }).start();
        });
    }

    // Méthode pour extraire la création d'utilisateur et garder le code propre
    private void creerNouvelUtilisateur(String email, String pseudo, String dob, String pass) {
        new Thread(() -> {
            User newUser = new User();
            newUser.email = email;
            newUser.nom = pseudo;
            newUser.dateNaissance = dob;
            newUser.password = pass;

            db.utilisateurDao().inserer(newUser);

            runOnUiThread(() -> {
                Toast.makeText(InscriptionActivity.this, "Inscription réussie !", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(InscriptionActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }).start();
    }

    // Petite méthode pour afficher l'erreur
    private void showError(TextView errorTv, String message) {
        errorTv.setText(message);
        errorTv.setVisibility(View.VISIBLE);
    }
}