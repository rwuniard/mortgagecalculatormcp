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
   - Type: ASYNC (asynchronous for multiple client support)
   - Capabilities: tools, resources, prompts, completion
   - SSE endpoint: `/sse`
   - Max connections: 10 concurrent clients
   - Connection timeout: 30 seconds
   - Tomcat max connections: 100

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
  - Verifies correct number of payments for loan term
  - Validates progressive decrease in interest payments over time
  - Confirms progressive increase in principal payments over time
  - Ensures final balance reaches zero
  - Tests that total principal payments equal original loan amount
- **Delta-based Assertions**: Floating-point comparisons with appropriate tolerance (0.0001-1.0)
- **JaCoCo Integration**: Code coverage reporting

## Docker and Kubernetes Deployment

### Building Docker Image
The project includes a multi-stage Dockerfile optimized for production deployment:

```bash
# Build Docker image
docker build -t mortgage-calculator-mcp:latest .

# Run container locally
docker run -p 8080:8080 -p 8081:8081 mortgage-calculator-mcp:latest
```

**Docker Features:**
- Multi-stage build with Eclipse Temurin Java 24
- Security-focused: non-root user (ID 1001), minimal attack surface
- Health checks and graceful shutdown support
- Optimized layer caching for faster builds

### Kubernetes Deployment

The application is designed for stateless deployment in Kubernetes with support for multiple concurrent MCP clients.

#### Quick Deployment
```bash
cd k8s
# Build and deploy everything
./deploy.sh full-deploy

# Check deployment status
./deploy.sh status

# Clean up
./deploy.sh delete
```

#### Kubernetes Components

**Namespace & RBAC:**
- Dedicated `mcp-server` namespace with resource quotas
- ServiceAccount with minimal RBAC permissions
- Secure pod security context (non-root, read-only filesystem where possible)

**Application Deployment:**
- 3-replica deployment with rolling update strategy
- Resource requests: 256Mi memory, 250m CPU
- Resource limits: 1Gi memory, 500m CPU
- Comprehensive health checks (startup, liveness, readiness)
- Graceful shutdown with 30-second termination period

**Scaling & Performance:**
- Horizontal Pod Autoscaler (HPA): 2-10 replicas based on CPU (70%) and memory (80%)
- Stateless architecture optimized for Kubernetes pod lifecycle
- ASYNC MCP server mode for concurrent client connections

**Networking:**
- ClusterIP service for internal access (ports 8080, 8081)
- Optional ingress with NGINX controller support
- WebSocket-compatible configuration for MCP protocol
- Separate management endpoints with restricted access

**Configuration:**
- ConfigMap-based configuration for Kubernetes profile
- Environment-specific settings via SPRING_PROFILES_ACTIVE=kubernetes
- JVM tuning for containerized environments

#### Local Kubernetes Testing
For local development with minikube or kind:

```bash
# Port-forward to access the service
kubectl port-forward -n mcp-server svc/mortgage-calculator-mcp-service 8080:8080

# Access health checks
kubectl port-forward -n mcp-server svc/mortgage-calculator-mcp-service 8081:8081
curl http://localhost:8081/actuator/health
```

#### Production Considerations
- **TLS Configuration**: Update ingress.yaml with proper domain and certificates
- **Resource Limits**: Adjust based on expected load and cluster capacity
- **Monitoring**: Prometheus metrics available at `/actuator/prometheus` (port 8081)
- **Log Management**: JSON logging enabled for Kubernetes log aggregation
- **Security**: RBAC configured with minimal required permissions