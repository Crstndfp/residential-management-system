package com.residencial.finance.domain.repository;

import com.residencial.finance.domain.model.BankTransaction;
import java.util.Optional;
import java.util.UUID;

public interface BankTransactionRepository {
    BankTransaction save(BankTransaction transaction);
    Optional<BankTransaction> findById(UUID id);
    boolean existsByAuthorizationNumber(String authorizationNumber);
}
