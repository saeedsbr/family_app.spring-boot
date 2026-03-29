package com.lifepulse.controller;

import com.lifepulse.dto.BudgetCategoryRequest;
import com.lifepulse.dto.BudgetCategoryResponse;
import com.lifepulse.security.UserDetailsImpl;
import com.lifepulse.service.BudgetCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/budget-categories")
@RequiredArgsConstructor
public class BudgetCategoryController {

    private final BudgetCategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<BudgetCategoryResponse>> getAll(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(categoryService.getCategories(user.getId()));
    }

    @PostMapping
    public ResponseEntity<BudgetCategoryResponse> create(@Valid @RequestBody BudgetCategoryRequest request,
                                                          @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(categoryService.addCustomCategory(request, user.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                        @AuthenticationPrincipal UserDetailsImpl user) {
        categoryService.deleteCustomCategory(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
