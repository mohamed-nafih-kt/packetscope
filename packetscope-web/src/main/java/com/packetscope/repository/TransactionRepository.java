package com.packetscope.repository;

import com.packetscope.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import com.packetscope.model.Transaction;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findByUser(User user);
}
