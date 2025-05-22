package org.example.charityproject1.controller;

import jakarta.servlet.http.HttpSession;

import org.example.charityproject1.model.ActionCharite;
import org.example.charityproject1.model.ActionStats;
import org.example.charityproject1.model.CategorieAction;
import org.example.charityproject1.model.Organisations;
import org.example.charityproject1.repository.OrganisationsRepository;
import org.example.charityproject1.service.ActionChariteService;
import org.example.charityproject1.service.CategorieActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.example.charityproject1.model.ActionStats;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/organisation")
public class OrganisationController {

    @Autowired
    private OrganisationsRepository organisationsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ActionChariteService actionChariteService;
     @Autowired
    private CategorieActionService categorieActionService;

   @GetMapping("/dashboard")
    public String dashboard(
            // Organizations parameters
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "6") int size,
            @RequestParam(value = "search", required = false) String search,
            
            // Campaigns parameters
            @RequestParam(value = "campaignPage", defaultValue = "0") int campaignPage,
            @RequestParam(value = "campaignSize", defaultValue = "6") int campaignSize,
            @RequestParam(value = "campaignSearch", required = false) String campaignSearch,
            @RequestParam(value = "campaignCategory", required = false) String campaignCategory,
            @RequestParam(value = "campaignSort", defaultValue = "newest") String campaignSort,
            
