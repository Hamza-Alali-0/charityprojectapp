package org.example.charityproject1.controller;

import jakarta.servlet.http.HttpSession;
import org.example.charityproject1.model.CategorieAction;
import org.example.charityproject1.model.Organisations;
import org.example.charityproject1.model.SuperAdmin;
import org.example.charityproject1.model.Utilisateurs;
import org.example.charityproject1.repository.SuperAdminRepository;
import org.example.charityproject1.repository.UtilisateursRepository;
import org.example.charityproject1.service.CategorieActionService;
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
    private UtilisateursRepository utilisateursRepository;
    @Autowired
    private SuperAdminService superAdminService;

    @Autowired
    private CategorieActionService categorieActionService;
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

    /**
     * Show confirmation page before deleting a user
     */
    /**
     * Show confirmation page before deleting a user
     */
    @GetMapping("/utilisateurs/confirm-delete/{userId}")
    public String showDeleteConfirmation(@PathVariable String userId, Model model) {
        // Get the user to delete
        Optional<Utilisateurs> userOpt = utilisateursRepository.findById(userId);

        if (userOpt.isPresent()) {
            model.addAttribute("user", userOpt.get());
            return "superadmin/utilisateurs/confirmation-modal";
        } else {
            // User not found, redirect to dashboard
            return "redirect:/superadmin/dashboard#users";
        }
    }

    /**
     * Process user deletion after confirmation
     */
    @PostMapping("/utilisateurs/delete")
    public String deleteUser(@RequestParam String userId, RedirectAttributes redirectAttributes) {
        try {
            // Check if user exists
            if (utilisateursRepository.existsById(userId)) {
                // Delete the user
                utilisateursRepository.deleteById(userId);
                redirectAttributes.addFlashAttribute("successMessage", "Utilisateur supprimé avec succès");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Utilisateur non trouvé");
            }
        } catch (Exception e) {
            // Log the error
            System.err.println("Error deleting user: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Une erreur est survenue lors de la suppression");
        }

        return "redirect:/superadmin/dashboard#users";
    }
    /**
     * Show confirmation page before deleting an organization
     */
    @GetMapping("/organisations/confirm-delete/{orgId}")
    public String showDeleteOrganisationConfirmation(@PathVariable String orgId, Model model) {
        // Get the organization to delete
        Optional<Organisations> orgOpt = superAdminService.getOrganisationById(orgId);

        if (orgOpt.isPresent()) {
            model.addAttribute("organisation", orgOpt.get());
            return "superadmin/organisations/confirmation-modal";
        } else {
            // Organization not found, redirect to dashboard
            return "redirect:/superadmin/dashboard#organisations";
        }
    }

    /**
     * Process organization deletion after confirmation
     */
    @PostMapping("/organisations/delete")
    public String deleteOrganisation(@RequestParam String orgId, RedirectAttributes redirectAttributes) {
        try {
            // Delete the organization
            superAdminService.deleteOrganisation(orgId);
            redirectAttributes.addFlashAttribute("successMessage", "Organisation supprimée avec succès");
        } catch (Exception e) {
            // Log the error
            System.err.println("Error deleting organization: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Une erreur est survenue lors de la suppression de l'organisation");
        }

        return "redirect:/superadmin/dashboard#organisations";
    }
    // Add error handling to your categories-content method
    @GetMapping("/categories-content")
    public String getCategoriesContent(Model model) {
        try {
            List<CategorieAction> categories = categorieActionService.getAllCategories();
            model.addAttribute("categories", categories);
            return "superadmin/categories/categories :: categories-content";
        } catch (Exception e) {
            // Log the error
            e.printStackTrace();
            model.addAttribute("errorMessage", "Une erreur est survenue lors du chargement des catégories: " + e.getMessage());
            return "superadmin/error :: error-content";
        }
    }
    // Add these methods to your SuperAdminController class

    @GetMapping("/api/categories")
    @ResponseBody
    public List<CategorieAction> getAllCategories() {
        return categorieActionService.getAllCategories();
    }

    @GetMapping("/api/categories/{id}")
    @ResponseBody
    public ResponseEntity<?> getCategoryById(@PathVariable("id") String id) {
        try {
            CategorieAction category = categorieActionService.getCategoryById(id);
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }
    // In SuperAdminController.java
    @PostMapping("/api/categories")
    @ResponseBody
    public ResponseEntity<?> createCategory(@RequestBody CategorieAction category) {
        try {
            // Enhanced debug logging
            System.out.println("Received category creation request");
            System.out.println("Category data: " + (category != null ? category.toString() : "null"));

            if (category == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Category data is missing"));
            }

            // Validate category
            if (category.getNom() == null || category.getNom().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Le nom de la catégorie est obligatoire"));
            }

            System.out.println("Creating category with name: " + category.getNom());
            CategorieAction createdCategory = categorieActionService.createCategory(category);
            System.out.println("Created category with ID: " + createdCategory.getIdCategorie());

            return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/api/categories/{id}")
    @ResponseBody
    public ResponseEntity<?> updateCategory(
            @PathVariable("id") String id,
            @RequestBody CategorieAction category) {
        try {
            CategorieAction updatedCategory = categorieActionService.updateCategory(id, category);
            return ResponseEntity.ok(updatedCategory);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/api/categories/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteCategory(@PathVariable("id") String id) {
        try {
            categorieActionService.deleteCategory(id);
            return ResponseEntity.ok(Map.of("message", "Catégorie supprimée avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }
    /**
     * Show confirmation page before deleting a category
     */
    @GetMapping("/categories/confirm-delete/{categoryId}")
    public String showDeleteCategoryConfirmation(@PathVariable String categoryId, Model model) {
        try {
            // Get the category to delete
            CategorieAction category = categorieActionService.getCategoryById(categoryId);
            model.addAttribute("category", category);
            return "superadmin/categories/confirmation-modal";
        } catch (Exception e) {
            // Category not found, redirect to dashboard
            return "redirect:/superadmin/dashboard#categories";
        }
    }

    /**
     * Process category deletion after confirmation
     */
    @PostMapping("/categories/delete")
    public String deleteCategory(@RequestParam String categoryId, RedirectAttributes redirectAttributes) {
        try {
            // Delete the category
            categorieActionService.deleteCategory(categoryId);
            redirectAttributes.addFlashAttribute("successMessage", "Catégorie supprimée avec succès");
        } catch (Exception e) {
            // Log the error
            System.err.println("Error deleting category: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Une erreur est survenue lors de la suppression de la catégorie");
        }

        return "redirect:/superadmin/dashboard#categories";
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
