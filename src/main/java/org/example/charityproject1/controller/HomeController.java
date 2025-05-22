package org.example.charityproject1.controller;

import org.example.charityproject1.model.ActionCharite;
import org.example.charityproject1.model.CategorieAction;
import org.example.charityproject1.model.Organisations;
import org.example.charityproject1.model.ContactMessage;
import org.example.charityproject1.service.ActionChariteService;
import org.example.charityproject1.service.CategorieActionService;
import org.example.charityproject1.service.OrganisationsService;
import org.example.charityproject1.service.UtilisateursService;
import org.example.charityproject1.service.ContactMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Controller
public class HomeController {

    @Autowired
    private CategorieActionService categorieActionService;
    
    @Autowired
    private ActionChariteService actionChariteService;
    
    @Autowired
    private OrganisationsService organisationsService;
    
    @Autowired
    private UtilisateursService utilisateursService;

    @Autowired
    private ContactMessageService contactMessageService;

    @GetMapping("/")
    public String home(Model model) {
        // Add global statistics
        Long orgCount = organisationsService.countOrganisations();
        Long campaignCount = actionChariteService.countActions();
        Long donorCount = utilisateursService.countUsers();
        Float totalDonations = actionChariteService.calculateTotalDonations();
        
        // Add to model
        model.addAttribute("orgCount", orgCount);
        model.addAttribute("campaignCount", campaignCount);
        model.addAttribute("donorCount", donorCount);
        model.addAttribute("totalDonations", totalDonations);
        
        return "redirect:/accueil";
    }

    @GetMapping("/accueil")
    public String accueil(Model model) {
        // Add global statistics (same as in home method)
        Long orgCount = organisationsService.countOrganisations();
        Long campaignCount = actionChariteService.countActions();
        Long donorCount = utilisateursService.countUsers();
        Float totalDonations = actionChariteService.calculateTotalDonations();
        
        // Add statistics to model with null checks
        model.addAttribute("orgCount", orgCount != null ? orgCount : 0);
        model.addAttribute("campaignCount", campaignCount != null ? campaignCount : 0);
        model.addAttribute("donorCount", donorCount != null ? donorCount : 0);
        model.addAttribute("totalDonations", totalDonations != null ? totalDonations : 0f);
        
        // Fetch categories and add to model
        List<CategorieAction> categories = categorieActionService.getAllCategories();
        model.addAttribute("categories", categories);
        
        // Rest of the existing code...
        List<ActionCharite> featuredActions = actionChariteService.getAllActiveActions();
        
        // Populate organization data for each action
        actionChariteService.populateOrganisationsForActions(featuredActions);
        
        // Populate category data for each action
        for (ActionCharite action : featuredActions) {
            if (action.getCategorieId() != null) {
                for (CategorieAction category : categories) {
                    if (category.getIdCategorie().equals(action.getCategorieId())) {
                        action.setCategorie(category);
                        break;
                    }
                }
            }
        }
        
        model.addAttribute("featuredActions", featuredActions);
        
        // Add organizations to the model
        List<Organisations> organisations = organisationsService.getAllOrganisations();
        model.addAttribute("organisations", organisations);
        
        return "Home/accueil";
    }
    
    @GetMapping("/about")
    public String about(Model model) {
        // Add global statistics
        Long orgCount = organisationsService.countOrganisations();
        Long campaignCount = actionChariteService.countActions();
        Long donorCount = utilisateursService.countUsers();
        Float totalDonations = actionChariteService.calculateTotalDonations();
        
        // Add statistics to model with null checks
        model.addAttribute("orgCount", orgCount != null ? orgCount : 0);
        model.addAttribute("campaignCount", campaignCount != null ? campaignCount : 0);
        model.addAttribute("donorCount", donorCount != null ? donorCount : 0);
        model.addAttribute("totalDonations", totalDonations != null ? totalDonations : 0f);
        
        return "Home/about";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        return "Home/contact";
    }

