package org.example.charityproject1.repository;

import org.example.charityproject1.model.Utilisateurs;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UtilisateursRepository extends MongoRepository<Utilisateurs, String> {
    // Find user by email
    Optional<Utilisateurs> findByEmail(String email);
    Utilisateurs findByemail(String email);
    // Find users by location
    List<Utilisateurs> findByLocalisation(String localisation);

    // Vérifier si un email est déjà utilisé
    boolean existsByEmail(String email);

    // 🔹 Find users who made at least one donation
    @Query("{ 'historiqueDons' : { $exists: true, $not: { $size: 0 } } }")
    List<Utilisateurs> findUsersWithDonations();
    List<Utilisateurs> findByLikedActionsContaining(String actionId);
}