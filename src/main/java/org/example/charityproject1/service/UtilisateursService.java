package org.example.charityproject1.service;

import org.example.charityproject1.model.Utilisateurs;
import org.example.charityproject1.repository.UtilisateursRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UtilisateursService {

    @Autowired
    private UtilisateursRepository utilisateursRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Register a new user
    public Utilisateurs registerUser(Utilisateurs utilisateur) {
        // Check if email is already used
        if (utilisateursRepository.existsByEmail(utilisateur.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        // Hash the password
        utilisateur.setPassword(passwordEncoder.encode(utilisateur.getPassword()));

        // Save the user
        return utilisateursRepository.save(utilisateur);
    }

    public Utilisateurs findByEmail(String email) {
        return utilisateursRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Utilisateurs findByemail(String email) {
        return utilisateursRepository.findByemail(email);  // Fetch user by email from repository
    }
    public void updateUser(Utilisateurs utilisateur) {
        // Update the user in the database
        utilisateursRepository.save(utilisateur);
    }
public Long countUsers() {
    return utilisateursRepository.count();
}

}