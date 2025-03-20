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
// User Management Functions

function loadUsersContent() {
    // Hide other content
    document.getElementById('dashboard-content').style.display = 'none';
    document.getElementById('organisations-content').style.display = 'none';
    document.getElementById('charities-content').style.display = 'none';
    document.getElementById('users-content').style.display = 'block';

    // Update active tab
    setActiveTab('users-nav');

    // Fetch users content
    fetch('/superadmin/utilisateurs-content')
        .then(response => response.text())
        .then(html => {
            document.getElementById('users-content').innerHTML = html;
        })
        .catch(error => {
            console.error('Error loading users content:', error);
            document.getElementById('users-content').innerHTML = '<div class="error-message">Error loading content. Please try again.</div>';
        });
}

// User detail modal functions
function showUserDetails(element) {
    const userId = element.getAttribute('data-user-id');
    const modal = document.getElementById('user-details-modal');

    // Get the user details
    fetch(`/superadmin/api/utilisateurs/${userId}`)
        .then(response => {
            if (!response.ok) throw new Error('Network response was not ok');
            return response.json();
        })
        .then(user => {
            // Create HTML for user details with all attributes
            let html = `
                <div class="user-detail-header">
                    <div class="user-detail-avatar">
                       ${user.logoPath ? `<img src="data:image/png;base64,${user.logoPath }" alt="Logo de ${user.nom }">` :
                '<i class="fas fa-building fa-3x" style="color: #2c3e50;"></i>'}
                    </div>
                    <div class="user-detail-title">
                        <h2>${user.nom}</h2>
                        <div class="user-detail-id"> ${user.email}</div>
                        <div class="user-location">
                            <i class="fas fa-map-marker-alt"></i>
                            <span>${user.localisation}</span>
                        </div>
                    </div>
                </div>
                
                <div class="user-detail-section">
                    <h3>Informations de contact</h3>
                    <div class="user-detail-grid">
                        <div class="user-detail-item">
                            <div class="user-detail-label">Email</div>
                            <div class="user-detail-value">${user.email}</div>
                        </div>
                        
                        <div class="user-detail-item">
                            <div class="user-detail-label">Téléphone</div>
                            <div class="user-detail-value">${user.telephone}</div>
                        </div>
                        
                        <div class="user-detail-item">
                            <div class="user-detail-label">Localisation</div>
                            <div class="user-detail-value">${user.localisation}</div>
                        </div>
                    </div>
                </div>
                
                <div class="user-detail-section">
                    <h3>Historique des dons</h3>
                    ${user.historiqueDons && user.historiqueDons.length > 0 ?
                `<div class="donation-history">
                            ${user.historiqueDons.map(don => `
                                <div class="donation-item">
                                    <div class="donation-info">
                                        <div class="donation-title">${don.actionChariteNom || 'Action de charité'}</div>
                                        <div class="donation-date">${new Date(don.date).toLocaleDateString('fr-FR')}</div>
                                    </div>
                                    <div class="donation-amount">${don.montant} €</div>
                                </div>
                            `).join('')}
                        </div>` :
                '<p>Aucun don effectué par cet utilisateur.</p>'
            }
                </div>
                
                <div class="user-detail-section">
                    <h3>Actions aimées</h3>
                    ${user.likedActions && user.likedActions.length > 0 ?
                `<div class="liked-actions">
                            <div class="user-detail-value">
                                ${user.likedActions.map(action => `
                                    <span class="liked-action-badge">${action}</span>
                                `).join(' ')}
                            </div>
                        </div>` :
                '<p>Aucune action aimée par cet utilisateur.</p>'
            }
                </div>
                
                <div class="user-detail-section">
                    <div class="action-buttons">
                        <button class="action-btn edit-btn" onclick="editUser('${user.userId}')">
                            <i class="fas fa-edit"></i> Modifier
                        </button>
                        <button class="action-btn delete-btn" onclick="deleteUser('${user.userId}')">
                            <i class="fas fa-trash"></i> Supprimer
                        </button>
                    </div>
                </div>
            `;

            document.getElementById('user-details-container').innerHTML = html;
            modal.style.display = 'block';
        })
        .catch(error => {
            console.error('Error fetching user details:', error);
            alert('Une erreur est survenue lors du chargement des détails de l\'utilisateur.');
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



/* User Management Functions */
// Edit user function - complete implementation
function editUser(userId) {
    console.log("Edit user function called with ID:", userId);

    // Close details modal if it's open
    const detailsModal = document.getElementById('user-details-modal');
    if (detailsModal) {
        detailsModal.style.display = 'none';
    }

    // Clear any previous errors
    clearFormErrors();

    // Show loading indicator
    const errorContainer = document.getElementById('userEditError');
    if (errorContainer) {
        errorContainer.textContent = 'Chargement des données utilisateur...';
        errorContainer.style.backgroundColor = '#e2f3f5';
        errorContainer.style.color = '#333';
        errorContainer.style.borderColor = '#b3e0e5';
        errorContainer.style.display = 'block';
    }

    // Get user data to populate the form
    fetch(`/superadmin/api/utilisateurs/${userId}`)
        .then(response => {
            // Hide loading message
            if (errorContainer) {
                errorContainer.style.display = 'none';
            }

            if (!response.ok) {
                throw new Error(`Server responded with status: ${response.status}`);
            }
            return response.json();
        })
        .then(user => {
            console.log("Fetched user data:", user);

            // Set the form fields
            document.getElementById('editUserId').value = user.userId;
            document.getElementById('editNom').value = user.nom || '';
            document.getElementById('editEmail').value = user.email || '';
            document.getElementById('originalEmail').value = user.email || '';
            document.getElementById('editTelephone').value = user.telephone || '';
            document.getElementById('editLocalisation').value = user.localisation || '';

            // Set the current logo
            const logoContainer = document.getElementById('currentUserLogo');
            if (logoContainer) {
                if (user.logoPath) {
                    logoContainer.innerHTML = `<img src="data:image/png;base64,${user.logoPath}" alt="Logo actuel">`;
                } else {
                    logoContainer.innerHTML = `<i class="fas fa-user fa-3x"></i>`;
                }
            }

            // Show the edit modal
            const editModal = document.getElementById('edit-user-modal');
            if (editModal) {
                editModal.style.display = 'block';

                // Setup close button
                const closeBtn = editModal.querySelector('.close-modal');
                if (closeBtn) {
                    closeBtn.onclick = function() {
                        editModal.style.display = 'none';
                    };
                }

                // Setup cancel button
                const cancelBtn = document.getElementById('cancelEditUser');
                if (cancelBtn) {
                    cancelBtn.onclick = function() {
                        editModal.style.display = 'none';
                    };
                }

                // Setup form submission
                const form = document.getElementById('userEditForm');
                if (form) {
                    form.onsubmit = function(e) {
                        e.preventDefault();
                        updateUser(form);
                    };
                }
            } else {
                console.error("Could not find edit modal element");
                alert('Une erreur est survenue: Modal introuvable');
            }
        })
        .catch(error => {
            console.error('Error fetching user for editing:', error);

            // Show error message in the error container
            if (errorContainer) {
                errorContainer.textContent = 'Une erreur est survenue lors du chargement des informations de l\'utilisateur: ' + error.message;
                errorContainer.style.backgroundColor = '#f8d7da';
                errorContainer.style.color = '#721c24';
                errorContainer.style.borderColor = '#f5c6cb';
                errorContainer.style.display = 'block';
            } else {
                alert('Une erreur est survenue lors du chargement des informations de l\'utilisateur: ' + error.message);
            }
        });

    // When user clicks outside the modal, close it
    window.onclick = function(event) {
        const modal = document.getElementById('edit-user-modal');
        if (event.target == modal) {
            modal.style.display = 'none';
        }
    };
}
function updateUser(form) {
    console.log("updateUser function called");

    // Reset previous error states
    clearFormErrors();

    // Form validation
    let isValid = true;

    // Validate name
    const nameField = document.getElementById('editNom');
    if (!nameField.value.trim()) {
        showFieldError(nameField, 'editNom-error', 'Le nom est obligatoire');
        isValid = false;
    }

    // Validate email
    const emailField = document.getElementById('editEmail');
    if (!emailField.value.trim() || !emailField.value.match(/^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/)) {
        showFieldError(emailField, 'editEmail-error', 'Veuillez saisir un email valide');
        isValid = false;
    }

    // Validate phone
    const phoneField = document.getElementById('editTelephone');
    if (!phoneField.value.match(/^[0-9]{10}$/)) {
        showFieldError(phoneField, 'editTelephone-error', 'Le téléphone doit contenir 10 chiffres');
        isValid = false;
    }

    // Validate location
    const locationField = document.getElementById('editLocalisation');
    if (!locationField.value.trim()) {
        showFieldError(locationField, 'editLocalisation-error', 'La localisation est obligatoire');
        isValid = false;
    }

    // Check file if provided
    const fileField = document.getElementById('logoFile');
    if (fileField.files.length > 0) {
        const file = fileField.files[0];

        // Check file size (max 2MB)
        if (file.size > 2 * 1024 * 1024) {
            showFieldError(fileField, 'logoFile-error', 'La taille de la photo ne doit pas dépasser 2MB');
            isValid = false;
        }

        // Check file type
        if (!file.type.match(/^image\/(jpeg|png)$/)) {
            showFieldError(fileField, 'logoFile-error', 'Le format de la photo doit être JPG ou PNG');
            isValid = false;
        }
    }

    if (!isValid) {
        return false;
    }

    const userId = document.getElementById('editUserId').value;
    const formData = new FormData(form);

    console.log("Updating user with ID:", userId);

    fetch(`/superadmin/api/utilisateurs/${userId}`, {
        method: 'PUT',
        body: formData
    })
        .then(response => {
            if (response.ok) {
                return response.json();
            } else {
                // If response is not OK, parse the error JSON
                return response.json().then(errorData => {
                    throw errorData;
                });
            }
        })
        .then(result => {
            console.log("Update successful:", result);
            // Close the modal
            const modal = document.getElementById('edit-user-modal');
            if (modal) modal.style.display = 'none';

            // Show success message
            alert('Utilisateur mis à jour avec succès!');

            // Reload users list
            loadUsersContent();
        })
        .catch(errorData => {
            console.error('Error updating user:', errorData);

            // Handle email exists error
            if (errorData.error === 'EMAIL_EXISTS') {
                showFieldError(document.getElementById('editEmail'), 'editEmail-error',
                    'Cet email est déjà utilisé par un autre utilisateur');
            }
            // Handle other field errors
            else if (errorData.fieldErrors) {
                for (const [field, message] of Object.entries(errorData.fieldErrors)) {
                    const fieldId = 'edit' + field.charAt(0).toUpperCase() + field.slice(1);
                    showFieldError(document.getElementById(fieldId), `${fieldId}-error`, message);
                }
            }
            // Handle general errors
            else {
                const errorContainer = document.getElementById('userEditError');
                errorContainer.textContent = errorData.message || 'Une erreur est survenue lors de la mise à jour de l\'utilisateur';
                errorContainer.style.display = 'block';
            }
        });
}
// Helper function to show field error
function showFieldError(field, errorElementId, message) {
    field.classList.add('is-invalid');
    const errorElement = document.getElementById(errorElementId);
    if (errorElement) {
        errorElement.textContent = message;
        errorElement.style.display = 'block';
    }
}

// Helper function to clear all form errors
function clearFormErrors() {
    // Hide error container
    const errorContainer = document.getElementById('userEditError');
    if (errorContainer) {
        errorContainer.style.display = 'none';
    }

    // Clear field errors
    const fields = document.querySelectorAll('.form-control');
    fields.forEach(field => {
        field.classList.remove('is-invalid');
    });

    // Hide error messages
    const errorMessages = document.querySelectorAll('.invalid-feedback');
    errorMessages.forEach(msg => {
        msg.style.display = 'none';
    });
}
// Delete user function
function deleteUser(userId) {
    // Confirm deletion
    if (confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur? Cette action est irréversible.')) {
        fetch(`/superadmin/api/utilisateurs/${userId}`, {
            method: 'DELETE'
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }

                // Close modal if open
                const modal = document.getElementById('user-details-modal');
                if (modal) modal.style.display = 'none';

                // Show success message
                alert('Utilisateur supprimé avec succès!');

                // Reload users list
                loadUsersContent();
            })
            .catch(error => {
                console.error('Error deleting user:', error);
                alert('Une erreur est survenue lors de la suppression de l\'utilisateur.');
            });
    }
}