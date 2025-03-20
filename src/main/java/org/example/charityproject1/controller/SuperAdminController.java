package org.example.charityproject1.controller;

import jakarta.servlet.http.HttpSession;
import org.example.charityproject1.model.Organisations;
import org.example.charityproject1.model.SuperAdmin;
import org.example.charityproject1.model.Utilisateurs;
import org.example.charityproject1.repository.SuperAdminRepository;
import org.example.charityproject1.service.SuperAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Controller
@RequestMapping("/superadmin")
public class SuperAdminController {

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private SuperAdminService superAdminService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        // Check if admin is logged in
        String adminEmail = (String) session.getAttribute("admin_email");
        if (adminEmail == null) {
            return "redirect:/auth/login/superadmin";
        }

        // Get admin details
        Optional<SuperAdmin> adminOptional = superAdminRepository.findByEmail(adminEmail);
        if (!adminOptional.isPresent()) {
            session.invalidate();
            return "redirect:/auth/login/superadmin";
        }

        // Add admin to model
        model.addAttribute("admin", adminOptional.get());

        // Get all organisations and add to model
        List<Organisations> organisations = superAdminService.getAllOrganisations();
        model.addAttribute("organisations", organisations);

        return "superadmin/dashboard";
    }
    // REST API endpoints for future implementation

    @PostMapping("/api/organisations/{id}/validate")
    @ResponseBody
    public ResponseEntity<?> validateOrganisation(@PathVariable String id) {
        try {
            // Add debug logging
            System.out.println("Attempting to validate organization with ID: " + id);

            superAdminService.validateOrganisation(id);

            // Return success response
            return ResponseEntity.ok().body(Map.of("status", "success", "message", "Organisation validée avec succès"));
        } catch (Exception e) {
            // Log the error
            System.err.println("Error validating organization: " + e.getMessage());
            e.printStackTrace();

            // Return error response
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    // Delete an organisation
    @DeleteMapping("/api/organisations/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteOrganisation(@PathVariable String id) {
        superAdminService.deleteOrganisation(id);
        return ResponseEntity.ok("Organisation deleted successfully");
    }

    // Get all organisations
    @GetMapping("/api/organisations")
    @ResponseBody
    public ResponseEntity<List<Organisations>> getAllOrganisations() {
        List<Organisations> organisations = superAdminService.getAllOrganisations();
        return ResponseEntity.ok(organisations);
    }

    // View all organisations in a web page
    @GetMapping("/organisations")
    public String viewAllOrganisations(HttpSession session, Model model) {
        // Check if admin is logged in
        String adminEmail = (String) session.getAttribute("admin_email");
        if (adminEmail == null) {
            return "redirect:/auth/login/superadmin";
        }

        // Get admin details for the navbar/header
        Optional<SuperAdmin> adminOptional = superAdminRepository.findByEmail(adminEmail);
        if (!adminOptional.isPresent()) {
            session.invalidate();
            return "redirect:/auth/login/superadmin";
        }

        // Add admin to model
        model.addAttribute("admin", adminOptional.get());

        // Get all organisations and add to model
        List<Organisations> organisations = superAdminService.getAllOrganisations();
        model.addAttribute("organisations", organisations);

        return "superadmin/organisations/organisations";
    }

    // Charities page
    @GetMapping("/charities")
    public String viewCharities(HttpSession session, Model model) {
        // Check if admin is logged in
        String adminEmail = (String) session.getAttribute("admin_email");
        if (adminEmail == null) {
            return "redirect:/auth/login/superadmin";
        }

        // Get admin details
        Optional<SuperAdmin> adminOptional = superAdminRepository.findByEmail(adminEmail);
        if (!adminOptional.isPresent()) {
            session.invalidate();
            return "redirect:/auth/login/superadmin";
        }

        // Add admin to model
        model.addAttribute("admin", adminOptional.get());

        return "superadmin/charities";
    }

    @GetMapping("/organisations-content")
    public String getOrganisationsContent(HttpSession session, Model model) {
        // Check if admin is logged in
        String adminEmail = (String) session.getAttribute("admin_email");
        if (adminEmail == null) {
            return "redirect:/auth/login/superadmin";
        }

        // Get admin details
        Optional<SuperAdmin> adminOptional = superAdminRepository.findByEmail(adminEmail);
        if (!adminOptional.isPresent()) {
            session.invalidate();
            return "redirect:/auth/login/superadmin";
        }

        // Add admin to model
        model.addAttribute("admin", adminOptional.get());

        // Get all organisations and add to model
        List<Organisations> organisations = superAdminService.getAllOrganisations();
        model.addAttribute("organisations", organisations);

        // Return only the fragment containing organizations content
        return "superadmin/organisations/organisations :: organisations-content";
    }
    // Get organization details by ID
    // Update this existing method with a complete implementation
    @GetMapping("/api/organisations/{id}")
    @ResponseBody
    public ResponseEntity<?> getOrganisationDetails(@PathVariable String id) {
        try {
            // Find the organization by ID
            Optional<Organisations> organisation = superAdminService.getOrganisationById(id);

            if (organisation.isPresent()) {
                return ResponseEntity.ok(organisation.get());
            } else {
                return ResponseEntity.status(404)
                        .body(Map.of("status", "error", "message", "Organisation non trouvée"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @GetMapping("/utilisateurs-content")
    public String getUtilisateursContent(HttpSession session, Model model) {
        // Check if admin is logged in
        String adminEmail = (String) session.getAttribute("admin_email");
        if (adminEmail == null) {
            return "redirect:/login";
        }

        // Get all users and add to model
        List<Utilisateurs> utilisateurs = superAdminService.getAllUtilisateurs();
        model.addAttribute("utilisateurs", utilisateurs);

        return "superadmin/utilisateurs/utilisateurs :: utilisateurs-content";
    }

    // Get user by ID
// Add this method to your SuperAdminController class
    @GetMapping("/api/utilisateurs/{id}")
    @ResponseBody
    public ResponseEntity<?> getUtilisateur(@PathVariable String id) {
        try {
            Utilisateurs utilisateur = superAdminService.getUtilisateurById(id);
            if (utilisateur == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(utilisateur);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving user: " + e.getMessage()));
        }
    }
// Add this method to your SuperAdminController class



    @PutMapping("/api/utilisateurs/{id}")
    @ResponseBody
    public ResponseEntity<?> updateUtilisateur(@PathVariable String id,
                                               @RequestParam("nom") String nom,
                                               @RequestParam("email") String email,
                                               @RequestParam("originalEmail") String originalEmail,
                                               @RequestParam("telephone") String telephone,
                                               @RequestParam("localisation") String localisation,
                                               @RequestParam(value = "logoFile", required = false) MultipartFile logoFile) {
        try {
            // Field validation
            Map<String, String> fieldErrors = new HashMap<>();

            if (nom == null || nom.isEmpty()) {
                fieldErrors.put("nom", "Le nom est obligatoire");
            }

            if (email == null || email.isEmpty()) {
                fieldErrors.put("email", "L'email est obligatoire");
            }

            if (telephone == null || !telephone.matches("^[0-9]{10}$")) {
                fieldErrors.put("telephone", "Le téléphone doit contenir 10 chiffres");
            }

            if (localisation == null || localisation.isEmpty()) {
                fieldErrors.put("localisation", "La localisation est obligatoire");
            }

            // Email changed? Check if new email is already used
            if (!email.equals(originalEmail)) {
                System.out.println("Email changed from " + originalEmail + " to " + email);
                // Check if email exists for another user
                if (superAdminService.isEmailInUse(email)) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("error", "EMAIL_EXISTS");
                    response.put("message", "Cet email est déjà utilisé par un autre utilisateur");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }

            if (!fieldErrors.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("fieldErrors", fieldErrors);
                response.put("message", "Veuillez corriger les erreurs dans le formulaire");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Get the current user
            Utilisateurs utilisateur = superAdminService.getUtilisateurById(id);
            if (utilisateur == null) {
                return ResponseEntity.notFound().build();
            }

            // Update user fields
            utilisateur.setNom(nom);
            utilisateur.setEmail(email);
            utilisateur.setTelephone(telephone);
            utilisateur.setLocalisation(localisation);

            // Update logo if provided
            if (logoFile != null && !logoFile.isEmpty()) {
                try {
                    // Convert the MultipartFile to a Base64 string
                    byte[] logoBytes = logoFile.getBytes();
                    String base64Logo = Base64.getEncoder().encodeToString(logoBytes);
                    utilisateur.setLogoPath(base64Logo);
                } catch (IOException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("message", "Error processing the logo: " + e.getMessage()));
                }
            }

            // Save the updated user
            utilisateur = superAdminService.updateUtilisateur(utilisateur);

            return ResponseEntity.ok(utilisateur);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error updating user: " + e.getMessage()));
        }
    }
    // Delete a user
    @DeleteMapping("/api/utilisateurs/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteUtilisateur(@PathVariable String id) {
        try {
            superAdminService.deleteUtilisateur(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting user: " + e.getMessage());
        }
    }
    // Update profile
    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam("nom") String nom,
                                @RequestParam("email") String email,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        String adminEmail = (String) session.getAttribute("admin_email");
        if (adminEmail == null) {
            return "redirect:/auth/login/superadmin";
        }

        Optional<SuperAdmin> adminOptional = superAdminRepository.findByEmail(adminEmail);
        if (!adminOptional.isPresent()) {
            session.invalidate();
            return "redirect:/auth/login/superadmin";
        }

        // Validate form data
        if (nom == null || nom.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("profileError", "name-invalid");
            redirectAttributes.addFlashAttribute("profileErrorMessage", "Le nom est obligatoire.");
            return "redirect:/superadmin/dashboard";
        }

        // Validate email
        if (email == null || email.trim().isEmpty() || !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            redirectAttributes.addFlashAttribute("profileError", "email-invalid");
            redirectAttributes.addFlashAttribute("profileErrorMessage", "Veuillez saisir un email valide.");
            return "redirect:/superadmin/dashboard";
        }

        SuperAdmin admin = adminOptional.get();

        // Check if email changed and is unique
        if (!email.equals(adminEmail)) {
            Optional<SuperAdmin> existingAdmin = superAdminRepository.findByEmail(email);
            if (existingAdmin.isPresent() && !existingAdmin.get().getIdAdmin().equals(admin.getIdAdmin())) {
                redirectAttributes.addFlashAttribute("profileError", "email-exists");
                redirectAttributes.addFlashAttribute("profileErrorMessage", "Cet email est déjà utilisé.");
                return "redirect:/superadmin/dashboard";
            }

            // Update email and logout
            admin.setEmail(email);
            superAdminRepository.save(admin);

            session.invalidate();
            redirectAttributes.addFlashAttribute("message", "Votre email a été modifié. Veuillez vous reconnecter.");
            return "redirect:/auth/login/superadmin";
        }

        // Update admin data
        admin.setNom(nom);
        superAdminRepository.save(admin);

        redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès.");
        return "redirect:/superadmin/dashboard";
    }

    // Change password
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        String adminEmail = (String) session.getAttribute("admin_email");
        if (adminEmail == null) {
            return "redirect:/auth/login/superadmin";
        }

        Optional<SuperAdmin> adminOptional = superAdminRepository.findByEmail(adminEmail);
        if (!adminOptional.isPresent()) {
            session.invalidate();
            return "redirect:/auth/login/superadmin";
        }

        SuperAdmin admin = adminOptional.get();

        // Check if current password is correct
        if (!passwordEncoder.matches(currentPassword, admin.getPassword())) {
            redirectAttributes.addFlashAttribute("passwordError", "password-incorrect");
            redirectAttributes.addFlashAttribute("errorMessage", "Le mot de passe actuel est incorrect.");
            return "redirect:/superadmin/dashboard";
        }

        // Check if new passwords match
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "password-mismatch");
            redirectAttributes.addFlashAttribute("errorMessage", "Les nouveaux mots de passe ne correspondent pas.");
            return "redirect:/superadmin/dashboard";
        }

        // Check if new password is different from current
        if (passwordEncoder.matches(newPassword, admin.getPassword())) {
            redirectAttributes.addFlashAttribute("passwordError", "password-same");
            redirectAttributes.addFlashAttribute("errorMessage", "Le nouveau mot de passe doit être différent de l'ancien.");
            return "redirect:/superadmin/dashboard";
        }

        // Password strength validation
        if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("passwordError", "password-weak");
            redirectAttributes.addFlashAttribute("errorMessage", "Le mot de passe doit contenir au moins 8 caractères.");
            return "redirect:/superadmin/dashboard";
        }

        // Update password
        admin.setPassword(passwordEncoder.encode(newPassword));
        superAdminRepository.save(admin);

        // Log out the user
        session.invalidate();
        redirectAttributes.addFlashAttribute("message", "Votre mot de passe a été modifié avec succès. Veuillez vous reconnecter.");
        return "redirect:/auth/login/superadmin";
    }
}