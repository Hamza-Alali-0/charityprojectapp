package org.example.charityproject1.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "organisations")
public class Organisations {

    @Id
    private String idOrganisation; 
    @NotBlank(message = "Le nom de l'organisation est obligatoire")
    @Size(max = 100, message = "Le nom de l'organisation ne doit pas dépasser 100 caractères")
    private String nom;

    @NotBlank(message = "L'adresse légale est obligatoire")
    @Size(max = 200, message = "L'adresse légale ne doit pas dépasser 200 caractères")
    private String adresseLegale;

    @NotBlank(message = "Le numéro d'identification est obligatoire")
    @Pattern(regexp = "^[A-Za-z0-9]{10}$", message = "Le numéro d'identification doit contenir 10 caractères alphanumériques")
    private String numeroIdentif;

    @NotBlank(message = "Le contact principal est obligatoire")
    @Size(max = 100, message = "Le contact principal ne doit pas dépasser 100 caractères")
    private String contactPrincipal;

    private String logo; // Store the file path or URL of the logo

    @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
    private String description;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @JsonIgnore // Hide password in API responses
    private String password;

    private boolean valideParAdmin; // Indicates if the profile is validated by a super-admin

    private List<ActionCharite> historiqueActionsCharite;

    private List<ActionCharite> archivedActionsCharite;
    // Getters and setters
    public String getIdOrganisation() {
        return idOrganisation;
    }

    public void setIdOrganisation(String idOrganisation) {
        this.idOrganisation = idOrganisation;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAdresseLegale() {
        return adresseLegale;
    }

    public void setAdresseLegale(String adresseLegale) {
        this.adresseLegale = adresseLegale;
    }

    public String getNumeroIdentif() {
        return numeroIdentif;
    }

    public void setNumeroIdentif(String numeroIdentif) {
        this.numeroIdentif = numeroIdentif;
    }

    public String getContactPrincipal() {
        return contactPrincipal;
    }

    public void setContactPrincipal(String contactPrincipal) {
        this.contactPrincipal = contactPrincipal;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getValideParAdmin() {
        return valideParAdmin;
    }

    public void setValideParAdmin(boolean validatedByAdmin) {
        this.valideParAdmin = validatedByAdmin;
    }

    public List<ActionCharite> getActionsCharite() {
        return historiqueActionsCharite;
    }

    public void setActionsCharite(List<ActionCharite> actionsCharite) {
        this.historiqueActionsCharite = actionsCharite;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isValideParAdmin() {
        return valideParAdmin;
    }

    public List<ActionCharite> getHistoriqueActionsCharite() {
        return historiqueActionsCharite;
    }

    public void setHistoriqueActionsCharite(List<ActionCharite> historiqueActionsCharite) {
        this.historiqueActionsCharite = historiqueActionsCharite;
    }

    public List<ActionCharite> getArchivedActionsCharite() {
        return archivedActionsCharite;
    }

    public void setArchivedActionsCharite(List<ActionCharite> archivedActionsCharite) {
        this.archivedActionsCharite = archivedActionsCharite;
    }
}