    // Optional: Handle form submission
    @PostMapping("/contact/submit")
    public String contactSubmit(@RequestParam String name, 
                           @RequestParam String email,
                           @RequestParam String subject,
                           @RequestParam String message,
                           RedirectAttributes redirectAttributes) {
    try {
        ContactMessage contactMessage = new ContactMessage();
        contactMessage.setName(name);
        contactMessage.setEmail(email);
        contactMessage.setSubject(subject != null ? subject : "No Subject");
        contactMessage.setMessage(message);
        contactMessage.setTimestamp(LocalDateTime.now());
        contactMessage.setRead(false);
        
        ContactMessage savedMessage = contactMessageService.saveContactMessage(contactMessage);
        
        if (savedMessage != null && savedMessage.getId() != null) {
            // Successfully saved
            redirectAttributes.addFlashAttribute("successMessage", "Your message has been sent successfully!");
        } else {
            // Something went wrong with saving
            redirectAttributes.addFlashAttribute("errorMessage", "There was a problem sending your message. Please try again later.");
        }
    } catch (Exception e) {
        // Log the exception for debugging
        e.printStackTrace();
        redirectAttributes.addFlashAttribute("errorMessage", "There was a problem sending your message. Please try again later.");
    }
    
    return "redirect:/contact";
}
    
    @GetMapping("/error")
    public String error() {
        return "Home/error";
    }

    @GetMapping("/campaigns")
    public String allCampaigns(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String organization, // Add this parameter
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size, 
            Model model) {
        
        // Get all active campaigns
        List<ActionCharite> allCampaigns = actionChariteService.getAllActiveActions();
        
        // Filter by search if provided
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.trim().toLowerCase();
            allCampaigns = allCampaigns.stream()
                .filter(action -> action.getTitre() != null && action.getTitre().toLowerCase().contains(searchLower))
                .collect(Collectors.toList());
        }
        
        // Filter by category if provided
        if (category != null && !category.isEmpty()) {
            allCampaigns = allCampaigns.stream()
                    .filter(action -> category.equals(action.getCategorieId()))
                    .collect(Collectors.toList());
        }
        
        // Filter by organization if provided
        if (organization != null && !organization.isEmpty()) {
            // Populate organizations for all campaigns to ensure proper filtering
            actionChariteService.populateOrganisationsForActions(allCampaigns);
            
            allCampaigns = allCampaigns.stream()
                    .filter(action -> action.getOrganisation() != null && 
                                     organization.equals(action.getOrganisation().getNumeroIdentif()))
                    .collect(Collectors.toList());
        }
        
        // Sort if needed
        if (sort != null) {
            switch (sort) {
                case "newest":
                    allCampaigns.sort((a1, a2) -> a2.getDateCreation().compareTo(a1.getDateCreation()));
                    break;
                case "ending-soon":
                    allCampaigns.sort((a1, a2) -> {
                        Date d1 = a1.getDatelimite() != null ? a1.getDatelimite() : a1.getDate();
                        Date d2 = a2.getDatelimite() != null ? a2.getDatelimite() : a2.getDate();
                        return d1.compareTo(d2);
                    });
                    break;
                case "most-funded":
                    allCampaigns.sort((a1, a2) -> Float.compare(a2.getMontantActuel(), a1.getMontantActuel()));
                    break;
                case "most-popular":
                    allCampaigns.sort((a1, a2) -> {
                        int likes1 = a1.getLikedByUsers() != null ? a1.getLikedByUsers().size() : 0;
                        int likes2 = a2.getLikedByUsers() != null ? a2.getLikedByUsers().size() : 0;
                        return Integer.compare(likes2, likes1);
                    });
                    break;
            }
        }
        
        // Calculate pagination
        int totalCampaigns = allCampaigns.size();
        int totalPages = (int) Math.ceil((double) totalCampaigns / size);
        
