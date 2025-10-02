// File: src/main/java/org/syos/domain/entity/Bill.java
package org.syos.domain.entity;

import org.syos.domain.enums.PaymentMethod;
import org.syos.domain.enums.TransactionType;
import org.syos.domain.valueobject.Money;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a sales bill/invoice.
 * Uses Builder pattern for complex construction.
 */
public class Bill {
    private Long billId;
    private String serialNumber;
    private LocalDateTime dateTime;
    private TransactionType transactionType;
    private Long customerId; // References either Customer or OnlineCustomer
    private String customerName;
    private List<BillItem> items;
    private Money subtotal;
    private Money discount;
    private Money totalAmount;
    private PaymentMethod paymentMethod;
    private Money cashTendered;
    private Money changeAmount;

    // Private constructor - use Builder
    private Bill(Builder builder) {
        this.billId = builder.billId;
        this.serialNumber = builder.serialNumber;
        this.dateTime = builder.dateTime;
        this.transactionType = builder.transactionType;
        this.customerId = builder.customerId;
        this.customerName = builder.customerName;
        this.items = new ArrayList<>(builder.items);
        this.paymentMethod = builder.paymentMethod;
        this.cashTendered = builder.cashTendered;
        this.discount = builder.discount;
        calculateTotals();
    }

    private void calculateTotals() {
        this.subtotal = items.stream()
                .map(BillItem::getTotalPrice)
                .reduce(Money.zero(), Money::add);
        // discount is now set from builder for manual discounts
        if (this.discount == null) {
            this.discount = Money.zero();
        }
        this.totalAmount = subtotal.subtract(discount);

        if (cashTendered != null) {
            this.changeAmount = cashTendered.subtract(totalAmount);
        }
    }

    // Builder Pattern
    public static class Builder {
        private Long billId;
        private String serialNumber;
        private LocalDateTime dateTime;
        private TransactionType transactionType;
        private Long customerId;
        private String customerName;
        private List<BillItem> items = new ArrayList<>();
        private PaymentMethod paymentMethod;
        private Money cashTendered;
        private Money discount;

        public Builder serialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
            return this;
        }

        public Builder dateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
            return this;
        }

        public Builder transactionType(TransactionType transactionType) {
            this.transactionType = transactionType;
            return this;
        }

        public Builder customerId(Long customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder customerName(String customerName) {
            this.customerName = customerName;
            return this;
        }

        public Builder addItem(BillItem item) {
            this.items.add(item);
            return this;
        }

        public Builder items(List<BillItem> items) {
            this.items = new ArrayList<>(items);
            return this;
        }

        public Builder paymentMethod(PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public Builder cashTendered(Money cashTendered) {
            this.cashTendered = cashTendered;
            return this;
        }

        public Builder discount(Money discount) {
            this.discount = discount;
            return this;
        }

        public Bill build() {
            if (serialNumber == null) {
                throw new IllegalStateException("Serial number is required");
            }
            if (dateTime == null) {
                this.dateTime = LocalDateTime.now();
            }
            if (transactionType == null) {
                throw new IllegalStateException("Transaction type is required");
            }
            if (items.isEmpty()) {
                throw new IllegalStateException("Bill must have at least one item");
            }
            return new Bill(this);
        }
    }

    // Getters
    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public List<BillItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public Money getSubtotal() {
        return subtotal;
    }

    public Money getDiscount() {
        return discount;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public Money getCashTendered() {
        return cashTendered;
    }

    public Money getChangeAmount() {
        return changeAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Bill bill = (Bill) o;
        return Objects.equals(billId, bill.billId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(billId);
    }
}
