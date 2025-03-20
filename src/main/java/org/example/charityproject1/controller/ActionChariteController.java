package org.example.charityproject1.controller;

import jakarta.servlet.http.HttpSession;
import org.example.charityproject1.model.ActionCharite;
import org.example.charityproject1.model.CategorieAction;
import org.example.charityproject1.model.Organisations;
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
            return "organisation/actions/create-action";
        }
    }

    @GetMapping("/list")
    public String listActions(Model model, HttpSession session) {
        String orgId = (String) session.getAttribute("org_identifier");
        if (orgId == null) {
            return "redirect:/auth/login/organisation";
        }

        // Add organisation for navbar
        Organisations organisation = organisationsService.findByNumeroIdentif(orgId);
        model.addAttribute("organisation", organisation);

        // Get active actions for the organisation
        List<ActionCharite> activeActions = actionChariteService.getActiveActionsByOrganisation(orgId);
        model.addAttribute("actions", activeActions);

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
}