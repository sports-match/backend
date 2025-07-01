# Backend Management System

## Project Introduction

A front-end and back-end separated management system based on Spring Boot 2.7.18, Spring Boot JPA, JWT, Spring Security, Redis, and Vue.

## Main Features

- Uses the latest tech stack with rich community resources.
- High development efficiency with code generator for one-click front-end and back-end code generation.
- Supports data dictionaries for easy status management.
- API rate limiting to prevent service overload from malicious requests.
- Supports function-level and data-level permissions, with customizable operations.
- Custom permission and anonymous API annotations for quick API interception and release.
- Encapsulates common front-end components: table data requests, data dictionaries, etc.
- Unified exception handling for both front and back ends, avoiding repetitive checks.
- Online user management and server performance monitoring, with single-user login restriction.
- Operations management for easy deployment and management of remote server applications.

## System Functions

- User Management: Configure users; new users default to password `123456`.
- Role Management: Assign permissions and menus, set data permissions by department.
- Menu Management: Dynamic routing, back-end configurable, supports multi-level menus.
- Position Management: Configure positions for each department.
- Dictionary Management: Maintain common fixed data, such as status and gender.
- System Log: Record user operation logs and exception logs for easy error tracking.
- SQL Monitoring: Use Druid to monitor database access performance, default username `admin`, password `123456`.
- Scheduled Tasks: Integrate Quartz for scheduled tasks, with task logs and task execution status.
- Code Generation: High flexibility code generation for front-end and back-end, reducing repetitive work.
- Email Tool: Send HTML format emails with rich text.
- AWS Cloud Storage: Synchronize AWS cloud storage data to the system, no need to log in to AWS cloud to operate cloud data.
- Server Monitoring: Monitor server load status.
- Operations Management: One-click deployment of your application.

## Project Structure

The project uses a modular development approach, with the following structure:

- `eladmin-common`: System public module, including various utility classes and public configurations.
- `eladmin-system`: System core module and project entry module, also the final module to be packaged and deployed.
- `eladmin-logging`: System log module, other modules need to import this module to record logs.
- `eladmin-tools`: Third-party tool module, including email, AWS cloud storage, local storage
- `eladmin-generator`: System code generation module, supporting front-end and back-end CRUD code generation.

## Detailed Structure

```
- eladmin-common Public Module
    - annotation System custom annotations
    - aspect Custom annotation aspects
    - base Entity, DTO base classes, and MapStruct common mapper
    - config Project public configurations
        - Web configuration: cross-domain, static resource mapping, Swagger configuration, file upload temporary path configuration
        - Redis configuration, Redission configuration, asynchronous thread pool configuration
        - Permission interception configuration, AuthorityConfig, Druid delete advertisement configuration
    - exception Project unified exception handling
    - utils System utility classes, including:
        - BigDecimaUtils Amount calculation utility class
        - RequestHolder Request utility class
        - SecurityUtils Security utility class
        - StringUtils String utility class
        - SpringBeanHolder Spring Bean utility class
        - RedisUtils Redis utility class
        - EncryptUtils Encryption utility class
        - FileUtil File utility class
- eladmin-system System Core Module (Project Entry Module)
    - sysrunner Program startup data processing
    - modules System-related modules (login authorization, system monitoring, scheduled tasks, system modules, operations management)
- eladmin-logging System Log Module
- eladmin-tools Third-party Tool Module
    - email Email tool
    - AWS S3 cloud storage tool
    - local-storage Local storage tool
- eladmin-generator System Code Generation Module
```

## Running the Project

### Development Environment

1. **Prerequisites**:
   - Java 17
   - Maven 3.6+
   - MySQL 8.0+
   - Redis

2. **Configure Development Properties**:
   - Update database connection in `eladmin-system/src/main/resources/config/application-dev.yml`
   - Configure Redis connection
   - Set email properties if using the email verification feature

3. **Run the Application**:
   ```bash
   # Using Maven Wrapper
   ./mvnw -pl eladmin-system spring-boot:run
   
   # Compile and build the project
   ./mvnw clean package
   ```

4. **Access the API**:
   - The API will be available at: `http://localhost:8000`
   - Swagger API documentation: `http://localhost:8000/swagger-ui/index.html`


### Production Environment

1. **Build the Project**:
   ```bash
   # Using Maven Wrapper
   ./mvnw clean package -Pproduct -Dmaven.test.skip=true
   ```

2. **Configure Production Properties**:
   - Update database connection in `eladmin-system/src/main/resources/config/application-prod.yml`
   - Configure Redis connection with secure password
   - Set appropriate logging levels
   - Enable CORS protection as needed

3. **Deploy the Application**:
   ```bash
   java -jar eladmin-system/target/eladmin-system-2.7.jar --spring.profiles.active=prod &
   ```

4. **Server Requirements**:
   - Minimum: 1 Core CPU, 2GB RAM
   - Recommended: 2+ Core CPU, 4GB+ RAM