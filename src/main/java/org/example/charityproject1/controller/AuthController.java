package org.example.charityproject1.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.charityproject1.model.Organisations;
import org.example.charityproject1.model.SuperAdmin;
import org.example.charityproject1.model.Utilisateurs;
import org.example.charityproject1.repository.OrganisationsRepository;
import org.example.charityproject1.repository.SuperAdminRepository;
import org.example.charityproject1.repository.UtilisateursRepository;
import org.example.charityproject1.repository.DonRepository;
import org.example.charityproject1.repository.ActionChariteRepository;
import org.example.charityproject1.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.Cookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UtilisateursRepository utilisateursRepository;

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private OrganisationsRepository organisationsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private DonRepository donRepository;

    @Autowired
    private ActionChariteRepository actionChariteRepository;

    @GetMapping("/register/user")
    public String showUserRegistrationForm(Model model) {
        // Add organization statistics to the model
        long organisationCount = organisationsRepository.count();
        double totalDonations = donRepository.sumTotalDonations();
        long campaignCount = actionChariteRepository.count();

        model.addAttribute("organisationCount", organisationCount);
        model.addAttribute("totalDonations", totalDonations);
        model.addAttribute("campaignCount", campaignCount);
        
        return "user/register_user"; 
    }

    @PostMapping("/register/user")
    public String registerUser(
            @RequestParam("nom") String nom,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("telephone") String telephone,
            @RequestParam("localisation") String localisation,
            @RequestParam("logo") MultipartFile logo,
            Model model) {

        // Validate email format
        if (email == null || !email.matches("[A-Za-z0-9+_.-]+@(.+)$")) {
            model.addAttribute("message", "Invalid email format");
            return "user/register_user";
        }

        // Check if email already exists
        Optional<Utilisateurs> existingUser = utilisateursRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            model.addAttribute("message", "Error: Email already exists!");
            return "user/register_user";  // Show error message on the registration page
        }

        try {
            // Convert the uploaded logo to Base64
            String logoBase64 = convertFileToBase64(logo);

            // Create a new user
            Utilisateurs user = new Utilisateurs();
            user.setNom(nom);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));  // Make sure to encode the password
            user.setTelephone(telephone);
            user.setLocalisation(localisation);
            user.setLogoPath(logoBase64); // Save Base64 string in the database

            // Save the user to the database
            utilisateursRepository.save(user);

            model.addAttribute("message", "User registered successfully!");
        } catch (Exception e) {
            model.addAttribute("message", "Error registering user: " + e.getMessage());
        }

        return "user/login_user"; // Redirect to login page after successful registration
    }

    // Render super admin registration page
    @GetMapping("/register/superadmin")
    public String showSuperAdminRegistrationForm(Model model) {
        model.addAttribute("parameterName", "value");
        CsrfToken csrfToken = (CsrfToken) model.getAttribute("_csrf");
        if (csrfToken != null) {
            System.out.println("CSRF Token: " + csrfToken.getToken());
        } else {
            System.out.println("CSRF Token is missing!");
        }
        return "superadmin/register_superadmin"; // Path to Thymeleaf template
    }

    @PostMapping("/register/superadmin")
    public String registerSuperAdmin(
            @RequestParam("nom") String nom,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Validate email format
        if (email == null || !email.matches("[A-Za-z0-9+_.-]+@(.+)$")) {
            model.addAttribute("message", "Invalid email format");
            return "superadmin/register_superadmin";
        }
        // Check if email already exists
        Optional<SuperAdmin> existingUser = superAdminRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            model.addAttribute("message", "Error: Email already exists!");
            return "superadmin/register_superadmin";  // Show error message on the registration page
        }
        try {
            SuperAdmin superAdmin = new SuperAdmin();
            superAdmin.setNom(nom);
            superAdmin.setEmail(email);
            superAdmin.setPassword(passwordEncoder.encode(password));
            superAdmin.setAdmin(true);

            superAdminRepository.save(superAdmin);

            // Store admin in session for dashboard access
            session.setAttribute("admin_email", email);

            // Add success message as flash attribute for the dashboard
            redirectAttributes.addFlashAttribute("successMessage", "Compte SuperAdmin créé avec succès!");

            return "redirect:/superadmin/dashboard";
        } catch (Exception e) {
            model.addAttribute("message", "Error registering SuperAdmin: " + e.getMessage());
            return "superadmin/register_superadmin";
        }
    }


    // Render superadmin login page
    @GetMapping("/login/superadmin")
    public String showSuperAdminLoginPage(Model model) {
        return "superadmin/login_superadmin"; // Path to Thymeleaf template
    }

    @PostMapping("/login/superadmin")
    public String loginSuperAdmin(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            Model model,
            HttpSession session) {

        try {
            // Validate that the superadmin exists in the database
            Optional<SuperAdmin> adminOptional = superAdminRepository.findByEmail(username);
            if (!adminOptional.isPresent()) {
                model.addAttribute("message", "SuperAdmin not found with email: " + username);
                return "superadmin/login_superadmin";
            }

            // Authenticate the superadmin
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // Set the authentication in the SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtUtil.generateToken(userDetails);

            // Store the token in session
            session.setAttribute("jwt_token", jwt);

            // Store admin email in session (helpful for retrieving admin data later)
            session.setAttribute("admin_email", username);

            // Redirect to dashboard
            return "redirect:/superadmin/dashboard";

        } catch (Exception e) {
            // If authentication fails, add error message and show login page
            model.addAttribute("message", "Login error: " + e.getMessage());
            return "superadmin/login_superadmin"; // Return the view name, not a redirect
        }
    }
    // Render organisation registration page
    @GetMapping("/register/organisation")
    public String showOrganisationRegistrationForm(Model model) {
        // Add organization statistics to the model
        long organisationCount = organisationsRepository.count();
        double totalDonations = donRepository.sumTotalDonations();
        long campaignCount = actionChariteRepository.count();

        model.addAttribute("organisationCount", organisationCount);
        model.addAttribute("totalDonations", totalDonations);
        model.addAttribute("campaignCount", campaignCount);

        model.addAttribute("parameterName", "value");
        CsrfToken csrfToken = (CsrfToken) model.getAttribute("_csrf");
        if (csrfToken != null) {
            System.out.println("CSRF Token: " + csrfToken.getToken());
        } else {
            System.out.println("CSRF Token is missing!");
        }
        return "organisation/register_organisation"; // Path to Thymeleaf template
    }

    @PostMapping("/register/organisation")
    public String registerOrganisation(
            @RequestParam("nom") String nom,
            @RequestParam("adresseLegale") String adresseLegale,
            @RequestParam("numeroIdentif") String numeroIdentif,
            @RequestParam("description") String description,
            @RequestParam("password") String password,
            @RequestParam("contactPrincipal") String contactPrincipal,
            @RequestParam("logo") MultipartFile logo,
            Model model) {

        try {
            // Check if organisation with same ID already exists
            Optional<Organisations> existingOrg = organisationsRepository.findByNumeroIdentif(numeroIdentif);
            if (existingOrg.isPresent()) {
                model.addAttribute("message", "Error: This organization ID already exists!");
                return "organisation/register_organisation";
            }

            // Rest of your code...
            String logoBase64 = convertFileToBase64(logo);

            // Create a new organisation
            Organisations organisation = new Organisations();
            organisation.setNom(nom);
            organisation.setAdresseLegale(adresseLegale);
            organisation.setNumeroIdentif(numeroIdentif);
            organisation.setDescription(description);
            organisation.setPassword(passwordEncoder.encode(password));
            organisation.setContactPrincipal(contactPrincipal);
            organisation.setLogo(logoBase64);

            organisationsRepository.save(organisation);

            model.addAttribute("message", "Organisation registered successfully");
            return "organisation/login_organisation";
        } catch (Exception e) {
            model.addAttribute("message", "Error registering organisation: " + e.getMessage());
            return "organisation/register_organisation";
        }
    }
 
    @GetMapping("/login/user")
    public String showUserLoginPage(Model model) {
        // Add organization statistics to the model
        long organisationCount = organisationsRepository.count();
        double totalDonations = donRepository.sumTotalDonations();
        long campaignCount = actionChariteRepository.count();

        model.addAttribute("organisationCount", organisationCount);
        model.addAttribute("totalDonations", totalDonations);
        model.addAttribute("campaignCount", campaignCount);
        
        return "user/login_user"; // Path to Thymeleaf template
    }

    @PostMapping("/login/user")
    public String loginUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            Model model,
            HttpSession session) {

        try {
            // Validate that the user exists in the database
            Optional<Utilisateurs> userOptional = utilisateursRepository.findByEmail(username);
            if (!userOptional.isPresent()) {
                model.addAttribute("message", "User not found with email: " + username);
                return "user/login_user";
            }

            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // Set the authentication in the SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtUtil.generateToken(userDetails);

            // Store the token in session
            session.setAttribute("jwt_token", jwt);

            // Store user email in session (helpful for retrieving user data later)
            session.setAttribute("user_email", username);

            // Redirect to dashboard
            return "redirect:/user/dashboard";

        } catch (Exception e) {
            // If authentication fails, add error message and show login page
            model.addAttribute("message", "Login error: " + e.getMessage());
            return "user/login_user"; // Return the view name, not a redirect
        }
    }

    // Render organisation login page
    @GetMapping("/login/organisation")
    public String showOrganisationLoginPage(Model model) {
        // Add organization statistics to the model
        long organisationCount = organisationsRepository.count();
        double totalDonations = donRepository.sumTotalDonations();
        long campaignCount = actionChariteRepository.count();

        model.addAttribute("organisationCount", organisationCount);
        model.addAttribute("totalDonations", totalDonations);
        model.addAttribute("campaignCount", campaignCount);
        
        return "organisation/login_organisation"; // Path to Thymeleaf template
    }

    @PostMapping("/login/organisation")
    public String loginOrganisation(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            Model model,
            HttpSession session) {

        try {
            // Validate that the organisation exists in the database
            Optional<Organisations> orgOptional = organisationsRepository.findByNumeroIdentif(username);
            if (!orgOptional.isPresent()) {
                model.addAttribute("message", "Organisation not found with ID: " + username);
                return "organisation/login_organisation";
            }

            // Authenticate the organization
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // Set the authentication in the SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtUtil.generateToken(userDetails);

            // Store the token in session
            session.setAttribute("jwt_token", jwt);

            // Store organization identifier in session (helpful for retrieving org data later)
            session.setAttribute("org_identifier", username);

            // Redirect to dashboard
            return "redirect:/organisation/dashboard";

        } catch (Exception e) {
            // If authentication fails, add error message and show login page
            model.addAttribute("message", "Login error: " + e.getMessage());
            return "organisation/login_organisation"; // Return the view name, not a redirect
        }
    }
    private String convertFileToBase64(MultipartFile file) throws IOException {
        byte[] fileBytes = file.getBytes(); // Get file content as bytes
        return Base64.getEncoder().encodeToString(fileBytes); // Encode bytes to Base64
    }

    private String saveUploadedFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        try {
            // Save the file to a specific directory (e.g., "uploads")
            String uploadDir = "uploads";
            File uploadPath = new File(uploadDir);
            if (!uploadPath.exists()) {
                uploadPath.mkdirs();
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            File savedFile = new File(uploadDir + "/" + fileName);
            file.transferTo(savedFile);

            return uploadDir + "/" + fileName; // Return the file path
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file", e);
        }
    }



}