package org.example.charityproject1.repository;

import org.example.charityproject1.model.Organisations;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface OrganisationsRepository extends MongoRepository<Organisations, String> {

    // Find organisation by numeroIdentif (unique identifier)
    Optional<Organisations> findByNumeroIdentif(String numeroIdentif);

    // Find organisations by legal address (adresseLegale)
    List<Organisations> findByAdresseLegale(String adresseLegale);

    // Check if a numeroIdentif is already used
    boolean existsByNumeroIdentif(String numeroIdentif);

    // Find organisations that have at least one charity action
    @Query("{ 'historiqueActionsCharite' : { $exists: true, $not: { $size: 0 } } }")
    List<Organisations> findOrganisationsWithCharityActions();

    // Find organisations by name containing a specific string (case insensitive) with pagination
    Page<Organisations> findByNomContainingIgnoreCase(String nom, Pageable pageable);

    // Find all organisations with pagination
    Page<Organisations> findAll(Pageable pageable);
}