            HttpSession session, 
            Model model) {
        
        String orgId = (String) session.getAttribute("org_identifier");
        if (orgId == null) {
            return "redirect:/auth/login/organisation";
        }

        Optional<Organisations> orgOptional = organisationsRepository.findByNumeroIdentif(orgId);
        if (!orgOptional.isPresent()) {
            session.invalidate();
            return "redirect:/auth/login/organisation";
        }

        // Add current organization to model
        model.addAttribute("organisation", orgOptional.get());
        
        // Add organization statistics
        long orgCount = organisationsRepository.count();
        model.addAttribute("orgCount", orgCount);
        
        // Add campaign statistics
        long campaignCount = actionChariteService.countActions();
        model.addAttribute("campaignCount", campaignCount);
        
        // Add participants count
        long participantsCount = actionChariteService.countParticipants();
       model.addAttribute("totalParticipants", participantsCount);
        
        // Add donation statistics
        float totalDonations = actionChariteService.calculateTotalDonations();
        model.addAttribute("totalDonations", totalDonations);
        
        // Add campaign donations statistics
        float campaignDonations = actionChariteService.calculateTotalDonations(); // You might need a different method for this
        model.addAttribute("campaignDonations", campaignDonations);
        
        // Get all categories for the filter dropdown
        List<CategorieAction> categories = categorieActionService.getAllCategories();
        model.addAttribute("categories", categories);
        
        // ====== ORGANIZATION SECTION ======
        // Paginated list of organizations
        Page<Organisations> orgPage;
        Pageable pageable = PageRequest.of(page, size);
        
        if (search != null && !search.trim().isEmpty()) {
            // Search by name
            orgPage = organisationsRepository.findByNomContainingIgnoreCase(search, pageable);
            model.addAttribute("searchParam", search);
        } else {
            // Get all organizations
            orgPage = organisationsRepository.findAll(pageable);
            model.addAttribute("searchParam", "");
        }
        
        // Add organizations and pagination data to model
        model.addAttribute("organizations", orgPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orgPage.getTotalPages());
        model.addAttribute("size", size);
        
        // ====== CAMPAIGNS SECTION ======
        // Get all campaigns for this organization
      List<ActionCharite> allCampaigns = actionChariteService.getAllActiveActions();
        
        // Filter campaigns based on search term and category
        List<ActionCharite> filteredCampaigns = allCampaigns.stream()
                .filter(campaign -> {
                    // Apply search filter if provided
                    boolean matchesSearch = true;
                    if (campaignSearch != null && !campaignSearch.trim().isEmpty()) {
                        String searchLower = campaignSearch.toLowerCase();
                        matchesSearch = (campaign.getTitre() != null && campaign.getTitre().toLowerCase().contains(searchLower)) ||
                                (campaign.getDescription() != null && campaign.getDescription().toLowerCase().contains(searchLower)) ||
                                (campaign.getLieu() != null && campaign.getLieu().toLowerCase().contains(searchLower));
                    }
                    
                    // Apply category filter if provided
                    boolean matchesCategory = true;
                    if (campaignCategory != null && !campaignCategory.trim().isEmpty()) {
                        matchesCategory = campaignCategory.equals(campaign.getCategorieId());
                    }
                    
                    return matchesSearch && matchesCategory;
                })
                .collect(Collectors.toList());
        
        // Sort filtered campaigns
        if ("ending-soon".equals(campaignSort)) {
            // Sort by date limit (ascending)
            filteredCampaigns.sort(Comparator.comparing(a -> a.getDatelimite() != null ? a.getDatelimite() : new Date(Long.MAX_VALUE)));
        } else if ("most-funded".equals(campaignSort)) {
            // Sort by percentage funded (descending)
            filteredCampaigns.sort((a1, a2) -> {
                double pct1 = a1.getObjectifCollecte() > 0 ? a1.getMontantActuel() / a1.getObjectifCollecte() : 0;
                double pct2 = a2.getObjectifCollecte() > 0 ? a2.getMontantActuel() / a2.getObjectifCollecte() : 0;
                return Double.compare(pct2, pct1);
            });
        } else if ("most-popular".equals(campaignSort)) {
            // Sort by number of participants (descending)
            filteredCampaigns.sort(Comparator.comparing(ActionCharite::getNombreParticipants).reversed());
        } else {
            // Default: newest first (by date descending)
            filteredCampaigns.sort(Comparator.comparing(ActionCharite::getDate).reversed());
        }
        
        // Apply pagination
        int start = campaignPage * campaignSize;
        int end = Math.min(start + campaignSize, filteredCampaigns.size());
        List<ActionCharite> pagedCampaigns = start < filteredCampaigns.size()
                ? filteredCampaigns.subList(start, end)
                : new ArrayList<>();
        
        // Calculate total pages
        int campaignTotalPages = (int) Math.ceil((double) filteredCampaigns.size() / campaignSize);
        
        // Load categories for each campaign
        for (ActionCharite campaign : pagedCampaigns) {
            if (campaign.getCategorieId() != null) {
                try {
                    CategorieAction categorie = categorieActionService.getCategoryById(campaign.getCategorieId());
                    campaign.setCategorie(categorie);
                } catch (Exception e) {
                    // Category not found, continue with null categorie
                }
            }
        }
        actionChariteService.populateOrganisationsForActions(pagedCampaigns);
        
        // Add campaigns and pagination data to model
        model.addAttribute("campaigns", pagedCampaigns);
        model.addAttribute("campaignCurrentPage", campaignPage);
        model.addAttribute("campaignTotalPages", campaignTotalPages > 0 ? campaignTotalPages : 1);
        model.addAttribute("campaignSize", campaignSize);
        
        // Add filter parameters to model for pagination links
        model.addAttribute("campaignSearchParam", campaignSearch != null ? campaignSearch : "");
        model.addAttribute("campaignCategoryParam", campaignCategory != null ? campaignCategory : "");
        model.addAttribute("campaignSortParam", campaignSort);
        
        return "organisation/dashboard";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam("nom") String nom,
                                @RequestParam(value = "numeroIdentif", required = false) String newNumeroIdentif,
                                @RequestParam("adresseLegale") String adresseLegale,
                                @RequestParam("description") String description,
                                @RequestParam("contactprincipal") String contactPrincipal,
                                @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        String orgId = (String) session.getAttribute("org_identifier");
        if (orgId == null) {
            return "redirect:/auth/login/organisation";
        }

        Optional<Organisations> orgOptional = organisationsRepository.findByNumeroIdentif(orgId);
        if (!orgOptional.isPresent()) {
            session.invalidate();
            return "redirect:/auth/login/organisation";
        }

        // Validate form data
        if (nom == null || nom.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("profileError", "name-invalid");
            redirectAttributes.addFlashAttribute("profileErrorMessage", "Le nom de l'organisation est obligatoire.");
            return "redirect:/organisation/dashboard?profileError=true";
        }

        // Validate numeroIdentif
        if (newNumeroIdentif == null || newNumeroIdentif.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("profileError", "id-empty");
            redirectAttributes.addFlashAttribute("profileErrorMessage", "Le numéro d'identification est obligatoire.");
            return "redirect:/organisation/dashboard?profileError=true";
        }

        if (adresseLegale == null || adresseLegale.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("profileError", "address-invalid");
            redirectAttributes.addFlashAttribute("profileErrorMessage", "L'adresse légale est obligatoire.");
            return "redirect:/organisation/dashboard?profileError=true";
        }

        if (description == null || description.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("profileError", "description-invalid");
            redirectAttributes.addFlashAttribute("profileErrorMessage", "La description est obligatoire.");
            return "redirect:/organisation/dashboard?profileError=true";
        }

        if (contactPrincipal == null || contactPrincipal.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("profileError", "contact-invalid");
            redirectAttributes.addFlashAttribute("profileErrorMessage", "Le contact principal est obligatoire.");
            return "redirect:/organisation/dashboard?profileError=true";
        }

        Organisations organisation = orgOptional.get();
        organisation.setValideParAdmin(false);

        // Check if ID changed and is unique
        if (!newNumeroIdentif.equals(orgId)) {
            Optional<Organisations> existingOrg = organisationsRepository.findByNumeroIdentif(newNumeroIdentif);
            if (existingOrg.isPresent() && !existingOrg.get().getIdOrganisation().equals(organisation.getIdOrganisation())) {
                redirectAttributes.addFlashAttribute("profileError", "id-exists");
                redirectAttributes.addFlashAttribute("profileErrorMessage", "Ce numéro d'identification est déjà utilisé.");
                return "redirect:/organisation/dashboard?profileError=true";
            }

            // Update ID and logout
            organisation.setNumeroIdentif(newNumeroIdentif);
            organisationsRepository.save(organisation);

            session.invalidate();
            redirectAttributes.addFlashAttribute("message", "Votre numéro d'identification a été modifié. Veuillez vous reconnecter.");
            return "redirect:/auth/login/organisation";
        }

        // Update organisation data
        organisation.setNom(nom);
        organisation.setAdresseLegale(adresseLegale);
        organisation.setDescription(description);
        organisation.setContactPrincipal(contactPrincipal);

        // Process logo if provided
        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                // Check file size (max 2MB)
                if (logoFile.getSize() > 2 * 1024 * 1024) {
                    redirectAttributes.addFlashAttribute("profileError", "logo-invalid");
                    redirectAttributes.addFlashAttribute("profileErrorMessage", "La taille du logo ne doit pas dépasser 2MB.");
                    return "redirect:/organisation/dashboard?profileError=true";
                }

                // Check file type
                String contentType = logoFile.getContentType();
                if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                    redirectAttributes.addFlashAttribute("profileError", "logo-invalid");
                    redirectAttributes.addFlashAttribute("profileErrorMessage", "Le format du logo doit être JPG ou PNG.");
                    return "redirect:/organisation/dashboard?profileError=true";
                }

                byte[] bytes = logoFile.getBytes();
                String base64Image = Base64.getEncoder().encodeToString(bytes);
                organisation.setLogo(base64Image);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("profileError", "logo-invalid");
                redirectAttributes.addFlashAttribute("profileErrorMessage", "Erreur lors du téléchargement du logo.");
                return "redirect:/organisation/dashboard?profileError=true";
            }
        }

        organisationsRepository.save(organisation);
        redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès.");
        return "redirect:/organisation/dashboard";
    }
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        String orgId = (String) session.getAttribute("org_identifier");
        if (orgId == null) {
            return "redirect:/auth/login/organisation";
        }

        Optional<Organisations> orgOptional = organisationsRepository.findByNumeroIdentif(orgId);
        if (!orgOptional.isPresent()) {
            session.invalidate();
            return "redirect:/auth/login/organisation";
        }

        Organisations organisation = orgOptional.get();

        // Check if current password is correct
        if (!passwordEncoder.matches(currentPassword, organisation.getPassword())) {
            redirectAttributes.addFlashAttribute("passwordError", "password-incorrect");
            redirectAttributes.addFlashAttribute("errorMessage", "Le mot de passe actuel est incorrect.");
            return "redirect:/organisation/dashboard?passwordError=true";
        }

        // Check if new passwords match
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "password-mismatch");
            redirectAttributes.addFlashAttribute("errorMessage", "Les nouveaux mots de passe ne correspondent pas.");
            return "redirect:/organisation/dashboard?passwordError=true";
        }

        // Check if new password is different from current
        if (passwordEncoder.matches(newPassword, organisation.getPassword())) {
            redirectAttributes.addFlashAttribute("passwordError", "password-same");
            redirectAttributes.addFlashAttribute("errorMessage", "Le nouveau mot de passe doit être différent de l'ancien.");
            return "redirect:/organisation/dashboard?passwordError=true";
        }

        // Password strength validation
        if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("passwordError", "password-weak");
            redirectAttributes.addFlashAttribute("errorMessage", "Le mot de passe doit contenir au moins 8 caractères.");
            return "redirect:/organisation/dashboard?passwordError=true";
        }

        // Update password
        organisation.setPassword(passwordEncoder.encode(newPassword));
        organisationsRepository.save(organisation);

        // Log out the user
        session.invalidate();
        redirectAttributes.addFlashAttribute("message", "Votre mot de passe a été modifié avec succès. Veuillez vous reconnecter.");
        return "redirect:/auth/login/organisation";
    }

   @GetMapping("/actions/dashboard-view/{idAction}")
    public String viewAction(@PathVariable("idAction") String idAction, Model model, HttpSession session) {
        // Get organization ID from session
        String orgId = (String) session.getAttribute("org_identifier");
        if (orgId == null) {
            return "redirect:/auth/login/organisation";
        }
        
        // Get the action/campaign
        ActionCharite action = actionChariteService.getActionById(idAction);
        if (action == null) {
            // Handle not found
            return "redirect:/organisation/dashboard";
        }
        
        // Populate organization info
        if (action.getOrganisationId() != null && (action.getOrganisation() == null || action.getOrganisation().getLogo() == null)) {
            Organisations org = organisationsRepository.findByNumeroIdentif(action.getOrganisationId()).orElse(null);
            action.setOrganisation(org);
        }
        
        // Determine if this campaign belongs to the current organization
        boolean isOwnCampaign = action.getOrganisationId() != null && action.getOrganisationId().equals(orgId);
        
        model.addAttribute("action", action);
        model.addAttribute("isOwnCampaign", isOwnCampaign);
        model.addAttribute("fromDashboard", true); // Flag to indicate we're viewing from dashboard
        
        // Add organization to the model
        Optional<Organisations> orgOptional = organisationsRepository.findByNumeroIdentif(orgId);
        if (orgOptional.isPresent()) {
            model.addAttribute("organisation", orgOptional.get());
        }
        
        return "organisation/view-campaign";
    }

}