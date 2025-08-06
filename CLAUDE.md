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
├── model/
│   └── PaymentBreakdown.java          # Payment breakdown data model
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

**Available Tools:**
1. **calculateMonthlyPayment** - Calculates monthly mortgage payment
   - Parameters: principal (double), annualInterestRate (double), loanTermYears (int)
   - Returns: double (monthly payment amount)

2. **getPaymentSchedule** - Returns complete payment breakdown for entire loan term
   - Parameters: principal (double), annualInterestRate (double), loanTermYears (int)
   - Returns: List<PaymentBreakdown> (monthly breakdown of principal, interest, and remaining balance)

### Spring AI Version
- Spring AI: 1.0.0
- Spring Boot: 3.5.4
- Java: 24

## Testing Strategy

The codebase uses JUnit 5 for testing with:
- **Basic Calculations**: Standard mortgage payment calculations (5% interest, 30-year loan)
- **Edge Cases**: Zero interest rate scenarios, short-term loans
- **Payment Schedule Testing**: Complete amortization schedule validation
  - Verifies correct number of payments
  - Validates progressive decrease in interest payments
  - Confirms progressive increase in principal payments
  - Ensures final balance reaches zero
  - Tests total principal payments equal loan amount
- **Delta-based Assertions**: Floating-point comparisons with appropriate tolerance (0.0001-1.0)
- **JaCoCo Integration**: Code coverage reporting

## CI/CD and Quality Gates

### GitHub Actions Workflows
The project includes automated CI/CD workflows:

1. **PR Code Review** (`.github/workflows/pr-review.yml`):
   - Automatically reviews pull requests using Claude
   - Focuses on security, performance, maintainability, and testing
   - Runs on PR open, synchronize, and reopen events

2. **Continuous Integration** (`.github/workflows/ci.yml`):
   - Runs on pushes to main/develop branches and all PRs
   - Executes full test suite with coverage reporting
   - Uploads coverage reports to Codecov
   - Compiles and packages the application
   - Blocks merges if tests fail

### Pre-commit Hooks
Local quality gates to prevent broken commits:

```bash
# Set up git hooks (run once after cloning)
./setup-hooks.sh
```

The pre-commit hook (`.githooks/pre-commit`):
- Automatically runs `./mvnw test` before each commit
- Prevents commits if any tests fail
- Ensures code quality is maintained locally

### Required Setup
1. Add `ANTHROPIC_API_KEY` to GitHub repository secrets for Claude code review
2. (Optional) Add `CODECOV_TOKEN` for coverage reporting
3. Run `./setup-hooks.sh` to enable pre-commit testing