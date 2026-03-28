package com.vms.service;

import com.vms.dto.BudgetCategoryRequest;
import com.vms.dto.BudgetCategoryResponse;
import com.vms.entity.BudgetCategory;
import com.vms.entity.User;
import com.vms.repository.BudgetCategoryRepository;
import com.vms.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetCategoryService {

    private final BudgetCategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // Seed system categories on startup
    @PostConstruct
    public void seedSystemCategories() {
        if (categoryRepository.count() > 0) return;
        Map<String, String> defaults = Map.ofEntries(
                Map.entry("Food & Dining", "#f97316"),
                Map.entry("Transport", "#3b82f6"),
                Map.entry("Bills & Utilities", "#8b5cf6"),
                Map.entry("Shopping", "#ec4899"),
                Map.entry("Entertainment", "#f59e0b"),
                Map.entry("Health & Medical", "#10b981"),
                Map.entry("Education", "#6366f1"),
                Map.entry("Salary", "#22c55e"),
                Map.entry("Business", "#0ea5e9"),
                Map.entry("Other", "#94a3b8")
        );
        defaults.forEach((name, color) -> {
            BudgetCategory cat = BudgetCategory.builder()
                    .name(name).color(color).isSystem(true).owner(null).build();
            categoryRepository.save(cat);
        });
    }

    public List<BudgetCategoryResponse> getCategories(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return categoryRepository.findSystemAndUserCategories(user)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public BudgetCategoryResponse addCustomCategory(BudgetCategoryRequest req, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        BudgetCategory cat = BudgetCategory.builder()
                .name(req.getName())
                .color(req.getColor() != null ? req.getColor() : "#94a3b8")
                .isSystem(false)
                .owner(user)
                .build();
        return mapToResponse(categoryRepository.save(cat));
    }

    @Transactional
    public void deleteCustomCategory(UUID categoryId, UUID userId) {
        BudgetCategory cat = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        if (cat.isSystem()) {
            throw new RuntimeException("Cannot delete system categories");
        }
        if (!cat.getOwner().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own categories");
        }
        categoryRepository.delete(cat);
    }

    private BudgetCategoryResponse mapToResponse(BudgetCategory cat) {
        return BudgetCategoryResponse.builder()
                .id(cat.getId())
                .name(cat.getName())
                .color(cat.getColor())
                .isSystem(cat.isSystem())
                .build();
    }
}
