package com.residencial.finance.infrastructure.persistence;

import com.residencial.finance.domain.model.BankTransaction;
import com.residencial.finance.domain.repository.BankTransactionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class BankTransactionRepositoryAdapter implements BankTransactionRepository {

    private final JpaBankTransactionRepository jpaBankTransactionRepository;

    public BankTransactionRepositoryAdapter(JpaBankTransactionRepository jpaBankTransactionRepository) {
        this.jpaBankTransactionRepository = jpaBankTransactionRepository;
    }

    @Override
    public BankTransaction save(BankTransaction transaction) {
        return jpaBankTransactionRepository.save(transaction);
    }

    @Override
    public Optional<BankTransaction> findById(UUID id) {
        return jpaBankTransactionRepository.findById(id);
    }

    @Override
    public boolean existsByAuthorizationNumber(String authorizationNumber) {
        return jpaBankTransactionRepository.existsByAuthorizationNumber(authorizationNumber);
    }
}
