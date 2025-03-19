document.addEventListener('DOMContentLoaded', function() {
    console.log("Admin dashboard script loaded"); // Debug line

    // Dropdown functionality
    const dropdownToggle = document.getElementById('dropdownToggle');
    const dropdownContent = document.querySelector('.dropdown-content');

    // Check if dropdown elements exist
    if (dropdownToggle && dropdownContent) {
        // Toggle dropdown when clicking the parameter icon
        dropdownToggle.addEventListener('click', function(e) {
            e.preventDefault();
            console.log("Dropdown toggle clicked"); // Debug line
            dropdownContent.classList.toggle('show');
        });

        // Close dropdown when clicking outside
        window.addEventListener('click', function(e) {
            if (!e.target.matches('.param-icon') && !e.target.matches('.fa-cog')) {
                if (dropdownContent.classList.contains('show')) {
                    dropdownContent.classList.remove('show');
                }
            }
        });
    } else {
        console.error("Dropdown elements not found"); // Debug line
    }

    // Profile Modal functionality
    const modal = document.getElementById('profileModal');
    const showProfileBtn = document.getElementById('showProfileModal');
    const closeBtn = modal ? modal.querySelector('.close') : null;

    const profileView = document.getElementById('profileView');
    const editProfileForm = document.getElementById('editProfileForm');
    const passwordForm = document.getElementById('passwordForm');

    const modifyBtn = document.getElementById('modifyBtn');
    const passwordBtn = document.getElementById('passwordBtn');
    const cancelEditBtn = document.getElementById('cancelEditBtn');
    const cancelPasswordBtn = document.getElementById('cancelPasswordBtn');

    // Check for flash attributes via data attributes
    const passwordError = document.body.getAttribute('data-password-error');
    const profileError = document.body.getAttribute('data-profile-error');

    // Check if modal elements exist
    if (modal && profileView) {
        // Check if we need to show error forms
        if (passwordError && passwordError !== 'null') {
            modal.style.display = 'block';
            profileView.style.display = 'none';
            if (editProfileForm) editProfileForm.style.display = 'none';
            if (passwordForm) passwordForm.style.display = 'block';
        } else if (profileError && profileError !== 'null') {
            modal.style.display = 'block';
            profileView.style.display = 'none';
            if (editProfileForm) editProfileForm.style.display = 'block';
            if (passwordForm) passwordForm.style.display = 'none';
        }

        // Show modal when clicking "Modifier profil"
        if (showProfileBtn) {
            showProfileBtn.addEventListener('click', function(e) {
                e.preventDefault();
                console.log("Show profile button clicked"); // Debug line
                modal.style.display = 'block';
                // Always show the profile view first
                profileView.style.display = 'block';
                if (editProfileForm) editProfileForm.style.display = 'none';
                if (passwordForm) passwordForm.style.display = 'none';

                // Close the dropdown
                if (dropdownContent) {
                    dropdownContent.classList.remove('show');
                }
            });
        }

        // Close modal when clicking on X
        if (closeBtn) {
            closeBtn.addEventListener('click', function() {
                modal.style.display = 'none';
            });
        }

        // Close modal when clicking outside
        window.addEventListener('click', function(e) {
            if (e.target == modal) {
                modal.style.display = 'none';
            }
        });

        // Show edit profile form when clicking "Modifier" button
        if (modifyBtn && editProfileForm) {
            modifyBtn.addEventListener('click', function() {
                profileView.style.display = 'none';
                editProfileForm.style.display = 'block';
                if (passwordForm) passwordForm.style.display = 'none';
            });
        }

        // Show password form when clicking "Modifier mot de passe" button
        if (passwordBtn && passwordForm) {
            passwordBtn.addEventListener('click', function() {
                profileView.style.display = 'none';
                if (editProfileForm) editProfileForm.style.display = 'none';
                passwordForm.style.display = 'block';
            });
        }

        // Return to profile view when clicking cancel buttons
        if (cancelEditBtn) {
            cancelEditBtn.addEventListener('click', function() {
                profileView.style.display = 'block';
                if (editProfileForm) editProfileForm.style.display = 'none';
                if (passwordForm) passwordForm.style.display = 'none';
            });
        }

        if (cancelPasswordBtn) {
            cancelPasswordBtn.addEventListener('click', function() {
                profileView.style.display = 'block';
                if (editProfileForm) editProfileForm.style.display = 'none';
                if (passwordForm) passwordForm.style.display = 'none';
            });
        }
    } else {
        console.error("Modal elements not found"); // Debug line
    }

    // Check for success message flash attribute
    const successMessage = document.querySelector('.alert-success');
    if (successMessage) {
        // Auto-hide success message after 5 seconds
        setTimeout(function() {
            successMessage.style.display = 'none';
        }, 5000);
    }
});
// Organization functions
function validateOrganisation(id) {
    fetch(`/superadmin/api/organisations/${id}/validate`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json())
        .then(data => {
            if (data.status === 'success') {
                // Reload organizations content to show updated status
                loadOrganisationsContent();
            } else {
                alert('Error: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error validating organization:', error);
            alert('An error occurred while validating the organization.');
        });
}

function deleteOrganisation(id) {
    if (confirm('Are you sure you want to delete this organization?')) {
        fetch(`/superadmin/api/organisations/${id}`, {
            method: 'DELETE'
        })
            .then(response => {
                if (response.ok) {
                    // Reload organizations content to remove deleted organization
                    loadOrganisationsContent();
                } else {
                    alert('Error deleting organization');
                }
            })
            .catch(error => {
                console.error('Error deleting organization:', error);
                alert('An error occurred while deleting the organization.');
            });
    }
}




function loadOrganisationsContent() {
    // Hide other content
    document.getElementById('dashboard-content').style.display = 'none';
    document.getElementById('organisations-content').style.display = 'block';
    document.getElementById('charities-content').style.display = 'none';
    document.getElementById('users-content').style.display = 'none';

    // Update active tab
    setActiveTab('organisations-nav');

    // Fetch organisations content
    fetch('/superadmin/organisations-content')
        .then(response => response.text())
        .then(html => {
            document.getElementById('organisations-content').innerHTML = html;
        })
        .catch(error => {
            console.error('Error loading organisations content:', error);
            document.getElementById('organisations-content').innerHTML = '<div class="error-message">Error loading content. Please try again.</div>';
        });
}

function loadCharitiesContent() {
    document.getElementById('dashboard-content').style.display = 'none';
    document.getElementById('organisations-content').style.display = 'none';
    document.getElementById('charities-content').style.display = 'block';
    document.getElementById('users-content').style.display = 'none';

    setActiveTab('charities-nav');
    // Fetch charities content here
}

function loadUsersContent() {
    document.getElementById('dashboard-content').style.display = 'none';
    document.getElementById('organisations-content').style.display = 'none';
    document.getElementById('charities-content').style.display = 'none';
    document.getElementById('users-content').style.display = 'block';

    setActiveTab('users-nav');
    // Fetch users content here
}

function setActiveTab(activeTabId) {
    // Remove active class from all tabs
    document.querySelectorAll('.sub-nav-item').forEach(item => {
        item.classList.remove('active');
    });

    // Add active class to current tab
    document.getElementById(activeTabId).classList.add('active');
}

// Initialize dashboard on page load
document.addEventListener('DOMContentLoaded', function() {
    // Check if URL has a hash and load the appropriate content
    const hash = window.location.hash;
    if (hash === '#organisations') {
        loadOrganisationsContent();
    } else if (hash === '#charities') {
        loadCharitiesContent();
    } else if (hash === '#users') {
        loadUsersContent();
    } else {
        loadDashboardContent();
    }
});

// Organization detail modal functions
function showOrganizationDetails(element) {
    const orgId = element.getAttribute('data-org-id');
    const modal = document.getElementById('org-details-modal');

    // Get the organization details
    fetch(`/superadmin/api/organisations/${orgId}`)
        .then(response => {
            if (!response.ok) throw new Error('Network response was not ok');
            return response.json();
        })
        .then(org => {
            // Format registration date
            const registrationDate = new Date(org.dateInscription).toLocaleDateString('fr-FR', {
                day: 'numeric',
                month: 'long',
                year: 'numeric'
            });

            // Create HTML for organization details
            let html = `
                <div class="org-detail-header">
                    <div class="org-detail-logo">
                        ${org.logo ? `<img src="data:image/png;base64,${org.logo}" alt="Logo de ${org.nom}">` :
                '<i class="fas fa-building fa-3x" style="color: #2c3e50;"></i>'}
                    </div>
                    <div class="org-detail-title">
                        <h2>${org.nom}</h2>
                        <span class="validation-badge ${org.valideParAdmin ? 'badge-validated' : 'badge-pending'}">
                            ${org.valideParAdmin ? 'Validée' : 'En attente de validation'}
                        </span>
                    </div>
                </div>
                
                <div class="org-detail-section">
                    <h3>Informations générales</h3>
                    <div class="org-detail-grid">
                        <div class="org-detail-item">
                            <div class="org-detail-label">Numéro d'identification</div>
                            <div class="org-detail-value">${org.numeroIdentif}</div>
                        </div>
                       
                        <div class="org-detail-item">
                            <div class="org-detail-label">Contact Principal</div>
                            <div class="org-detail-value">${org.contactPrincipal || 'Non spécifié'}</div>
                        </div>
                       
                    </div>
                </div>
                
                <div class="org-detail-section">
                    <h3>Localisation</h3>
                    <div class="org-detail-item">
                        <div class="org-detail-label">Adresse légale</div>
                        <div class="org-detail-value">${org.adresseLegale || 'Non spécifiée'}</div>
                    </div>
                </div>
                
                <div class="org-detail-section">
                    <h3>Description</h3>
                    <p class="org-description">${org.description || 'Aucune description disponible.'}</p>
                </div>
                
                <div class="org-detail-section">
                    <div class="action-buttons">
                        ${!org.valideParAdmin ?
                `<button class="action-btn validate-btn" onclick="validateOrganisation('${org.numeroIdentif}')">
                            <i class="fas fa-check"></i> Valider
                        </button>` : ''}
                        <button class="action-btn delete-btn" onclick="deleteOrganisation('${org.numeroIdentif}')">
                            <i class="fas fa-trash"></i> Supprimer
                        </button>
                    </div>
                </div>
            `;

            document.getElementById('org-details-container').innerHTML = html;
            modal.style.display = 'block';
        })
        .catch(error => {
            console.error('Error fetching organization details:', error);
            alert('Une erreur est survenue lors du chargement des détails de l\'organisation.');
        });



    // Get the <span> element that closes the modal
    const span = document.getElementsByClassName('close-modal')[0];

    // When the user clicks on <span> (x), close the modal
    span.onclick = function() {
        modal.style.display = 'none';
    };

    // When the user clicks anywhere outside of the modal, close it
    window.onclick = function(event) {
        if (event.target == modal) {
            modal.style.display = 'none';
        }
    };
}