package org.example.charityproject1.model;


import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "dons")
public class Don{

    @Id
    private String idDon; 
    @Positive(message = "Le montant doit être positif")
    private float montant;

    @NotNull(message = "La date est obligatoire")
    private Date date;

    @NotBlank(message = "L'ID de l'utilisateur est obligatoire")
    private String utilisateurId;

    @NotBlank(message = "L'ID de l'action est obligatoire")
    private String actionId;

    // Getters et setters
    public String getIdDon() {
        return idDon;
    }

    public void setIdDon(String idDon) {
        this.idDon = idDon;
    }

    public float getMontant() {
        return montant;
    }

    public void setMontant(float montant) {
        this.montant = montant;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(String utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    // Méthode pour suivre la progression des dons
    public void suivreProgression(ActionCharite action) {
        action.setMontantActuel(action.getMontantActuel() + this.montant);
    }
}
