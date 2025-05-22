package org.example.charityproject1.repository;

import org.example.charityproject1.model.Don;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface DonRepository extends MongoRepository<Don, String> {
    // Find all donations by user ID
    List<Don> findByUtilisateurId(String utilisateurId);

    // Find all donations for a specific action
    List<Don> findByActionId(String actionId);

    @Query(value = "{ }", fields = "{ 'amount' : 1 }")
    List<Don> findAllAmounts();

    // Replace the SQL query with a MongoDB aggregation
    default double sumTotalDonations() {
        List<Don> donations = findAll();
        return donations.stream()
            .mapToDouble(Don::getMontant) // Assuming the amount field is called "montant"
            .sum();
    }
}