package com.example.demo;

public class FriendRequest {
    // Ces noms doivent être EXACTEMENT les mêmes que dans ta base Firebase
    public String senderEmail;
    public String receiverEmail;
    public String status;
    private String documentId; // Pour pouvoir modifier le document plus tard

    // Constructeur vide OBLIGATOIRE pour Firebase
    public FriendRequest() {
    }

    // Getter et Setter pour le documentId (on s'en sert pour Accepter/Refuser)
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}