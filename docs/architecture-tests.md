# Architecture Tests and CI Pipeline Integration

## Overview

This document explains the architecture tests used in the project and how they are integrated into the CI/CD pipeline to ensure code quality and architectural compliance.

## Architecture Tests

The project uses ArchUnit tests to enforce architectural rules and conventions. These tests are located in the `sport/src/test/java/com/srr/architecture` package and include:

### 1. Entity-Repository Pattern Tests
- Validates that entity classes are properly annotated with `@Entity` and `@Table`
- Ensures entities have proper ID fields
- Validates that repositories extend `JpaRepository` and `JpaSpecificationExecutor`
- Ensures DTOs follow proper naming conventions

### 2. Service Layer Tests
- Ensures service implementations are annotated with `@Service`
- Validates that methods modifying data are annotated with `@Transactional`
- Checks proper dependency injection patterns

### 3. Controller Layer Tests
- Ensures controller methods are documented with `@ApiOperation`
- Validates that public endpoints are secured with `@PreAuthorize`
- Enforces REST conventions for controllers

### 4. General Architecture Tests
- Enforces layered architecture (controllers → services → repositories)
- Prevents services from depending on web layer
- Ensures repositories are only accessed by services
- Enforces package structure conventions

### 5. Pagination Convention Tests
- Ensures consistent pagination patterns in service and controller methods
- Validates return types of methods that accept `Pageable` parameters

## CI Pipeline Integration

The architecture tests are integrated into the CI/CD pipeline using GitHub Actions. The workflow is defined in `.github/workflows/maven-build.yml` and includes:

1. **Build Job**:
   - Compiles the code
   - Packages the application (skipping tests at this stage)

2. **Test Job**:
   - Runs all unit tests across the project
   - Specifically runs architecture tests to validate compliance
   - Captures and uploads test results as artifacts
   - Must pass before deployment can occur

3. **Deploy Job**:
   - Only runs on the master branch
   - Only executes if both build and test jobs pass
   - Deploys the application to the target environment

## Running Architecture Tests Locally

To run the architecture tests locally:

```bash
# Run all tests
./mvnw test

# Run only architecture tests
./mvnw test -pl sport -Dtest=com.srr.architecture.*Test
```

## Maintaining Architecture Tests

When making changes to the codebase:

1. Ensure new code follows established patterns
2. Add appropriate annotations for security (`@PreAuthorize`) and transactions (`@Transactional`)
3. Run architecture tests locally before pushing changes
4. If architecture rules need to be modified, update the tests accordingly

The architecture tests serve as living documentation of the project's design principles and ensure consistent implementation across the codebase.
