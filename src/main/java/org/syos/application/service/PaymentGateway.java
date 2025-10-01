// File: src/main/java/org/syos/application/service/PaymentGateway.java
package org.syos.application.service;

import org.syos.domain.valueobject.Money;
import org.syos.domain.enums.PaymentMethod;

/**
 * Service interface for payment processing.
 * Strategy pattern - can have different implementations.
 */
public interface PaymentGateway {
    boolean processPayment(Money amount, PaymentMethod method, String details);
    boolean verifyPayment(String transactionId);
}