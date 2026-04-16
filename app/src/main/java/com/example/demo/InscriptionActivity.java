package com.example.demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.google.firebase.firestore.FirebaseFirestore; // IMPORT FIREBASE

import java.util.Calendar;

public class InscriptionActivity extends AppCompatActivity {

    private AppDatabase db;
    private FirebaseFirestore dbFirebase; // DÉCLARATION FIREBASE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscription);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialisation Room
        db = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "app_database"
        ).fallbackToDestructiveMigration().build();

        // INITIALISATION FIREBASE
        dbFirebase = FirebaseFirestore.getInstance();

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

            if (email.isEmpty() || pseudo.isEmpty() || dob.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                showError(error, "Veuillez remplir tous les champs");
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showError(error, "Format d'email invalide");
                return;
            }

            if (!dob.matches("^[0-9]{2}/[0-9]{2}/[0-9]{4}$")) {
                showError(error, "Date invalide (Format: JJ/MM/AAAA)");
                return;
            }

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

            if (!pass.equals(confirm)) {
                showError(error, "Les mots de passe ne correspondent pas");
                return;
            }

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

    private void creerNouvelUtilisateur(String email, String pseudo, String dob, String pass) {
        new Thread(() -> {
            // 1. Création de l'objet User
            User newUser = new User();
            newUser.email = email;
            newUser.nom = pseudo;
            newUser.dateNaissance = dob;
            newUser.password = pass;

            // 2. Sauvegarde locale (Room)
            db.utilisateurDao().inserer(newUser);

            // 3. SAUVEGARDE EN LIGNE (Firebase Firestore)
            dbFirebase.collection("users")
                    .document(email) // L'email sert d'identifiant unique sur le cloud
                    .set(newUser)
                    .addOnSuccessListener(aVoid -> Log.d("Firebase", "User ajouté avec succès !"))
                    .addOnFailureListener(e -> Log.e("Firebase", "Erreur d'ajout : " + e.getMessage()));

            runOnUiThread(() -> {
                Toast.makeText(InscriptionActivity.this, "Inscription réussie !", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(InscriptionActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }).start();
    }

    private void showError(TextView errorTv, String message) {
        errorTv.setText(message);
        errorTv.setVisibility(View.VISIBLE);
    }
}