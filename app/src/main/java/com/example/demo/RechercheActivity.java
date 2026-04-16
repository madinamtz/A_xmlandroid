package com.example.demo;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class RechercheActivity extends AppCompatActivity {

    private EditText searchInput;
    private ImageButton searchBtn;
    private ImageButton backBtn; // Nouveau bouton
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recherche);

        // On cache la barre du haut par défaut d'Android
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 1. Initialisation des vues
        searchInput = findViewById(R.id.search_input);
        searchBtn = findViewById(R.id.search_btn);
        backBtn = findViewById(R.id.btn_back_search); // On lie le bouton retour
        recyclerView = findViewById(R.id.rv_search_results);

        // 2. Initialisation Firebase et RecyclerView
        db = FirebaseFirestore.getInstance();
        adapter = new UserAdapter();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // --- ACTIONS ---

        // Action du bouton RETOUR
        backBtn.setOnClickListener(v -> {
            finish(); // Ferme cette page et revient à la Home
        });

        // Action du bouton RECHERCHE
        searchBtn.setOnClickListener(v -> {
            String text = searchInput.getText().toString().trim();
            if (!text.isEmpty()) {
                rechercherUtilisateur(text);
            } else {
                Toast.makeText(this, "Tape un pseudo !", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void rechercherUtilisateur(String pseudoTape) {
        db.collection("users")
                .whereEqualTo("nom", pseudoTape) // Recherche le pseudo EXACT
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // On transforme les résultats Firebase en liste d'objets User
                    List<User> resultats = queryDocumentSnapshots.toObjects(User.class);

                    if (resultats.isEmpty()) {
                        Toast.makeText(this, "Aucun utilisateur trouvé", Toast.LENGTH_SHORT).show();
                    }

                    adapter.setData(resultats);
                })
                .addOnFailureListener(e -> {
                    Log.e("SearchError", "Erreur : " + e.getMessage());
                    Toast.makeText(this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
                });
    }
}