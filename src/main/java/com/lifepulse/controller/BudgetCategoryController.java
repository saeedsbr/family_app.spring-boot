package com.lifepulse.controller;

import com.lifepulse.dto.BudgetCategoryRequest;
import com.lifepulse.dto.BudgetCategoryResponse;
import com.lifepulse.service.BudgetCategoryService;
import com.lifepulse.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/budget-categories")
@RequiredArgsConstructor
public class BudgetCategoryController {

    private final BudgetCategoryService categoryService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ResponseEntity<List<BudgetCategoryResponse>> getAll(Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(categoryService.getCategories(userId));
    }

    @PostMapping
    public ResponseEntity<BudgetCategoryResponse> create(@Valid @RequestBody BudgetCategoryRequest request,
                                                          Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        return ResponseEntity.ok(categoryService.addCustomCategory(request, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                        Authentication authentication) {
        UUID userId = currentUserService.getCurrentUserId(authentication);
        categoryService.deleteCustomCategory(id, userId);
        return ResponseEntity.noContent().build();
    }
}
