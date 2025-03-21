package org.example.charityproject1.service;

import org.example.charityproject1.model.ActionCharite;
import org.example.charityproject1.model.Don;
import org.example.charityproject1.model.Utilisateurs;
import org.example.charityproject1.repository.ActionChariteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
public class ActionChariteService {

    @Autowired
    private ActionChariteRepository actionChariteRepository;

    public ActionCharite createActionCharite(ActionCharite actionCharite, List<MultipartFile> mediaFiles) throws IOException {
        // Initialize collections
        if (actionCharite.getLikedByUsers() == null) {
            actionCharite.setLikedByUsers(new ArrayList<>());
        }
        if (actionCharite.getListUsersContribue() == null) {
            actionCharite.setListUsersContribue(new ArrayList<>());
        }
        if (actionCharite.getListedons() == null) {
            actionCharite.setListedons(new ArrayList<>());
        }

        // Set initial values
        actionCharite.setMontantActuel(0);
        actionCharite.setNombreParticipants(0);
        actionCharite.setDateCreation(new Date());

        // Process media files
        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            List<String> mediaUrls = new ArrayList<>();

            for (MultipartFile file : mediaFiles) {
                if (!file.isEmpty()) {
                    // Convert to Base64 and store
                    byte[] fileContent = file.getBytes();
                    String encodedString = Base64.getEncoder().encodeToString(fileContent);
                    mediaUrls.add(encodedString);
                }
            }

            actionCharite.setMediaUrls(mediaUrls);
        }

        return actionChariteRepository.save(actionCharite);
    }

    public List<ActionCharite> getActionsByOrganisation(String organisationId) {
        return actionChariteRepository.findByOrganisationId(organisationId);
    }

    public List<ActionCharite> getActiveActionsByOrganisation(String organisationId) {
        Date now = new Date();
        return actionChariteRepository.findActiveActionsByOrganisation(now, organisationId);
    }

    public List<ActionCharite> getArchivedActionsByOrganisation(String organisationId) {
        Date now = new Date();
        return actionChariteRepository.findArchivedActionsByOrganisation(now, organisationId);
    }

    public ActionCharite getActionById(String actionId) {
        return actionChariteRepository.findById(actionId)
                .orElseThrow(() -> new RuntimeException("Action not found"));
    }

    public ActionCharite addLike(String actionId, Utilisateurs user) {
        ActionCharite action = getActionById(actionId);
        action.ajouterLike(user);
        return actionChariteRepository.save(action);
    }

    public ActionCharite removeLike(String actionId, String userId) {
        ActionCharite action = getActionById(actionId);
        action.supprimerLike(userId);
        return actionChariteRepository.save(action);
    }

    public ActionCharite addDonation(String actionId, Don don, Utilisateurs user) {
        ActionCharite action = getActionById(actionId);

        // Add donation
        if (action.getListedons() == null) {
            action.setListedons(new ArrayList<>());
        }
        action.getListedons().add(don);

        // Update amount
        float newAmount = action.getMontantActuel() + don.getMontant();
        action.setMontantActuel(newAmount);

        // Add user to contributors if not already present
        if (action.getListUsersContribue() == null) {
            action.setListUsersContribue(new ArrayList<>());
        }

        // FIX: Use userId (String) instead of user object
        if (!action.getListUsersContribue().contains(user.getUserId())) {
            action.getListUsersContribue().add(user.getUserId());
            action.setNombreParticipants(action.getNombreParticipants() + 1);
        }

        return actionChariteRepository.save(action);
    }
}