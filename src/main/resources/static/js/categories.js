// Modal Functions
function showAddCategoryModal() {
    const modal = document.getElementById('add-category-modal');
    if (!modal) {
        console.error('Add category modal not found');
        return;
    }

    modal.style.display = 'block';

    // Reset form and clear errors
    const form = document.getElementById('addCategoryForm');
    if (form) {
        form.reset();
    }

    clearAddCategoryErrors();
}

function closeAddCategoryModal() {
    clearAddCategoryErrors();
    const form = document.getElementById('addCategoryForm');
    if (form) {
        form.reset();
    }
    const modal = document.getElementById('add-category-modal');
    if (modal) {
        modal.style.display = 'none';
    }
}

function editCategory(id, name) {
    const modal = document.getElementById('edit-category-modal');
    if (!modal) {
        console.error('Edit category modal not found');
        return;
    }

    // Set form values
    document.getElementById('editCategoryId').value = id;
    document.getElementById('editCategoryName').value = name;

    // Clear any previous errors
    clearEditCategoryErrors();

    // Show modal
    modal.style.display = 'block';
}

function closeEditCategoryModal() {
    clearEditCategoryErrors();
    const modal = document.getElementById('edit-category-modal');
    if (modal) {
        modal.style.display = 'none';
    }
}

function confirmDeleteCategory(id, name) {
    const modal = document.getElementById('delete-category-modal');
    if (!modal) {
        console.error('Delete category modal not found');
        return;
    }

    // Set the category name in the confirmation message
    const nameSpan = document.getElementById('categoryToDelete');
    if (nameSpan) {
        nameSpan.textContent = name;
    }

    // Remove any previous error messages
    const modalContent = document.querySelector('#delete-category-modal .cat-modal-content');
    if (modalContent) {
        const existingError = modalContent.querySelector('.cat-error-message');
        if (existingError) {
            existingError.remove();
        }
    }

    // Set up the confirm delete button
    const confirmBtn = document.getElementById('confirmDeleteBtn');
    if (confirmBtn) {
        confirmBtn.onclick = function() {
            deleteCategory(id);
        };
    }

    // Show the modal
    modal.style.display = 'block';
}

function closeDeleteCategoryModal() {
    // Remove any error messages
    const modalContent = document.querySelector('#delete-category-modal .cat-modal-content');
    if (modalContent) {
        const existingError = modalContent.querySelector('.cat-error-message');
        if (existingError) {
            existingError.remove();
        }
    }

    const modal = document.getElementById('delete-category-modal');
    if (modal) {
        modal.style.display = 'none';
    }
}

// Error Handling Functions
function showAddCategoryError(message) {
    const errorElement = document.getElementById('categoryName-error');
    const inputElement = document.getElementById('categoryName');

    if (errorElement) {
        errorElement.textContent = message;
        errorElement.style.display = 'block';
    }

    if (inputElement) {
        inputElement.classList.add('is-invalid');
    }
}

function clearAddCategoryErrors() {
    const errorElement = document.getElementById('categoryName-error');
    const inputElement = document.getElementById('categoryName');

    if (errorElement) {
        errorElement.textContent = '';
        errorElement.style.display = 'none';
    }

    if (inputElement) {
        inputElement.classList.remove('is-invalid');
    }
}

function showEditCategoryError(message) {
    const errorElement = document.getElementById('editCategoryName-error');
    const inputElement = document.getElementById('editCategoryName');

    if (errorElement) {
        errorElement.textContent = message;
        errorElement.style.display = 'block';
    }

    if (inputElement) {
        inputElement.classList.add('is-invalid');
    }
}

function clearEditCategoryErrors() {
    const errorElement = document.getElementById('editCategoryName-error');
    const inputElement = document.getElementById('editCategoryName');

    if (errorElement) {
        errorElement.textContent = '';
        errorElement.style.display = 'none';
    }

    if (inputElement) {
        inputElement.classList.remove('is-invalid');
    }
}

// CRUD Operations
function addCategory(form) {
    console.log("addCategory function called");

    // Clear previous errors
    clearAddCategoryErrors();

    // Get form data
    const formData = new FormData(form);
    const categoryName = formData.get('nom');

    console.log("Category name to be added:", categoryName);

    // Validate form data
    if (!categoryName || categoryName.trim() === '') {
        showAddCategoryError('Le nom de la catégorie est obligatoire');
        return;
    }

    // Create a proper category object that matches your model
    const categoryData = {
        nom: categoryName
    };

    console.log("Sending category data:", JSON.stringify(categoryData));

    // Submit form data with improved error handling
    fetch('/superadmin/api/categories', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(categoryData)
    })
        .then(response => {
            console.log("Response status:", response.status);

            // First check if the response is ok
            if (!response.ok) {
                return response.text().then(text => {
                    try {
                        // Extract just the message from the JSON
                        const parsedError = JSON.parse(text);
                        return parsedError.message || 'Error creating category';
                    } catch (e) {
                        // If not JSON or other parsing error
                        return text;
                    }
                });
            }

            return response.json();
        })
        .then(data => {
            // Check if this is an error message string
            if (typeof data === 'string') {
                throw data; // This will be caught by the catch block
            }

            console.log("Category created successfully:", data);
            closeAddCategoryModal();
            loadCategoriesContent();
        })
        .catch(error => {
            console.error('Error adding category:', error);
            showAddCategoryError(error); // Now error is just the message string
        });
}

