package org.example.charityproject1.service;

import org.example.charityproject1.model.CategorieAction;
import org.example.charityproject1.repository.CategorieActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategorieActionService {

    @Autowired
    private CategorieActionRepository categorieActionRepository;

    public List<CategorieAction> getAllCategories() {
        return categorieActionRepository.findAll();
    }

    public CategorieAction getCategoryById(String id) {
        return categorieActionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie non trouvée"));
    }

    /**
     * Create a new category
     * @param categorieAction The category to create
     * @return The created category
     * @throws RuntimeException If a category with the same name already exists
     */
    public CategorieAction createCategory(CategorieAction categorieAction) {
        // Check if category with same name already exists
        CategorieAction existingCategory = categorieActionRepository.findByNom(categorieAction.getNom());
        if (existingCategory != null) {
            throw new RuntimeException("Une catégorie avec ce nom existe déjà");
        }

        return categorieActionRepository.save(categorieAction);
    }

    /**
     * Update an existing category
     * @param id The ID of the category to update
     * @param updatedCategory The updated category data
     * @return The updated category
     * @throws RuntimeException If category not found or if another category with the same name exists
     */
    public CategorieAction updateCategory(String id, CategorieAction updatedCategory) {
        CategorieAction existingCategory = getCategoryById(id);

        // Check if name is being changed and if new name already exists
        if (!existingCategory.getNom().equals(updatedCategory.getNom())) {
            CategorieAction categoryWithSameName = categorieActionRepository.findByNom(updatedCategory.getNom());
            if (categoryWithSameName != null) {
                throw new RuntimeException("Une catégorie avec ce nom existe déjà");
            }
        }

        // Update fields
        existingCategory.setNom(updatedCategory.getNom());

        return categorieActionRepository.save(existingCategory);
    }

    /**
     * Delete a category by ID
     * @param id The ID of the category to delete
     * @throws RuntimeException If category not found
     */
    public void deleteCategory(String id) {
        // Check if category exists
        if (!categorieActionRepository.existsById(id)) {
            throw new RuntimeException("Catégorie non trouvée");
        }

        categorieActionRepository.deleteById(id);
    }
}