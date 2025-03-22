package org.example.charityproject1.controller;

import jakarta.servlet.http.HttpSession;
import org.example.charityproject1.model.ActionCharite;
import org.example.charityproject1.model.CategorieAction;
import org.example.charityproject1.model.Utilisateurs;
import org.example.charityproject1.repository.ActionChariteRepository;
import org.example.charityproject1.repository.UtilisateursRepository;
import org.example.charityproject1.service.ActionChariteService;
import org.example.charityproject1.service.CategorieActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.*;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UtilisateursRepository utilisateursRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ActionChariteService actionChariteService;

    @Autowired
    private CategorieActionService categorieActionService;
    @Autowired
    private ActionChariteRepository actionChariteRepository;


    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String userEmail = (String) session.getAttribute("user_email");
        if (userEmail == null) {
            return "redirect:/auth/login";
        }

        Optional<Utilisateurs> userOptional = utilisateursRepository.findByEmail(userEmail);
        if (!userOptional.isPresent()) {
            session.invalidate();
            return "redirect:/auth/login";
        }

        model.addAttribute("user", userOptional.get());
        return "user/dashboard";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam("nom") String nom,
                                @RequestParam("email") String email,
                                @RequestParam("telephone") String telephone,
                                @RequestParam("localisation") String localisation,
                                @RequestParam(value = "photoFile", required = false) MultipartFile photoFile,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        String userEmail = (String) session.getAttribute("user_email");
        if (userEmail == null) {
            return "redirect:/auth/login";
        }

        Optional<Utilisateurs> userOptional = utilisateursRepository.findByEmail(userEmail);
        if (!userOptional.isPresent()) {
            session.invalidate();
            return "redirect:/auth/login";
        }

        // Validate form data
        if (nom == null || nom.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("profileError", "name-invalid");
            redirectAttributes.addFlashAttribute("profileErrorMessage", "Le nom est obligatoire.");
            return "redirect:/user/dashboard?profileError=true";
        }

        // Validate email
        if (email == null || email.trim().isEmpty() || !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            redirectAttributes.addFlashAttribute("profileError", "email-invalid");
            redirectAttributes.addFlashAttribute("profileErrorMessage", "Veuillez saisir un email valide.");
            return "redirect:/user/dashboard?profileError=true";
        }

        // Validate phone
        if (telephone == null || !telephone.matches("^[0-9]{10}$")) {
            redirectAttributes.addFlashAttribute("profileError", "phone-invalid");
            redirectAttributes.addFlashAttribute("profileErrorMessage", "Le téléphone doit contenir 10 chiffres.");
            return "redirect:/user/dashboard?profileError=true";
        }

        if (localisation == null || localisation.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("profileError", "localisation-invalid");
            redirectAttributes.addFlashAttribute("profileErrorMessage", "La localisation est obligatoire.");
            return "redirect:/user/dashboard?profileError=true";
        }

        Utilisateurs utilisateur = userOptional.get();

        // Check if email changed and is unique
        if (!email.equals(userEmail)) {
            Optional<Utilisateurs> existingUser = utilisateursRepository.findByEmail(email);
            if (existingUser.isPresent() && !existingUser.get().getUserId().equals(utilisateur.getUserId())) {
                redirectAttributes.addFlashAttribute("profileError", "email-exists");
                redirectAttributes.addFlashAttribute("profileErrorMessage", "Cet email est déjà utilisé.");
                return "redirect:/user/dashboard?profileError=true";
            }

            // Update email and logout
            utilisateur.setEmail(email);
            utilisateursRepository.save(utilisateur);

            session.invalidate();
            redirectAttributes.addFlashAttribute("message", "Votre email a été modifié. Veuillez vous reconnecter.");
            return "redirect:/auth/login";
        }

        // Update user data
        utilisateur.setNom(nom);
        utilisateur.setTelephone(telephone);
        utilisateur.setLocalisation(localisation);

        // Process photo if provided
        if (photoFile != null && !photoFile.isEmpty()) {
            try {
                // Check file size (max 2MB)
                if (photoFile.getSize() > 2 * 1024 * 1024) {
                    redirectAttributes.addFlashAttribute("profileError", "photo-invalid");
                    redirectAttributes.addFlashAttribute("profileErrorMessage", "La taille de la photo ne doit pas dépasser 2MB.");
                    return "redirect:/user/dashboard?profileError=true";
                }

                // Check file type
                String contentType = photoFile.getContentType();
                if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                    redirectAttributes.addFlashAttribute("profileError", "photo-invalid");
                    redirectAttributes.addFlashAttribute("profileErrorMessage", "Le format de la photo doit être JPG ou PNG.");
                    return "redirect:/user/dashboard?profileError=true";
                }

                byte[] bytes = photoFile.getBytes();
                String base64Image = Base64.getEncoder().encodeToString(bytes);
                utilisateur.setLogoPath(base64Image);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("profileError", "photo-invalid");
                redirectAttributes.addFlashAttribute("profileErrorMessage", "Erreur lors du téléchargement de la photo.");
                return "redirect:/user/dashboard?profileError=true";
            }
        }

        utilisateursRepository.save(utilisateur);
        redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès.");
        return "redirect:/user/dashboard";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        String userEmail = (String) session.getAttribute("user_email");
        if (userEmail == null) {
            return "redirect:/auth/login";
        }

        Optional<Utilisateurs> userOptional = utilisateursRepository.findByEmail(userEmail);
        if (!userOptional.isPresent()) {
            session.invalidate();
            return "redirect:/auth/login";
        }

        Utilisateurs utilisateur = userOptional.get();

        // Check if current password is correct
        if (!passwordEncoder.matches(currentPassword, utilisateur.getPassword())) {
            redirectAttributes.addFlashAttribute("passwordError", "password-incorrect");
            redirectAttributes.addFlashAttribute("errorMessage", "Le mot de passe actuel est incorrect.");
            return "redirect:/user/dashboard?passwordError=true";
        }

        // Check if new passwords match
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "password-mismatch");
            redirectAttributes.addFlashAttribute("errorMessage", "Les nouveaux mots de passe ne correspondent pas.");
            return "redirect:/user/dashboard?passwordError=true";
        }

        // Check if new password is different from current
        if (passwordEncoder.matches(newPassword, utilisateur.getPassword())) {
            redirectAttributes.addFlashAttribute("passwordError", "password-same");
            redirectAttributes.addFlashAttribute("errorMessage", "Le nouveau mot de passe doit être différent de l'ancien.");
            return "redirect:/user/dashboard?passwordError=true";
        }

        // Password strength validation
        if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("passwordError", "password-weak");
            redirectAttributes.addFlashAttribute("errorMessage", "Le mot de passe doit contenir au moins 8 caractères.");
            return "redirect:/user/dashboard?passwordError=true";
        }

        // Update password
        utilisateur.setPassword(passwordEncoder.encode(newPassword));
        utilisateursRepository.save(utilisateur);

        // Log out the user
        session.invalidate();
        redirectAttributes.addFlashAttribute("message", "Votre mot de passe a été modifié avec succès. Veuillez vous reconnecter.");
        return "redirect:/auth/login";
    }
    // ...existing code...



    @GetMapping("/actions")
    public String showActions(HttpSession session, Model model) {
        String userEmail = (String) session.getAttribute("user_email");
        if (userEmail == null) {
            return "redirect:/auth/login";
        }

        Optional<Utilisateurs> userOptional = utilisateursRepository.findByEmail(userEmail);
        if (!userOptional.isPresent()) {
            session.invalidate();
            return "redirect:/auth/login";
        }

        Utilisateurs user = userOptional.get();
        model.addAttribute("user", user);

        // Fetch all active actions
        List<ActionCharite> actions = actionChariteService.getAllActiveActions();

        // Load categories for each action
        for (ActionCharite action : actions) {
            if (action.getCategorieId() != null) {
                try {
                    CategorieAction categorie = categorieActionService.getCategoryById(action.getCategorieId());
                    action.setCategorie(categorie);
                } catch (Exception e) {
                    // Category not found, continue with null categorie
                }
            }
        }
        actionChariteService.populateOrganisationsForActions(actions);

        model.addAttribute("actions", actions);

        return "user/actions/actions-feed";
    }
    // ...existing code...



    // Find this method:
    @GetMapping("/actions/details/{id}")
    public String showActionDetails(@PathVariable("id") String id, HttpSession session, Model model) {
        String userEmail = (String) session.getAttribute("user_email");
        if (userEmail == null) {
            return "redirect:/auth/login";
        }

        Utilisateurs user = utilisateursRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("user", user);

        ActionCharite action = actionChariteService.getActionById(id);

        // Call populateOrganisationsForActions to set the organization object
        if (action != null) {
            List<ActionCharite> actions = new ArrayList<>();
            actions.add(action);
            actionChariteService.populateOrganisationsForActions(actions);

            // If the action has a category, load it
            if (action.getCategorieId() != null) {
                try {
                    CategorieAction categorie = categorieActionService.getCategoryById(action.getCategorieId());
                    action.setCategorie(categorie);
                } catch (Exception e) {
                    // Category not found, continue with null categorie
                }
            }
        }

        model.addAttribute("action", action);

        return "user/actions/action-details";  // Update this path to match the new template location
    }

    @PostMapping("/actions/like/{id}")
    @ResponseBody
    public Map<String, Object> likeAction(@PathVariable("id") String id, HttpSession session) {
        String userEmail = (String) session.getAttribute("user_email");
        if (userEmail == null) {
            return Map.of("success", false, "message", "User not logged in");
        }

        try {
            Utilisateurs user = utilisateursRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Add action to user's liked actions
            if (user.getLikedActions() == null) {
                user.setLikedActions(new ArrayList<>());
            }
            user.ajouterActionAimee(id);
            utilisateursRepository.save(user);

            // Add user to action's liked by users
            ActionCharite action = actionChariteService.addLike(id, user);

            return Map.of("success", true, "likesCount", action.getLikesCount());
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/actions/unlike/{id}")
    @ResponseBody
    public Map<String, Object> unlikeAction(@PathVariable("id") String id, HttpSession session) {
        String userEmail = (String) session.getAttribute("user_email");
        if (userEmail == null) {
            return Map.of("success", false, "message", "User not logged in");
        }

        try {
            Utilisateurs user = utilisateursRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Remove action from user's liked actions
            if (user.getLikedActions() != null) {
                user.supprimerActionAimee(id);
                utilisateursRepository.save(user);
            }

            // Remove user from action's liked by users
            ActionCharite action = actionChariteService.removeLike(id, user.getUserId());

            return Map.of("success", true, "likesCount", action.getLikesCount());
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/actions/participate/{id}")
    @ResponseBody
    public Map<String, Object> participateInAction(@PathVariable("id") String id, HttpSession session) {
        String userEmail = (String) session.getAttribute("user_email");
        if (userEmail == null) {
            return Map.of("success", false, "message", "User not logged in");
        }

        try {
            Utilisateurs user = utilisateursRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ActionCharite action = actionChariteService.getActionById(id);

            // Check if user is already a participant
            if (action.getListUsersContribue() == null) {
                action.setListUsersContribue(new ArrayList<>());
            }

            if (!action.getListUsersContribue().contains(user.getUserId())) {
                action.getListUsersContribue().add(user.getUserId());
                action.setNombreParticipants(action.getNombreParticipants() + 1);
                actionChariteRepository.save(action);

                return Map.of("success", true, "participantsCount", action.getNombreParticipants());
            } else {
                return Map.of("success", false, "message", "Vous participez déjà à cette action");
            }
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }


}