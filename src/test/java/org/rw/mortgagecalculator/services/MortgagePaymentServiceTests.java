package org.rw.mortgagecalculator.services;

import org.junit.jupiter.api.Test;
import org.rw.mortgagecalculator.model.PaymentBreakdown;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MortgagePaymentServiceTests {

    private final MortgagePaymentService mortgagePaymentService = new MortgagePaymentService();

    @Test
    public void testCalculateMonthlyPayment() {
        double principal = 200000;
        double annualInterestRate = 5;
        int loanTermYears = 30;
        double expectedMonthlyPayment = 1073.64325;
        double actualMonthlyPayment = mortgagePaymentService.calculateMonthlyPayment(principal, annualInterestRate, loanTermYears);
        assertEquals(expectedMonthlyPayment, actualMonthlyPayment, 0.0001);
    }

    @Test
    public void testCalculateMonthlyPayment_ZeroInterest() {
        double principal = 200000;
        double annualInterestRate = 0;
        int loanTermYears = 30;
        double expectedMonthlyPayment = 200000.0 / (30 * 12);
        double actualMonthlyPayment = mortgagePaymentService.calculateMonthlyPayment(principal, annualInterestRate, loanTermYears);
        assertEquals(expectedMonthlyPayment, actualMonthlyPayment, 0.0001);
    }

    @Test
    public void testGetPaymentSchedule() {
        double principal = 200000;
        double annualInterestRate = 5;
        int loanTermYears = 30;
        
        List<PaymentBreakdown> schedule = mortgagePaymentService.getPaymentSchedule(principal, annualInterestRate, loanTermYears);
        
        // Verify schedule has correct number of payments
        assertEquals(360, schedule.size()); // 30 years * 12 months
        
        // Verify first payment
        PaymentBreakdown firstPayment = schedule.get(0);
        assertEquals(1, firstPayment.getPaymentNumber());
        assertEquals(833.33, firstPayment.getInterestPayment(), 0.01); // 200000 * (0.05/12)
        assertEquals(240.31, firstPayment.getPrincipalPayment(), 0.01); // 1073.64 - 833.33
        assertEquals(1073.64, firstPayment.getTotalPayment(), 0.01);
        assertEquals(199759.69, firstPayment.getRemainingBalance(), 0.01);
        
        // Verify last payment
        PaymentBreakdown lastPayment = schedule.get(schedule.size() - 1);
        assertEquals(360, lastPayment.getPaymentNumber());
        assertEquals(0.0, lastPayment.getRemainingBalance(), 0.01); // Should be fully paid off
        assertTrue(lastPayment.getInterestPayment() > 0); // Should have some interest
        assertTrue(lastPayment.getPrincipalPayment() > 0); // Should have principal payment
        
        // Verify total principal payments equals original loan amount
        double totalPrincipal = schedule.stream()
                .mapToDouble(PaymentBreakdown::getPrincipalPayment)
                .sum();
        assertEquals(principal, totalPrincipal, 1.0); // Allow $1 tolerance for rounding
        
        // Verify all payments have non-negative values
        for (PaymentBreakdown payment : schedule) {
            assertTrue(payment.getPrincipalPayment() >= 0);
            assertTrue(payment.getInterestPayment() >= 0);
            assertTrue(payment.getRemainingBalance() >= 0);
        }
    }

    @Test
    public void testGetPaymentSchedule_ShortTerm() {
        double principal = 100000;
        double annualInterestRate = 4;
        int loanTermYears = 5;
        
        List<PaymentBreakdown> schedule = mortgagePaymentService.getPaymentSchedule(principal, annualInterestRate, loanTermYears);
        
        // Verify schedule has correct number of payments
        assertEquals(60, schedule.size()); // 5 years * 12 months
        
        // Verify total principal payments
        double totalPrincipal = schedule.stream()
                .mapToDouble(PaymentBreakdown::getPrincipalPayment)
                .sum();
        assertEquals(principal, totalPrincipal, 0.1);
        
        // Verify final balance is zero
        PaymentBreakdown lastPayment = schedule.get(schedule.size() - 1);
        assertEquals(0.0, lastPayment.getRemainingBalance(), 0.01);
    }

    @Test
    public void testGetPaymentSchedule_ZeroInterest() {
        double principal = 120000;
        double annualInterestRate = 0;
        int loanTermYears = 10;
        
        List<PaymentBreakdown> schedule = mortgagePaymentService.getPaymentSchedule(principal, annualInterestRate, loanTermYears);
        
        // Verify schedule has correct number of payments
        assertEquals(120, schedule.size()); // 10 years * 12 months
        
        // With zero interest, all payments should be equal principal payments
        double expectedPrincipalPayment = principal / (loanTermYears * 12);
        
        for (int i = 0; i < schedule.size(); i++) {
            PaymentBreakdown payment = schedule.get(i);
            assertEquals(expectedPrincipalPayment, payment.getPrincipalPayment(), 0.01);
            assertEquals(0.0, payment.getInterestPayment(), 0.01);
            assertEquals(expectedPrincipalPayment, payment.getTotalPayment(), 0.01);
        }
        
        // Final payment should result in zero balance
        PaymentBreakdown lastPayment = schedule.get(schedule.size() - 1);
        assertEquals(0.0, lastPayment.getRemainingBalance(), 0.01);
    }

    @Test
    public void testGetPaymentSchedule_ProgressiveDecrease() {
        double principal = 200000;
        double annualInterestRate = 6;
        int loanTermYears = 15;
        
        List<PaymentBreakdown> schedule = mortgagePaymentService.getPaymentSchedule(principal, annualInterestRate, loanTermYears);
        
        // Interest payments should decrease over time
        // Principal payments should increase over time
        PaymentBreakdown firstPayment = schedule.get(0);
        PaymentBreakdown midPayment = schedule.get(schedule.size() / 2);
        PaymentBreakdown lastPayment = schedule.get(schedule.size() - 1);
        
        // Interest should decrease over time
        assertTrue(firstPayment.getInterestPayment() > midPayment.getInterestPayment());
        assertTrue(midPayment.getInterestPayment() > lastPayment.getInterestPayment());
        
        // Principal should increase over time  
        assertTrue(firstPayment.getPrincipalPayment() < midPayment.getPrincipalPayment());
        assertTrue(midPayment.getPrincipalPayment() < lastPayment.getPrincipalPayment());
        
        // Remaining balance should decrease consistently
        assertTrue(firstPayment.getRemainingBalance() > midPayment.getRemainingBalance());
        assertTrue(midPayment.getRemainingBalance() > lastPayment.getRemainingBalance());
        assertEquals(0.0, lastPayment.getRemainingBalance(), 0.01);
    }
}

