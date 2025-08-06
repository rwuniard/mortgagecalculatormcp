package org.rw.mortgagecalculator.model;

/**
 * Represents a single month's mortgage payment breakdown
 */
public class PaymentBreakdown {
    private final int paymentNumber;
    private final double principalPayment;
    private final double interestPayment;
    private final double remainingBalance;
    
    public PaymentBreakdown(int paymentNumber, double principalPayment, double interestPayment, double remainingBalance) {
        this.paymentNumber = paymentNumber;
        this.principalPayment = principalPayment;
        this.interestPayment = interestPayment;
        this.remainingBalance = remainingBalance;
    }
    
    public int getPaymentNumber() {
        return paymentNumber;
    }
    
    public double getPrincipalPayment() {
        return principalPayment;
    }
    
    public double getInterestPayment() {
        return interestPayment;
    }
    
    public double getTotalPayment() {
        return principalPayment + interestPayment;
    }
    
    public double getRemainingBalance() {
        return remainingBalance;
    }
    
    @Override
    public String toString() {
        return String.format("Payment %d: Principal=%.2f, Interest=%.2f, Total=%.2f, Balance=%.2f", 
                paymentNumber, principalPayment, interestPayment, getTotalPayment(), remainingBalance);
    }
}