package ru.practicum.ewmservice.category;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    Category createCategory(CategoryDto categoryDto);

    void deleteCategory(Long catId);

    Category updateCategory(Long catId, CategoryDto categoryDto);

    List<Category> getCategories(Pageable pageRequest);

    Category getCategoryById(Long catId);
}
