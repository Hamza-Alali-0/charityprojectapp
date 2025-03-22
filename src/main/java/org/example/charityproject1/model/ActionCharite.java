package org.example.charityproject1.model;

import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "actionsCharite")
public class ActionCharite {

    @Id
    private String idAction;

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotNull(message = "La date est obligatoire")
    private Date date;

    private Date datelimite;

    @NotBlank(message = "Le lieu est obligatoire")
    private String lieu;

    @Min(value = 1, message = "L'objectif de collecte doit être supérieur à 0")
    private float objectifCollecte;

    private float montantActuel = 0f;

    @NotBlank(message = "L'ID de l'organisation est obligatoire")
    private String organisationId;

    @NotBlank(message = "La catégorie est obligatoire")
    private String categorieId;
    // Add this property to your class
    private int nombreParticipants = 0;

    private List<String> mediaUrls;
    private List<String> likedByUsers;
    private List<String> listUsersContribue;
    private List<Don> listedons;
    private boolean active = true;
    private Date dateCreation;

    private CategorieAction categorie;
    private Organisations organisation;

    public ActionCharite() {
        this.likedByUsers = new ArrayList<>();
        this.listUsersContribue = new ArrayList<>();
        this.listedons = new ArrayList<>();
        this.mediaUrls = new ArrayList<>();
    }

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

    public Date getDatelimite() {
        return datelimite;
    }

    public void setDatelimite(Date datelimite) {
        this.datelimite = datelimite;
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

    public String getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(String organisationId) {
        this.organisationId = organisationId;
    }

    public String getCategorieId() {
        return categorieId;
    }

    public void setCategorieId(String categorieId) {
        this.categorieId = categorieId;
    }
    // Add these getter and setter methods
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

    public List<String> getLikedByUsers() {
        return likedByUsers;
    }

    public void setLikedByUsers(List<String> likedByUsers) {
        this.likedByUsers = likedByUsers;
    }

    public List<String> getListUsersContribue() {
        return listUsersContribue;
    }

    public void setListUsersContribue(List<String> listUsersContribue) {
        this.listUsersContribue = listUsersContribue;
    }

    public List<Don> getListedons() {
        return listedons;
    }

    public void setListedons(List<Don> listedons) {
        this.listedons = listedons;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public CategorieAction getCategorie() {
        return categorie;
    }

    public void setCategorie(CategorieAction categorie) {
        this.categorie = categorie;
    }

    public Organisations getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisations organisation) {
        this.organisation = organisation;
    }

    // Convenience methods for the view
    public float getMontantCollecte() {
        return montantActuel;
    }

    public void setMontantCollecte(float montantCollecte) {
        this.montantActuel = montantCollecte;
    }

    public int getLikesCount() {
        return likedByUsers != null ? likedByUsers.size() : 0;
    }

    public int getDonateursCount() {
        return listUsersContribue != null ? listUsersContribue.size() : 0;
    }

    @Override
    public String toString() {
        return "ActionCharite{" +
                "idAction='" + idAction + '\'' +
                ", titre='" + titre + '\'' +
                ", lieu='" + lieu + '\'' +
                ", objectifCollecte=" + objectifCollecte +
                ", montantActuel=" + montantActuel +
                ", organisationId='" + organisationId + '\'' +
                ", categorieId='" + categorieId + '\'' +
                ", active=" + active +
                '}';
    }
    // Add these methods to handle likes

    /**
     * Add a user like to this action
     * @param user The user who likes the action
     */
    public void ajouterLike(Utilisateurs user) {
        if (this.likedByUsers == null) {
            this.likedByUsers = new ArrayList<>();
        }

        // Add the user ID if not already present
        if (user != null && user.getUserId() != null && !this.likedByUsers.contains(user.getUserId())) {
            this.likedByUsers.add(user.getUserId());
        }
    }

    /**
     * Remove a user like from this action
     * @param userId The ID of the user to remove the like
     */
    public void supprimerLike(String userId) {
        if (this.likedByUsers != null && userId != null) {
            this.likedByUsers.remove(userId);
        }
    }
    public boolean isLikedByUser(String userId) {
        return likedByUsers != null && likedByUsers.contains(userId);
    }
}