function updateCategory(form) {
    // Clear previous errors
    clearEditCategoryErrors();

    // Get form data
    const formData = new FormData(form);
    const categoryId = formData.get('idCategorie');
    const categoryName = formData.get('nom');

    // Validate form data
    if (!categoryName || categoryName.trim() === '') {
        showEditCategoryError('Le nom de la catégorie est obligatoire');
        return;
    }

    const categoryData = {
        idCategorie: categoryId,
        nom: categoryName
    };

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
                return response.text().then(text => {
                    try {
                        // Extract just the message from the JSON
                        const parsedError = JSON.parse(text);
                        return parsedError.message || 'Error updating category';
                    } catch (e) {
                        // If not JSON or other parsing error
                        return text;
                    }
                });
            }
            return response.json();
        })
        .then(data => {
            // Check if this is an error message string
            if (typeof data === 'string') {
                throw data; // This will be caught by the catch block
            }

            // Close modal
            closeEditCategoryModal();

            // Reload categories
            loadCategoriesContent();
        })
        .catch(error => {
            console.error('Error updating category:', error);
            showEditCategoryError(error); // Now error is just the message string
        });
}

function deleteCategory(id) {
    fetch(`/superadmin/api/categories/${id}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    try {
                        // Extract just the message from the JSON
                        const parsedError = JSON.parse(text);
                        return parsedError.message || 'Error deleting category';
                    } catch (e) {
                        // If not JSON or other parsing error
                        return text;
                    }
                });
            }
            return response.json();
        })
        .then(data => {
            // Check if this is an error message string
            if (typeof data === 'string') {
                throw data; // This will be caught by the catch block
            }

            // Close modal
            closeDeleteCategoryModal();

            // Reload categories
            loadCategoriesContent();
        })
        .catch(error => {
            console.error('Error deleting category:', error);

            // Show error message in the delete modal
            const modalContent = document.querySelector('#delete-category-modal .cat-modal-content');

            // Remove existing error message if any
            const existingError = modalContent.querySelector('.cat-error-message');
            if (existingError) {
                existingError.remove();
            }

            // Add new error message
            const errorMessage = document.createElement('p');
            errorMessage.className = 'cat-error-message';
            errorMessage.textContent = error; // Now error is just the message string
            errorMessage.style.color = '#e74c3c';
            errorMessage.style.fontWeight = 'bold';
            errorMessage.style.marginTop = '10px';

            // Insert before the action buttons
            const actionButtons = modalContent.querySelector('.cat-form-actions');
            modalContent.insertBefore(errorMessage, actionButtons);
        });
}

// Load Categories Content
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

                // IMPORTANT: Initialize form handlers after content is loaded
                initializeCategoryForms();
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

// Initialize Form Handlers
function initializeCategoryForms() {
    console.log("Initializing category form handlers");

    // Setup add category form submission
    const addForm = document.getElementById('addCategoryForm');
    if (addForm) {
        console.log("Add category form found, setting up event listener");
        addForm.addEventListener('submit', function(e) {
            e.preventDefault();
            addCategory(this);
        });
    } else {
        console.warn("Add category form not found");
    }

    // Setup edit category form submission
    const editForm = document.getElementById('editCategoryForm');
    if (editForm) {
        editForm.addEventListener('submit', function(e) {
            e.preventDefault();
            updateCategory(this);
        });
    }

    // Setup delete buttons
    const deleteButtons = document.querySelectorAll('.cat-delete-btn');
    deleteButtons.forEach(button => {
        button.addEventListener('click', function() {
            const id = this.getAttribute('data-id');
            const name = this.getAttribute('data-name');
            confirmDeleteCategory(id, name);
        });
    });

    // Setup edit buttons
    const editButtons = document.querySelectorAll('.cat-edit-btn');
    editButtons.forEach(button => {
        button.addEventListener('click', function() {
            const id = this.getAttribute('data-id');
            const name = this.getAttribute('data-name');
            editCategory(id, name);
        });
    });
}

// Initialize when the page loads
document.addEventListener('DOMContentLoaded', function() {
    // Check if we're on the categories tab via URL hash
    if (window.location.hash === '#categories') {
        loadCategoriesContent();
    }
});