        // Apply pagination
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalCampaigns);
        
        List<ActionCharite> paginatedCampaigns = 
                fromIndex < totalCampaigns ? allCampaigns.subList(fromIndex, toIndex) : new ArrayList<>();
        
        // Populate organization data for each action
        actionChariteService.populateOrganisationsForActions(paginatedCampaigns);
        
        // Populate category data for each action
        List<CategorieAction> categories = categorieActionService.getAllCategories();
        for (ActionCharite action : paginatedCampaigns) {
            if (action.getCategorieId() != null) {
                for (CategorieAction cat : categories) {
                    if (cat.getIdCategorie().equals(action.getCategorieId())) {
                        action.setCategorie(cat);
                        break;
                    }
                }
            }
        }
        
        // Add to model
        model.addAttribute("campaigns", paginatedCampaigns);
        model.addAttribute("categories", categories);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        
        // Pre-select the filter/sort values
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedSort", sort);
       model.addAttribute("param", Map.of("search", search == null ? "" : search));
        
        // Add statistics data for the stats section
        Long orgCount = organisationsService.countOrganisations();
        Long campaignCount = actionChariteService.countActions();
        Long donorCount = utilisateursService.countUsers();
        Float totalDonations = actionChariteService.calculateTotalDonations();
        
        // Add to model with null checks
        model.addAttribute("orgCount", orgCount != null ? orgCount : 0);
        model.addAttribute("campaignCount", campaignCount != null ? campaignCount : 0);
        model.addAttribute("donorCount", donorCount != null ? donorCount : 0);
        model.addAttribute("totalDonations", totalDonations != null ? totalDonations : 0f);
        
        return "Home/campaigns";
    }
    
    @GetMapping("/organizations")
    public String allOrganizations(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model) {
        
        // Get all organizations
        List<Organisations> allOrganizations = organisationsService.getAllOrganisations();
        
        // Filter by search if provided
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.trim().toLowerCase();
            allOrganizations = allOrganizations.stream()
                .filter(org -> (org.getNom() != null && org.getNom().toLowerCase().contains(searchLower)) ||
                              (org.getDescription() != null && org.getDescription().toLowerCase().contains(searchLower)))
                .collect(Collectors.toList());
        }
        
        // Calculate pagination
        int totalOrgs = allOrganizations.size();
        int totalPages = (int) Math.ceil((double) totalOrgs / size);
        
        // Apply pagination
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalOrgs);
        
        List<Organisations> paginatedOrgs = 
                fromIndex < totalOrgs ? allOrganizations.subList(fromIndex, toIndex) : new ArrayList<>();
        
        // Add to model
        model.addAttribute("organizations", paginatedOrgs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("search", search);
        
        // Add statistics for the top section
        Long orgCount = organisationsService.countOrganisations();
        Long campaignCount = actionChariteService.countActions();
        Long donorCount = utilisateursService.countUsers();
        Float totalDonations = actionChariteService.calculateTotalDonations();
        
        model.addAttribute("orgCount", orgCount != null ? orgCount : 0);
        model.addAttribute("campaignCount", campaignCount != null ? campaignCount : 0);
        model.addAttribute("donorCount", donorCount != null ? donorCount : 0);
        model.addAttribute("totalDonations", totalDonations != null ? totalDonations : 0f);
        
        return "Home/organizations";
    }
  
    @GetMapping("/campaigns/view/{id}")
    public String viewCampaign(@PathVariable("id") String id, Model model) {
        // Convert the id to the appropriate type (assuming it's a String in the URL)
        ActionCharite action = actionChariteService.getActionById(id);
        
        // Handle case where action is not found
        if (action == null) {
            return "redirect:/campaigns";
        }
        
        // Populate organization data for the action
        // Fix: Use the existing method that takes a list
        List<ActionCharite> singleActionList = new ArrayList<>();
        singleActionList.add(action);
        actionChariteService.populateOrganisationsForActions(singleActionList);
        
        // Populate category data for the action
        if (action.getCategorieId() != null) {
            CategorieAction category = categorieActionService.getCategoryById(action.getCategorieId());
            action.setCategorie(category);
        }
        
        // Add the action to the model
        model.addAttribute("action", action);
        
        // Return the view-campaign template
        return "Home/view-campaign";
    }
    
    /**
     * Display organization details page with its campaigns and statistics
     */
    @GetMapping("/organization/{id}")
    public String viewOrganization(
            @PathVariable("id") String id,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            Model model) {
        try {
            // Basic organization fetch without security checks
            List<Organisations> allOrganizations = organisationsService.getAllOrganisations();
            Organisations organization = null;
            for (Organisations org : allOrganizations) {
                if (org.getNumeroIdentif() != null && org.getNumeroIdentif().equals(id)) {
                    organization = org;
                    break;
                }
            }
            
            if (organization == null) {
                return "redirect:/organizations?error=not-found";
            }
            
            // Get all campaigns
            List<ActionCharite> allCampaigns = actionChariteService.getAllActiveActions();
            
            // Populate organizations before filtering
            actionChariteService.populateOrganisationsForActions(allCampaigns);
            
            // Filter campaigns for this organization
            List<ActionCharite> orgCampaigns = new ArrayList<>();
            for (ActionCharite action : allCampaigns) {
                if (action.getOrganisation() != null && 
                    id.equals(action.getOrganisation().getNumeroIdentif())) {
                    orgCampaigns.add(action);
                }
            }
            
            // Get all categories for filtering
            List<CategorieAction> categories = categorieActionService.getAllCategories();
            
            // Apply search filter if provided
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.trim().toLowerCase();
                orgCampaigns = orgCampaigns.stream()
                    .filter(action -> action.getTitre() != null && 
                                     action.getTitre().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
            }
            
            // Apply category filter if provided
            if (category != null && !category.isEmpty()) {
                orgCampaigns = orgCampaigns.stream()
                        .filter(action -> category.equals(action.getCategorieId()))
                        .collect(Collectors.toList());
            }
            
            // Apply sorting if needed
            if (sort != null) {
                switch (sort) {
                    case "newest":
                        orgCampaigns.sort((a1, a2) -> a2.getDateCreation().compareTo(a1.getDateCreation()));
                        break;
                    case "ending-soon":
                        orgCampaigns.sort((a1, a2) -> {
                            Date d1 = a1.getDatelimite() != null ? a1.getDatelimite() : a1.getDate();
                            Date d2 = a2.getDatelimite() != null ? a2.getDatelimite() : a2.getDate();
                            return d1.compareTo(d2);
                        });
                        break;
                    case "most-funded":
                        orgCampaigns.sort((a1, a2) -> Float.compare(a2.getMontantActuel(), a1.getMontantActuel()));
                        break;
                    case "most-popular":
                        orgCampaigns.sort((a1, a2) -> {
                            int likes1 = a1.getLikedByUsers() != null ? a1.getLikedByUsers().size() : 0;
                            int likes2 = a2.getLikedByUsers() != null ? a2.getLikedByUsers().size() : 0;
                            return Integer.compare(likes2, likes1);
                        });
                        break;
                }
            }
            
            // Calculate pagination
            int totalCampaigns = orgCampaigns.size();
            int totalPages = (int) Math.ceil((double) totalCampaigns / size);
            
            // Apply pagination
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, totalCampaigns);
            
            List<ActionCharite> paginatedCampaigns = 
                    fromIndex < totalCampaigns ? orgCampaigns.subList(fromIndex, toIndex) : new ArrayList<>();
            
            // Populate categories for paginated campaigns
            for (ActionCharite action : paginatedCampaigns) {
                if (action.getCategorieId() != null) {
                    for (CategorieAction cat : categories) {
                        if (cat.getIdCategorie().equals(action.getCategorieId())) {
                            action.setCategorie(cat);
                            break;
                        }
                    }
                }
            }
            
            // Calculate statistics (use original list for total stats)
            int campaignCount = orgCampaigns.size();
            int participantCount = 0;
            int likeCount = 0;
            float totalDonations = 0f;
            
            // Calculate stats safely
            for (ActionCharite action : orgCampaigns) {
                participantCount += action.getNombreParticipants();
                likeCount += (action.getLikedByUsers() != null) ? action.getLikedByUsers().size() : 0;
                totalDonations += action.getMontantActuel();
            }
            
            // Estimate donors
            int donorCount = Math.max(5, (int)(totalDonations / 500));
            
            // Build URL parameters for pagination links
            StringBuilder searchParams = new StringBuilder();
            if (search != null && !search.isEmpty()) {
                searchParams.append("&search=").append(search);
            }
            if (category != null && !category.isEmpty()) {
                searchParams.append("&category=").append(category);
            }
            if (sort != null && !sort.isEmpty()) {
                searchParams.append("&sort=").append(sort);
            }
            
            // Add to model
            model.addAttribute("organization", organization);
            model.addAttribute("campaigns", paginatedCampaigns);
            model.addAttribute("categories", categories);
            model.addAttribute("campaignCount", campaignCount);
            model.addAttribute("participantCount", participantCount);
            model.addAttribute("donorCount", donorCount);
            model.addAttribute("likeCount", likeCount);
            model.addAttribute("totalDonations", totalDonations);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("searchParams", searchParams.toString());
            model.addAttribute("selectedCategory", category);
            model.addAttribute("selectedSort", sort);
            
            return "Home/organization-details";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/organizations?error=details-error";
        }
    }
    
}