package org.example.charityproject1.model;


import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "categories")
public class CategorieAction {

    @Id
    private String idCategorie; // Utilisation de String pour la compatibilité avec MongoDB

    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    @Size(max = 50, message = "Le nom de la catégorie ne doit pas dépasser 50 caractères")
    private String nom;

    // Getters et setters
    public String getIdCategorie() {
        return idCategorie;
    }

    public void setIdCategorie(String idCategorie) {
        this.idCategorie = idCategorie;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
}