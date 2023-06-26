package ru.practicum.ewmservice.category.controllers;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewmservice.category.Category;
import ru.practicum.ewmservice.category.CategoryService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/categories")
@AllArgsConstructor
public class PublicCategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public List<Category> getCategories(@PositiveOrZero @RequestParam(value = "from", defaultValue = "0") int from,
                                        @Positive @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageRequest = PageRequest.of(from / size, size);

        return categoryService.getCategories(pageRequest);
    }

    @GetMapping("/{catId}")
    public Category getCategoryById(@PathVariable Long catId) {
        return categoryService.getCategoryById(catId);
    }
}
