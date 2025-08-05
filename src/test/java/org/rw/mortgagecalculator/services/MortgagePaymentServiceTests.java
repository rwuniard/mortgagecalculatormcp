package org.rw.mortgagecalculator.services;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
}

