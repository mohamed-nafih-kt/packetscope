package com.mdnafih.PacketScope.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.mdnafih.PacketScope.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
}
