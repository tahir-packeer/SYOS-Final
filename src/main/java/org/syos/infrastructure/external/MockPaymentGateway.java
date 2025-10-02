// File: src/main/java/org/syos/infrastructure/external/MockPaymentGateway.java
package org.syos.infrastructure.external;

import org.syos.application.service.PaymentGateway;
import org.syos.domain.enums.PaymentMethod;
import org.syos.domain.valueobject.Money;

/**
 * Mock payment gateway for CCCP1.
 * In CCCP2, this would integrate with real payment processors.
 */
public class MockPaymentGateway implements PaymentGateway {
    
    @Override
    public boolean processPayment(Money amount, PaymentMethod method, String details) {
        // Simulate payment processing
        System.out.println("Processing payment: " + amount.toDisplayString() + 
                         " via " + method.getDisplayName());
        
        // Mock success for all non-cash payments
        return method != PaymentMethod.CASH;
    }
    
    @Override
    public boolean verifyPayment(String transactionId) {
        // Mock verification - always returns true
        return true;
    }
}
