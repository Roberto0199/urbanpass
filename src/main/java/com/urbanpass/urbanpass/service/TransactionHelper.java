package com.urbanpass.urbanpass.service;

import com.urbanpass.urbanpass.entity.Card;
import com.urbanpass.urbanpass.entity.Station;
import com.urbanpass.urbanpass.entity.Transaction;
import com.urbanpass.urbanpass.enums.TransactionStatus;
import com.urbanpass.urbanpass.enums.TransactionType;
import com.urbanpass.urbanpass.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionHelper {

    private final TransactionRepository transactionRepository;

    // REQUIRES_NEW abre una transacción independiente
    // Aunque validateCard haga rollback, esta se guarda igual
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailedTransaction(Card card, Station station,
                                      BigDecimal amount, String description) {

        log.info("Guardando transacción FAILED: {}", description);

        Transaction failedTx = Transaction.builder()
                .card(card)
                .station(station)
                .amount(amount)
                .type(TransactionType.VALIDATION)
                .status(TransactionStatus.FAILED)
                .balanceAfter(card.getBalance())
                .description(description)
                .build();

        transactionRepository.save(failedTx);
        log.info("Transacción FAILED guardada exitosamente.");
    }
}