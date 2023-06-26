package ru.practicum.ewmservice.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewmservice.event.EventRepository;
import ru.practicum.ewmservice.exception.ConflictException;
import ru.practicum.ewmservice.exception.NotFoundException;

import java.util.List;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Transactional
    @Override
    public Category createCategory(CategoryDto categoryDto) {
        if (Boolean.TRUE.equals(categoryRepository.existsByName(categoryDto.getName()))) {
            throw new ConflictException("Category with such name has already existed in DB");
        }
        Category category = Category.builder()
                .name(categoryDto.getName())
                .build();
        Category savedCategory = categoryRepository.save(category);
        log.info("Category was added: {}", savedCategory);
        return savedCategory;
    }

    @Transactional
    @Override
    public void deleteCategory(Long catId) {
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Category with such id wasn't found");
        }
        if (Boolean.TRUE.equals(eventRepository.existsByCategoryId(catId))) {
            throw new ConflictException("Category cannot be removed. There are some events in this category.");
        }
        categoryRepository.deleteById(catId);
        log.info("Category was deleted: {}", catId);
    }

    @Transactional
    @Override
    public Category updateCategory(Long catId, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with such id wasn't found"));
        if (Boolean.TRUE.equals(categoryRepository.existsByName(categoryDto.getName()))
                && !category.getName().equals(categoryDto.getName())) {
            throw new ConflictException("Category with such name has already existed in DB");
        }
        category.setName(categoryDto.getName());
        categoryRepository.save(category);
        log.info("Category was updated in DB by initiator. New category is: {}", category);
        return category;
    }

    @Override
    public List<Category> getCategories(Pageable pageRequest) {
        List<Category> categories = categoryRepository.findAll(pageRequest).getContent();
        log.info("Categories were found in DB: {}", categories);
        return categories;
    }

    @Override
    public Category getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with such id wasn't found"));
        log.info("Category was found in DB: {}", category);
        return category;
    }
}
