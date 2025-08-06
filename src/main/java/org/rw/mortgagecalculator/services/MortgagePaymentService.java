package org.rw.mortgagecalculator.services;

import org.rw.mortgagecalculator.model.PaymentBreakdown;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MortgagePaymentService {

    @Tool(name = "calculateMonthlyPayment", description = "Calculates the monthly mortgage payment based on principal, annual interest rate (For 5% you provide 5 and not 0.05), and loan term in years.")
    /*
         M = P [ r(1 + r)^n ] / [ (1 + r)^n - 1]
    Where:
        M is the total monthly mortgage payment.
        P is the principal loan amount (the amount you're borrowing from the lender).
        r is the monthly interest rate: Divide the annual interest rate by 12 (the number of months in a year).
        n is the number of payments over the loan's lifetime: Multiply the number of years in your loan term by 12.
     */
    public double calculateMonthlyPayment(double principal, double annualInterestRate, int loanTermYears) {
        double monthlyInterestRate = annualInterestRate / 100 / 12;
        int numberOfPayments = loanTermYears * 12;
        if (monthlyInterestRate == 0) {
            return principal / numberOfPayments;
        }
        double powerTerm = Math.pow(1 + monthlyInterestRate, numberOfPayments);
        double numerator = principal * monthlyInterestRate * powerTerm;
        double denominator = powerTerm - 1;
        return numerator / denominator;
    }

    @Tool(name = "getPaymentSchedule", description = "Returns a complete payment schedule showing monthly breakdown of principal and interest payments for the entire loan term. For the annual interest rate, you provide the percentage (e.g., for 5% use 5, not 0.05).")
    public List<PaymentBreakdown> getPaymentSchedule(double principal, double annualInterestRate, int loanTermYears) {
        List<PaymentBreakdown> paymentSchedule = new ArrayList<>();
        
        double monthlyInterestRate = annualInterestRate / 100 / 12;
        int numberOfPayments = loanTermYears * 12;
        double monthlyPayment = calculateMonthlyPayment(principal, annualInterestRate, loanTermYears);
        
        double remainingBalance = principal;
        
        for (int paymentNumber = 1; paymentNumber <= numberOfPayments; paymentNumber++) {
            // Calculate interest payment for this month
            double interestPayment = remainingBalance * monthlyInterestRate;
            
            // Calculate principal payment (total payment - interest)
            double principalPayment = monthlyPayment - interestPayment;
            
            // Handle final payment adjustment (remaining balance might be less than calculated principal)
            if (paymentNumber == numberOfPayments || principalPayment > remainingBalance) {
                principalPayment = remainingBalance;
            }
            
            // Update remaining balance
            remainingBalance -= principalPayment;
            
            // Create payment breakdown entry
            PaymentBreakdown breakdown = new PaymentBreakdown(
                paymentNumber, 
                principalPayment, 
                interestPayment, 
                Math.max(0, remainingBalance) // Ensure no negative balance due to rounding
            );
            
            paymentSchedule.add(breakdown);
            
            // Break if loan is fully paid
            if (remainingBalance <= 0) {
                break;
            }
        }
        
        return paymentSchedule;
    }
}
