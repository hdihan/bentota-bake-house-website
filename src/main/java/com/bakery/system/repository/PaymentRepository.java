package com.bakery.system.repository;

import com.bakery.system.entity.Order;
import com.bakery.system.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder(Order order);
    Optional<Payment> findByPaymentId(String paymentId);
}