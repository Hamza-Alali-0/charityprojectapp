package org.example.charityproject1.repository;

import org.example.charityproject1.model.ActionCharite;
import org.example.charityproject1.model.Utilisateurs;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;

public interface ActionChariteRepository extends MongoRepository<ActionCharite, String> {

    // Find actions by organization ID
    List<ActionCharite> findByOrganisationId(String OrganisationId);

    @Query("{ $and: [ { 'organisationId': ?1 }, { $or: [ { 'datelimite': { $gt: ?0 } }, { 'datelimite': null } ] } ] }")
    List<ActionCharite> findActiveActionsByOrganisation(Date now, String organisationId);

    @Query("{ 'datelimite' : { $lt: ?0 }, 'datelimite': { $ne: null }, 'organisationId': ?1 }")
    List<ActionCharite> findArchivedActionsByOrganisation(Date now, String organisationId);

    // Find actions by category
    List<ActionCharite> findByCategorieId(String categorieId);

    // Find popular actions (by number of likes)
    @Query(value = "{ }", sort = "{ 'likedByUsers': -1 }")
    List<ActionCharite> findPopularActions();


    @Query("{ $or: [ { 'datelimite': { $gt: ?0 } }, { 'datelimite': null } ] }")
    List<ActionCharite> findActiveActions(Date now);


}