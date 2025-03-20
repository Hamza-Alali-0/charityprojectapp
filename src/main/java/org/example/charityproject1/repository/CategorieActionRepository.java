package org.example.charityproject1.repository;

import org.example.charityproject1.model.CategorieAction;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategorieActionRepository extends MongoRepository<CategorieAction, String> {
    // Find category by name (case-insensitive search)
    CategorieAction findByNom(String nom);
}