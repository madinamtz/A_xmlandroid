package com.example.demo;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "utilisateurs")
public class User {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String nom;
    public String email;
    public String password;

    @ColumnInfo(name = "date_naissance")
    public String dateNaissance;

    @ColumnInfo(name = "image_path")
    public String imagePath;

    // --- ICI LE CONSTRUCTEUR VIDE POUR FIREBASE ---
    public User() {
        // Laisser vide, c'est juste pour Firebase
    }
}