package org.example.charityproject1.repository;

import org.example.charityproject1.model.ActionCharite;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;

public interface ActionChariteRepository extends MongoRepository<ActionCharite, String> {

    // Find actions by organization ID
    List<ActionCharite> findByOrganisationId(String OrganisationId);

    // Find active actions (where date limite is in the future or date limite is null)
    @Query("{ $and: [ { 'OrganisationId': ?1 }, { $or: [ { 'datelimite': { $gt: ?0 } }, { 'datelimite': null } ] } ] }")
    List<ActionCharite> findActiveActionsByOrganisation(Date now, String organisationId);

    // Find archived actions (where date limite is in the past)
    @Query("{ 'datelimite' : { $lt: ?0 }, 'datelimite': { $ne: null }, 'OrganisationId': ?1 }")
    List<ActionCharite> findArchivedActionsByOrganisation(Date now, String organisationId);

    // Find actions by category
    List<ActionCharite> findByCategorieId(String categorieId);

    // Find popular actions (by number of likes)
    @Query(value = "{ }", sort = "{ 'likedByUsers': -1 }")
    List<ActionCharite> findPopularActions();
}