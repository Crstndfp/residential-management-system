package com.residencial.finance.infrastructure.persistence;

import com.residencial.finance.domain.model.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaBankTransactionRepository extends JpaRepository<BankTransaction, UUID> {
    boolean existsByAuthorizationNumber(String authorizationNumber);
    List<BankTransaction> findByHouseReference(String houseReference);
}
