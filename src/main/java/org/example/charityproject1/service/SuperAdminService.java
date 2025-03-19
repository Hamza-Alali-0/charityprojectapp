package org.example.charityproject1.service;

import org.example.charityproject1.model.Organisations;
import org.example.charityproject1.model.Utilisateurs;
import org.example.charityproject1.repository.OrganisationsRepository;
import org.example.charityproject1.repository.UtilisateursRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SuperAdminService {

    @Autowired
    private OrganisationsRepository organisationsRepository;
    @Autowired
    private UtilisateursRepository utilisateursRepository;
    public void validateOrganisation(String id) {
        System.out.println("Finding organization with ID: " + id);

        // Get all orgs for debugging
        List<Organisations> allOrgs = organisationsRepository.findAll();
        System.out.println("Total orgs in DB: " + allOrgs.size());
        allOrgs.forEach(org -> System.out.println("DB org ID: " + org.getNumeroIdentif()));

        // Try multiple ways to find the organization
        Optional<Organisations> orgOptional = organisationsRepository.findByNumeroIdentif(id);

        if (!orgOptional.isPresent()) {
            // If not found by numeroIdentif, try by MongoDB _id
            try {
                orgOptional = organisationsRepository.findById(id);
            } catch (Exception e) {
                System.out.println("Error finding by _id: " + e.getMessage());
            }
        }

        if (orgOptional.isPresent()) {
            Organisations org = orgOptional.get();
            System.out.println("Found organization: " + org.getNom());
            org.setValideParAdmin(true);
            organisationsRepository.save(org);
            System.out.println("Organization validated successfully");
        } else {
            throw new RuntimeException("Organisation not found with ID: " + id);
        }
    }

    public void deleteOrganisation(String id) {
        System.out.println("Attempting to delete organisation with ID: " + id);

        // Try different approaches to find and delete
        try {
            // First try to find by numeroIdentif
            Optional<Organisations> orgOptional = organisationsRepository.findByNumeroIdentif(id);

            if (orgOptional.isPresent()) {
                Organisations org = orgOptional.get();
                System.out.println("Found organization by numeroIdentif: " + org.getNom());

                // Delete by the MongoDB ID
                organisationsRepository.deleteById(org.getIdOrganisation());
                System.out.println("Organization deleted by ID: " + org.getIdOrganisation());
            } else {
                // If not found by numeroIdentif, try by MongoDB _id directly
                try {
                    organisationsRepository.deleteById(id);
                    System.out.println("Organization deleted directly by ID: " + id);
                } catch (Exception e) {
                    System.err.println("Failed to delete by ID: " + e.getMessage());
                    throw new RuntimeException("Organisation not found with ID: " + id);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in deleteOrganisation: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete organisation: " + e.getMessage());
        }

        // Verify deletion
        try {
            Optional<Organisations> checkDeleted = organisationsRepository.findById(id);
            if (checkDeleted.isPresent()) {
                System.err.println("WARNING: Organization still exists after deletion attempt!");
            } else {
                System.out.println("Verified: Organization no longer exists in database");
            }
        } catch (Exception e) {
            // Ignore verification errors
        }
    }

    // Get all organisations
    public List<Organisations> getAllOrganisations() {
        return organisationsRepository.findAll();
    }
    public Optional<Organisations> getOrganisationById(String id) {
        return organisationsRepository.findByNumeroIdentif(id);
    }
    /**
     * Get all users in the system
     */
    public List<Utilisateurs> getAllUtilisateurs() {
        return utilisateursRepository.findAll();
    }

    /**
     * Get a specific user by ID
     */
    public Utilisateurs getUtilisateurById(String id) {
        return utilisateursRepository.findById(id).orElse(null);
    }

    /**
     * Delete a user by ID
     */
    public void deleteUtilisateur(String id) {
        utilisateursRepository.deleteById(id);
    }

    public Utilisateurs updateUtilisateur(Utilisateurs utilisateur) {
        return utilisateursRepository.save(utilisateur);}

}