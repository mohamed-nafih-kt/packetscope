package com.mdnafih.PacketScope.repository;

import com.mdnafih.PacketScope.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import com.mdnafih.PacketScope.model.Transaction;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findByUser(User user);
}
