package com.residencial.finance.infrastructure.persistence;

import com.residencial.finance.domain.model.ExpenseCategory;
import com.residencial.finance.domain.repository.ExpenseCategoryRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ExpenseCategoryRepositoryAdapter implements ExpenseCategoryRepository {

    private final JpaExpenseCategoryRepository jpaExpenseCategoryRepository;

    public ExpenseCategoryRepositoryAdapter(JpaExpenseCategoryRepository jpaExpenseCategoryRepository) {
        this.jpaExpenseCategoryRepository = jpaExpenseCategoryRepository;
    }

    @Override
    public ExpenseCategory save(ExpenseCategory category) {
        return jpaExpenseCategoryRepository.save(category);
    }

    @Override
    public Optional<ExpenseCategory> findById(UUID id) {
        return jpaExpenseCategoryRepository.findById(id);
    }

    @Override
    public Optional<ExpenseCategory> findByCode(String code) {
        return jpaExpenseCategoryRepository.findByCode(code);
    }
}
