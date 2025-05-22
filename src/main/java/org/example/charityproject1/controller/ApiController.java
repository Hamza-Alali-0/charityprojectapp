package org.example.charityproject1.controller;

import org.example.charityproject1.model.Organisations;
import org.example.charityproject1.repository.OrganisationsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private OrganisationsRepository organisationsRepository;

    @GetMapping("/organization/{id}")
    public ResponseEntity<Organisations> getOrganizationDetails(@PathVariable String id) {
        Optional<Organisations> organization = organisationsRepository.findByNumeroIdentif(id);
        
        if (organization.isPresent()) {
            return ResponseEntity.ok(organization.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}