package org.example.charityproject1.repository;

import org.example.charityproject1.model.ContactMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactMessageRepository extends MongoRepository<ContactMessage, String> {
    // You can add custom query methods here if needed
}