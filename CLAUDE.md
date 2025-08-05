# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot application that implements a Model Context Protocol (MCP) server for mortgage calculations. The server exposes a mortgage payment calculation tool that can be consumed by MCP clients.

**Key Components:**
- **MortgagePaymentService**: Core service implementing the mortgage payment calculation formula
- **MortgageCalculatorApplication**: Spring Boot main class with MCP tool configuration
- **Spring AI MCP Integration**: Uses `spring-ai-starter-mcp-server-webmvc` for MCP server capabilities

## Development Commands

### Building and Running
```bash
# Build the project
./mvnw clean compile

# Run tests
./mvnw test

# Run the application
./mvnw spring-boot:run

# Build with tests and coverage report
./mvnw clean test

# Package the application
./mvnw package
```

### Testing Commands
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=MortgagePaymentServiceTests

# Run single test method
./mvnw test -Dtest=MortgagePaymentServiceTests#testCalculateMonthlyPayment

# Generate test coverage report (JaCoCo)
./mvnw jacoco:report
# Coverage report will be in target/site/jacoco/index.html
```

## Architecture

### MCP Server Architecture
The application is structured as a Spring AI MCP server with the following architecture:

1. **MCP Server Configuration** (`application.yaml`):
   - Server name: `mortgage-calculator-mcp-server`
   - Type: SYNC (synchronous)
   - Capabilities: tools, resources, prompts, completion
   - SSE endpoint: `/sse`

2. **Tool Registration** (`MortgageCalculatorApplication`):
   - Uses `MethodToolCallbackProvider` to register service methods as MCP tools
   - The `mortgagePaymentTool` bean registers mortgage calculation tools

3. **Service Layer** (`MortgagePaymentService`):
   - `@Tool` annotation exposes methods as MCP tools
   - Implements standard mortgage payment formula: M = P [ r(1 + r)^n ] / [ (1 + r)^n - 1]
   - Handles edge case of zero interest rate

### Package Structure
```
org.rw.mortgagecalculator/
├── MortgageCalculatorApplication.java  # Main Spring Boot app + MCP config
└── services/
    └── MortgagePaymentService.java     # Core mortgage calculation logic
```

## Key Technical Details

### Mortgage Calculation Formula
The service implements the standard amortization formula with proper handling of:
- Zero interest rate scenarios (simple division)
- Monthly interest rate conversion (annual rate / 100 / 12)
- Payment count calculation (years * 12)

### MCP Tool Definition
Tools are defined using Spring AI annotations:
- `@Tool(name, description)` - Exposes method as MCP tool
- Tool name: `calculateMonthlyPayment`
- Parameters: principal (double), annualInterestRate (double), loanTermYears (int)

### Spring AI Version
- Spring AI: 1.0.0
- Spring Boot: 3.5.4
- Java: 24

## Testing Strategy

The codebase uses JUnit 5 for testing with:
- Standard calculation test case (5% interest, 30-year loan)
- Edge case testing (zero interest rate)
- Delta-based assertions for floating-point comparisons (0.0001 tolerance)
- JaCoCo integration for code coverage reporting