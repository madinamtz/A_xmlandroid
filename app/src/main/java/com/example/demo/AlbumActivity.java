package com.example.demo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Arrays;
import java.util.List;

public class AlbumActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album); // Tu devras créer ce XML

        String barNom = getIntent().getStringExtra("BAR_NOM");
        String pathsString = getIntent().getStringExtra("PHOTOS_PATHS");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Album : " + barNom);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (pathsString != null && !pathsString.isEmpty()) {
            // Découpe la String pour retrouver la liste des photos
            List<String> photoList = Arrays.asList(pathsString.split(","));

            RecyclerView rv = findViewById(R.id.recycler_view_album);
            // On affiche en grille de 2 colonnes
            rv.setLayoutManager(new GridLayoutManager(this, 2));

            PhotoAdapter adapter = new PhotoAdapter(photoList);
            rv.setAdapter(adapter);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}