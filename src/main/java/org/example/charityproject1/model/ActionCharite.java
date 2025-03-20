package org.example.charityproject1.model;


import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "actionsCharite")
public class ActionCharite {

    @Id
    private String idAction; // Utilisation de String pour la compatibilité avec MongoDB

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 100, message = "Le titre ne doit pas dépasser 100 caractères")
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
    private String description;

    @NotNull(message = "La date est obligatoire")
    private Date date;

    @NotBlank(message = "Le lieu est obligatoire")
    @Size(max = 100, message = "Le lieu ne doit pas dépasser 100 caractères")
    private String lieu;

    @Positive(message = "L'objectif de collecte doit être positif")
    private float objectifCollecte;

    @PositiveOrZero(message = "Le montant actuel ne peut pas être négatif")
    private float montantActuel;

    private Date datelimite;
    private int nombreParticipants; // Suivi du nombre de participants

    private List<String> mediaUrls; // URLs des médias (images, vidéos)

    @NotNull(message = "La catégorie est obligatoire")
    private String categorieId; // Référence à la catégorie
    private List<Utilisateurs> likedByUsers;

    private List<Utilisateurs> listUsersContribue;
    private List<Don> listedons;
    private String OrganisationId;
    // Getters et setters
    public String getIdAction() {
        return idAction;
    }

    public void setIdAction(String idAction) {
        this.idAction = idAction;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public float getObjectifCollecte() {
        return objectifCollecte;
    }

    public void setObjectifCollecte(float objectifCollecte) {
        this.objectifCollecte = objectifCollecte;
    }

    public float getMontantActuel() {
        return montantActuel;
    }

    public void setMontantActuel(float montantActuel) {
        this.montantActuel = montantActuel;
    }

    public Date getDatelimite() {return datelimite; }

    public void setDatelimite(Date datelimite) {this.datelimite = datelimite; }

    public int getNombreParticipants() {
        return nombreParticipants;
    }

    public void setNombreParticipants(int nombreParticipants) {
        this.nombreParticipants = nombreParticipants;
    }

    public List<String> getMediaUrls() {
        return mediaUrls;
    }

    public void setMediaUrls(List<String> mediaUrls) {
        this.mediaUrls = mediaUrls;
    }

    public String getCategorieId() {
        return categorieId;
    }

    public void setCategorieId(String categorieId) {
        this.categorieId = categorieId;
    }
    public List<Utilisateurs> getLikedByUsers() {
        return likedByUsers;
    }

    public void setLikedByUsers(List<Utilisateurs> likedByUsers) {
        this.likedByUsers = likedByUsers;
    }

    public List<Utilisateurs> getListUsersContribue() {
        return listUsersContribue;
    }

    public void setListUsersContribue(List<Utilisateurs> listUsersContribue) {
        this.listUsersContribue = listUsersContribue;
    }

    public List<Don> getListedons() {
        return listedons;
    }

    public void setListedons(List<Don> listedons) {
        this.listedons = listedons;
    }

    public String getOrganisationId() {
        return OrganisationId;
    }

    public void setOrganisationId(String organisationId) {
        OrganisationId = organisationId;
    }



    // Méthode pour ajouter un like
    public void ajouterLike(Utilisateurs userId) {
        if (!this.likedByUsers.contains(userId)) {
            this.likedByUsers.add(userId);
        }
    }

    // Méthode pour supprimer un like
    public void supprimerLike(String userId) {
        this.likedByUsers.remove(userId);
    }
}