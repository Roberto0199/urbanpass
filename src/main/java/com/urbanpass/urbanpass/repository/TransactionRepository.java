package com.urbanpass.urbanpass.repository;

import com.urbanpass.urbanpass.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Historial paginado ordenado del más reciente al más antiguo
    Page<Transaction> findByCardIdOrderByTimestampDesc(Long cardId, Pageable pageable);
}