package org.example.charityproject1.service;

import org.example.charityproject1.model.ContactMessage;
import org.example.charityproject1.repository.ContactMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactMessageService {
    
    @Autowired
    private ContactMessageRepository contactMessageRepository;
    
    public ContactMessage saveContactMessage(ContactMessage message) {
        return contactMessageRepository.save(message);
    }
    
    public List<ContactMessage> getAllMessages() {
        return contactMessageRepository.findAll();
    }
    
    public ContactMessage getMessageById(String id) {
        return contactMessageRepository.findById(id).orElse(null);
    }
    
    public void markAsRead(String id) {
        ContactMessage message = getMessageById(id);
        if (message != null) {
            message.setRead(true);
            contactMessageRepository.save(message);
        }
    }
}