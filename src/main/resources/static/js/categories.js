// Add these functions to match the new class names

function showAddCategoryModal() {
    const modal = document.getElementById('add-category-modal');
    if (!modal) {
        console.error("Add category modal not found");
        return;
    }

    modal.style.display = 'block';

    // Reset form and clear errors
    const form = document.getElementById('addCategoryForm');
    if (form) {
        form.reset();
    }

    const errorElement = document.getElementById('categoryName-error');
    if (errorElement) {
        errorElement.style.display = 'none';
    }

    const nameField = document.getElementById('categoryName');
    if (nameField) {
        nameField.classList.remove('is-invalid');
    }
}

function closeAddCategoryModal() {
    const modal = document.getElementById('add-category-modal');
    if (modal) {
        modal.style.display = 'none';
    }
}

function editCategory(id, name) {
    const modal = document.getElementById('edit-category-modal');
    if (!modal) {
        console.error("Edit category modal not found");
        return;
    }

    // Set form values
    document.getElementById('editCategoryId').value = id;
    document.getElementById('editCategoryName').value = name;

    // Clear any previous errors
    const errorElement = document.getElementById('editCategoryName-error');
    if (errorElement) {
        errorElement.style.display = 'none';
    }

    // Show modal
    modal.style.display = 'block';
}

function closeEditCategoryModal() {
    const modal = document.getElementById('edit-category-modal');
    if (modal) {
        modal.style.display = 'none';
    }
}

function confirmDeleteCategory(id, name) {
    const modal = document.getElementById('delete-category-modal');
    if (!modal) {
        console.error("Delete category modal not found");
        return;
    }

    // Set the category name in the confirmation message
    const nameSpan = document.getElementById('categoryToDelete');
    if (nameSpan) {
        nameSpan.textContent = name;
    }

    // Set up the confirm delete button
    const confirmBtn = document.getElementById('confirmDeleteBtn');
    if (confirmBtn) {
        // Remove previous event listeners
        const newBtn = confirmBtn.cloneNode(true);
        confirmBtn.parentNode.replaceChild(newBtn, confirmBtn);

        // Add new event listener
        newBtn.addEventListener('click', function() {
            deleteCategory(id);
        });
    }

    // Show the modal
    modal.style.display = 'block';
}

function closeDeleteCategoryModal() {
    const modal = document.getElementById('delete-category-modal');
    if (modal) {
        modal.style.display = 'none';
    }
}
// Add missing category management functions

/**
 * Add a new category
 * @param {HTMLFormElement} form - The form element containing category data
 */
// Update the addCategory function
function addCategory(form) {
    console.log("addCategory function called");

    // Get form data
    const formData = new FormData(form);
    const categoryName = formData.get('nom');

    console.log("Category name to be added:", categoryName);

    // Validate form data
    if (!categoryName || categoryName.trim() === '') {
        const errorElement = document.getElementById('categoryName-error');
        if (errorElement) {
            errorElement.textContent = 'Le nom de la catégorie est obligatoire';
            errorElement.style.display = 'block';
        }
        document.getElementById('categoryName').classList.add('is-invalid');
        return;
    }

    // Create a proper category object that matches your model
    const categoryData = {
        nom: categoryName
    };

    console.log("Sending category data:", JSON.stringify(categoryData));

    // Submit form data with error handling
    fetch('/superadmin/api/categories', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(categoryData)
    })
        .then(response => {
            console.log("Response status:", response.status);
            return response.text().then(text => {
                // Try to parse as JSON, but don't fail if it's not valid JSON
                try {
                    return JSON.parse(text);
                } catch (e) {
                    console.error("Response is not valid JSON:", text);
                    throw new Error("Invalid response format: " + text);
                }
            });
        })
        .then(data => {
            console.log("Category created successfully:", data);
            closeAddCategoryModal();
            alert('Catégorie ajoutée avec succès');
            loadCategoriesContent();
        })
        .catch(error => {
            console.error('Error adding category:', error);
            alert('Erreur: ' + error.message);
        });
}
/**
 * Update an existing category
 * @param {HTMLFormElement} form - The form element containing updated category data
 */
function updateCategory(form) {
    // Get form data
    const formData = new FormData(form);
    const categoryId = formData.get('idCategorie');
    const categoryData = {
        idCategorie: categoryId,
        nom: formData.get('nom')
    };

    // Validate form data
    if (!categoryData.nom || categoryData.nom.trim() === '') {
        const errorElement = document.getElementById('editCategoryName-error');
        if (errorElement) {
            errorElement.textContent = 'Le nom de la catégorie est obligatoire';
            errorElement.style.display = 'block';
        }
        document.getElementById('editCategoryName').classList.add('is-invalid');
        return;
    }

    // Submit form data
    fetch(`/superadmin/api/categories/${categoryId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(categoryData)
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.message || 'Erreur lors de la mise à jour de la catégorie');
                });
            }
            return response.json();
        })
        .then(data => {
            // Close modal
            closeEditCategoryModal();

            // Show success message
            alert('Catégorie mise à jour avec succès');

            // Reload categories
            loadCategoriesContent();
        })
        .catch(error => {
            console.error('Error updating category:', error);
            alert('Erreur: ' + error.message);
        });
}

/**
 * Delete a category
 * @param {string} id - The category ID to delete
 */
function deleteCategory(id) {
    fetch(`/superadmin/api/categories/${id}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.message || 'Erreur lors de la suppression');
                });
            }
            return response.json();
        })
        .then(data => {
            // Close modal
            closeDeleteCategoryModal();

            // Show success message
            alert('Catégorie supprimée avec succès');

            // Reload categories
            loadCategoriesContent();
        })
        .catch(error => {
            console.error('Error deleting category:', error);
            alert('Erreur: ' + error.message);
        });
}

// Add loadCategoriesContent function if not already in admin-dashboard.js
function loadCategoriesContent() {
    // Hide all content sections
    document.querySelectorAll('#content-container > div').forEach(function(section) {
        section.style.display = 'none';
    });

    // Show categories section
    const categoriesSection = document.getElementById('categories-content');
    if (categoriesSection) {
        categoriesSection.style.display = 'block';
    }

    // Set active tab
    setActiveTab('categories-nav');

    // Load categories content via AJAX
    fetch('/superadmin/categories-content')
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.text();
        })
        .then(html => {
            if (categoriesSection) {
                categoriesSection.innerHTML = html;
            }
        })
        .catch(error => {
            console.error('Error loading categories content:', error);
            if (categoriesSection) {
                categoriesSection.innerHTML =
                    `<div class="error-message">
                        Error loading content: ${error.message}. 
                        Please check browser console for details.
                    </div>`;
            }
        });
}