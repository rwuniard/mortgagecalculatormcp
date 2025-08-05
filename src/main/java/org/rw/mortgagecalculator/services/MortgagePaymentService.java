package org.rw.mortgagecalculator.services;


import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class MortgagePaymentService {

    @Tool(name = "calculateMonthlyPayment", description = "Calculates the monthly mortgage payment based on principal, annual interest rate, and loan term in years.")
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
}
