package org.rw.mortgagecalculator;

import org.rw.mortgagecalculator.services.MortgagePaymentService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MortgageCalculatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MortgageCalculatorApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider mortgagePaymentTool(MortgagePaymentService mortgagePaymentService) {
        return MethodToolCallbackProvider.builder().toolObjects(mortgagePaymentService).build();
    }
}
