package org.example.charityproject1.service;

import org.example.charityproject1.model.Organisations;
import org.example.charityproject1.model.Utilisateurs;
import org.example.charityproject1.repository.OrganisationsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganisationsService {

    @Autowired
    private OrganisationsRepository organisationsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Organisations registerOrganisation(Organisations organisation) {
        // Check if numeroIdentif is already used
        if (organisationsRepository.existsByNumeroIdentif(organisation.getNumeroIdentif())) {
            throw new RuntimeException("Numéro d'identification déjà utilisé");
        }

        // Validate other fields (e.g., password, email)
        if (organisation.getPassword() == null || organisation.getPassword().isEmpty()) {
            throw new RuntimeException("Le mot de passe est requis");
        }

        // Hash the password
        organisation.setPassword(passwordEncoder.encode(organisation.getPassword()));

        // Save the organization
        return organisationsRepository.save(organisation);
    }

    public Organisations authenticateOrganisation(String numeroIdentif, String password) {
        Organisations organisation = organisationsRepository.findByNumeroIdentif(numeroIdentif)
                .orElseThrow(() -> new RuntimeException("Numero Identifiant  non trouvé"));

        if (!passwordEncoder.matches(password, organisation.getPassword())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        return organisation;
    }
    // Find organization by numeroIdentif
    public Organisations findByNumeroIdentif(String numeroIdentif) {
        return organisationsRepository.findByNumeroIdentif(numeroIdentif)
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée"));
    }

    // Update the organization in the database
    public void updateOrganisation(Organisations organisation) {
        organisationsRepository.save(organisation);
    }

    // Get all organizations
    public List<Organisations> getAllOrganisations() {
        return organisationsRepository.findAll();
    }
    public Long countOrganisations() {
    return organisationsRepository.count();
}

}
