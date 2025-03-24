package org.example.charityproject1.controller;

import jakarta.servlet.http.HttpSession;
import org.example.charityproject1.model.ActionCharite;
import org.example.charityproject1.model.CategorieAction;
import org.example.charityproject1.model.Organisations;
import org.example.charityproject1.model.Utilisateurs;
import org.example.charityproject1.service.ActionChariteService;
import org.example.charityproject1.service.CategorieActionService;
import org.example.charityproject1.service.OrganisationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/organisation/actions")
public class ActionChariteController {

    @Autowired
    private ActionChariteService actionChariteService;

    @Autowired
    private OrganisationsService organisationsService;

    @Autowired
    private CategorieActionService categorieActionService;

    @GetMapping("/create")
    public String showCreateForm(Model model, HttpSession session) {
        String orgId = (String) session.getAttribute("org_identifier");
        if (orgId == null) {
            return "redirect:/auth/login/organisation";
        }

        // Load organisation for navbar
        Organisations organisation = organisationsService.findByNumeroIdentif(orgId);
        model.addAttribute("organisation", organisation);

        // Load categories for the dropdown
        List<CategorieAction> categories = categorieActionService.getAllCategories();
        model.addAttribute("categories", categories);

        // Add this section to load actions for the right panel
        // Get active actions
        List<ActionCharite> actions = actionChariteService.getActiveActionsByOrganisation(orgId);

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

        model.addAttribute("actions", actions);

        return "organisation/actions/create-action";
    }
    @PostMapping("/create")
    public String createAction(
            @RequestParam("titre") String titre,
            @RequestParam("description") String description,
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
            @RequestParam(value = "datelimite", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateLimite,
            @RequestParam("lieu") String lieu,
            @RequestParam("objectifCollecte") float objectifCollecte,
            @RequestParam("categorieId") String categorieId,
            @RequestParam(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        String orgId = (String) session.getAttribute("org_identifier");
        if (orgId == null) {
            return "redirect:/auth/login/organisation";
        }

        // Validate date
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String todayStr = sdf.format(today);
        String dateStr = sdf.format(date);

        if (dateStr.compareTo(todayStr) < 0) {
            model.addAttribute("error", "La date de début doit être aujourd'hui ou une date future.");
            model.addAttribute("categories", categorieActionService.getAllCategories());
            model.addAttribute("organisation", organisationsService.findByNumeroIdentif(orgId));

            // Load actions for the right panel
            List<ActionCharite> actions = actionChariteService.getActiveActionsByOrganisation(orgId);
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
            model.addAttribute("actions", actions);

            return "organisation/actions/create-action";
        }
        if (dateLimite != null && dateLimite.before(date)) {
            model.addAttribute("error", "La date d'expiration doit être égale ou postérieure à la date de début.");
            model.addAttribute("categories", categorieActionService.getAllCategories());
            model.addAttribute("organisation", organisationsService.findByNumeroIdentif(orgId));

            // Load actions for the right panel
            List<ActionCharite> actions = actionChariteService.getActiveActionsByOrganisation(orgId);
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
            model.addAttribute("actions", actions);

            return "organisation/actions/create-action";
        }

        // Create action object
        ActionCharite action = new ActionCharite();
        action.setTitre(titre);
        action.setDescription(description);
        action.setDate(date);
        action.setDatelimite(dateLimite); // Can be null
        action.setLieu(lieu);
        action.setObjectifCollecte(objectifCollecte);
        action.setCategorieId(categorieId);
        action.setOrganisationId(orgId);

        try {
            // Save action with media files
            ActionCharite savedAction = actionChariteService.createActionCharite(action, mediaFiles);
            redirectAttributes.addFlashAttribute("success", "Action caritative créée avec succès");
            return "redirect:/organisation/actions/list";
        } catch (IOException e) {
            // Re-add form data and show error
            model.addAttribute("error", "Erreur lors de la création de l'action: " + e.getMessage());
            model.addAttribute("categories", categorieActionService.getAllCategories());
            model.addAttribute("organisation", organisationsService.findByNumeroIdentif(orgId));

            // Load actions for the right panel
            List<ActionCharite> actions = actionChariteService.getActiveActionsByOrganisation(orgId);
            for (ActionCharite actionCharite : actions) {
                if (actionCharite.getCategorieId() != null) {
                    try {
                        CategorieAction categorie = categorieActionService.getCategoryById(actionCharite.getCategorieId());
                        actionCharite.setCategorie(categorie);
                    } catch (Exception ex) {
                        // Category not found, continue with null categorie
                    }
                }
            }
            model.addAttribute("actions", actions);

            return "organisation/actions/create-action";
        }
    }

    @GetMapping("/list")
    public String listActions(Model model, HttpSession session) {
        String orgId = (String) session.getAttribute("org_identifier");
        if (orgId == null) {
            return "redirect:/login";
        }

        // Load organisation for navbar
        Organisations organisation = organisationsService.findByNumeroIdentif(orgId);
        model.addAttribute("organisation", organisation);

        // Get active actions
        List<ActionCharite> actions = actionChariteService.getActiveActionsByOrganisation(orgId);

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

        model.addAttribute("actions", actions);
        return "organisation/actions/list-actions";
    }

    @GetMapping("/archived")
    public String listArchivedActions(Model model, HttpSession session) {
        String orgId = (String) session.getAttribute("org_identifier");
        if (orgId == null) {
            return "redirect:/auth/login/organisation";
        }

        // Add organisation for navbar
        Organisations organisation = organisationsService.findByNumeroIdentif(orgId);
        model.addAttribute("organisation", organisation);

        // Get archived actions for the organisation
        List<ActionCharite> archivedActions = actionChariteService.getArchivedActionsByOrganisation(orgId);
        model.addAttribute("actions", archivedActions);

        return "organisation/actions/archived-actions";
    }
    @GetMapping("/view/{id}")
    public String viewAction(@PathVariable("id") String id, Model model, HttpSession session) {
        String orgId = (String) session.getAttribute("org_identifier");
        if (orgId == null) {
            return "redirect:/auth/login/organisation";
        }

        // Load organisation for navbar
        Organisations organisation = organisationsService.findByNumeroIdentif(orgId);
        model.addAttribute("organisation", organisation);

        // Get the action details
        ActionCharite action = actionChariteService.getActionById(id);

        // Make sure the action exists and belongs to this organisation
        if (action == null || !action.getOrganisationId().equals(orgId)) {
            return "redirect:/organisation/actions/list";
        }

        // Load category for the action
        if (action.getCategorieId() != null) {
            try {
                CategorieAction categorie = categorieActionService.getCategoryById(action.getCategorieId());
                action.setCategorie(categorie);
            } catch (Exception e) {
                // Category not found, continue with null categorie
            }
        }
        List<Utilisateurs> likedUsers = actionChariteService.getUsersWhoLikedAction(id);
        model.addAttribute("likedUsers", likedUsers);
        model.addAttribute("action", action);

        return "organisation/actions/view-action";
    }
    // ...existing code...

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") String id, Model model, HttpSession session) {
        String orgId = (String) session.getAttribute("org_identifier");
        if (orgId == null) {
            return "redirect:/auth/login/organisation";
        }

        // Load organisation for navbar
        Organisations organisation = organisationsService.findByNumeroIdentif(orgId);
        model.addAttribute("organisation", organisation);

        // Get the action to edit
        ActionCharite action = actionChariteService.getActionById(id);

        // Make sure the action exists and belongs to this organisation
        if (action == null || !action.getOrganisationId().equals(orgId)) {
            return "redirect:/organisation/actions/list";
        }

        // Load categories for the dropdown
        List<CategorieAction> categories = categorieActionService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("action", action);

        return "organisation/actions/edit-action";
    }

    @PostMapping("/edit/{id}")
    public String updateAction(
            @PathVariable("id") String id,
            @RequestParam("titre") String titre,
            @RequestParam("description") String description,
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
            @RequestParam(value = "datelimite", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateLimite,
            @RequestParam("lieu") String lieu,
            @RequestParam("objectifCollecte") float objectifCollecte,
            @RequestParam("categorieId") String categorieId,
            @RequestParam(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles,
            @RequestParam(value = "deleteExistingMedia", required = false, defaultValue = "false") boolean deleteExistingMedia,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        String orgId = (String) session.getAttribute("org_identifier");
        if (orgId == null) {
            return "redirect:/auth/login/organisation";
        }

        // Load organisation for navbar
        Organisations organisation = organisationsService.findByNumeroIdentif(orgId);
        model.addAttribute("organisation", organisation);

        // Get the existing action
        ActionCharite existingAction = actionChariteService.getActionById(id);

        // Make sure the action exists and belongs to this organisation
        if (existingAction == null || !existingAction.getOrganisationId().equals(orgId)) {
            return "redirect:/organisation/actions/list";
        }
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String todayStr = sdf.format(today);
        String dateStr = sdf.format(date);

        if (dateStr.compareTo(todayStr) < 0) {
            redirectAttributes.addFlashAttribute("error", "La date de début doit être aujourd'hui ou une date future.");
            return "redirect:/organisation/actions/edit/" + id;
        }

        if (dateLimite != null && dateLimite.before(date)) {
            redirectAttributes.addFlashAttribute("error", "La date d'expiration doit être égale ou postérieure à la date de début.");
            return "redirect:/organisation/actions/edit/" + id;
        }
        // Update action fields
        existingAction.setTitre(titre);
        existingAction.setDescription(description);
        existingAction.setDate(date);
        existingAction.setDatelimite(dateLimite);
        existingAction.setLieu(lieu);
        existingAction.setObjectifCollecte(objectifCollecte);
        existingAction.setCategorieId(categorieId);

        try {
            // Update action with new media files if provided
            ActionCharite updatedAction = actionChariteService.updateActionCharite(existingAction, mediaFiles, deleteExistingMedia);
            redirectAttributes.addFlashAttribute("success", "Action caritative mise à jour avec succès");
            return "redirect:/organisation/actions/view/" + id;
        } catch (IllegalArgumentException e) {
            // Handle validation errors
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/organisation/actions/edit/" + id;
        } catch (IOException e) {
            // Handle file processing errors
            redirectAttributes.addFlashAttribute("error", "Erreur lors du traitement des fichiers: " + e.getMessage());
            return "redirect:/organisation/actions/edit/" + id;
        } catch (Exception e) {
            // Handle any other unexpected errors
            redirectAttributes.addFlashAttribute("error", "Une erreur est survenue: " + e.getMessage());
            return "redirect:/organisation/actions/edit/" + id;
        }
    }
    // ...existing code...

    @GetMapping("/delete/{id}")
    public String deleteAction(@PathVariable("id") String id, HttpSession session, RedirectAttributes redirectAttributes) {
        String orgId = (String) session.getAttribute("org_identifier");
        if (orgId == null) {
            return "redirect:/auth/login/organisation";
        }

        // Get the action to delete
        ActionCharite action = actionChariteService.getActionById(id);

        // Make sure the action exists and belongs to this organisation
        if (action == null || !action.getOrganisationId().equals(orgId)) {
            return "redirect:/organisation/actions/list";
        }

        // Delete the action
        actionChariteService.deleteAction(id);

        redirectAttributes.addFlashAttribute("success", "Action supprimée avec succès");

        // Redirect to appropriate page based on where the action came from
        if (!action.isActive()) {
            return "redirect:/organisation/actions/archived";
        } else {
            return "redirect:/organisation/actions/list";
        }
    }

    @GetMapping("/archive/{id}")
    public String archiveAction(@PathVariable("id") String id, HttpSession session, RedirectAttributes redirectAttributes) {
        String orgId = (String) session.getAttribute("org_identifier");
        if (orgId == null) {
            return "redirect:/auth/login/organisation";
        }

        // Get the action to archive
        ActionCharite action = actionChariteService.getActionById(id);

        // Make sure the action exists and belongs to this organisation
        if (action == null || !action.getOrganisationId().equals(orgId)) {
            return "redirect:/organisation/actions/list";
        }

        // Archive the action
        action.setActive(false);
        actionChariteService.updateActionStatus(action);

        redirectAttributes.addFlashAttribute("success", "Action archivée avec succès");
        return "redirect:/organisation/actions/list";
    }

    @GetMapping("/activate/{id}")
    public String activateAction(@PathVariable("id") String id, HttpSession session, RedirectAttributes redirectAttributes) {
        String orgId = (String) session.getAttribute("org_identifier");
        if (orgId == null) {
            return "redirect:/auth/login/organisation";
        }

        // Get the action to activate
        ActionCharite action = actionChariteService.getActionById(id);

        // Make sure the action exists and belongs to this organisation
        if (action == null || !action.getOrganisationId().equals(orgId)) {
            return "redirect:/organisation/actions/archived";
        }

        // Activate the action
        action.setActive(true);
        actionChariteService.updateActionStatus(action);

        redirectAttributes.addFlashAttribute("success", "Action réactivée avec succès");
        return "redirect:/organisation/actions/list";
    }


}