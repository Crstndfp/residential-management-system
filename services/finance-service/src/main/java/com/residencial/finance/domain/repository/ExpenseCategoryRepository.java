package com.residencial.finance.domain.repository;

import com.residencial.finance.domain.model.ExpenseCategory;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseCategoryRepository {
    ExpenseCategory save(ExpenseCategory category);
    Optional<ExpenseCategory> findById(UUID id);
    Optional<ExpenseCategory> findByCode(String code);
}
