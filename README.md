# Java Banking API Playground

This project is a small banking API built to demonstrate Java and Spring Boot concepts to a developer learning Java.

> **Demonstration only:** this project is a learning exercise. It does not represent a complete banking platform or secure production architecture.

The API can:

- Register a customer and generate a 16-digit customer number.
- Retrieve a customer by customer number.
- List all accounts belonging to a customer.
- Open and close accounts.
- Deposit and withdraw money.
- Transfer money between accounts.

Customer and account numbers contain 16 digits and include a Luhn check digit. The application uses these numbers directly as identifiers and does not expose separate internal IDs.

## Technology

- Java 17
- Spring Boot 3.5
- Spring Web
- Jakarta Bean Validation
- Maven multi-module projects
- In-memory repositories
- JUnit 5 and Mockito unit tests

## Project structure

```text
JavaBankingApiPlayground/
|-- api/          Spring Boot entry point, controllers and HTTP exception handling
|-- business/     Customer and account business rules and unit tests
|-- domain/       Models, DTOs, enums and service/repository interfaces
|-- repository/   In-memory repository implementations
|-- utils/        Shared number generation and validation utilities
|-- tests/http/   Manual HTTP request scenarios
`-- pom.xml       Parent Maven configuration
```

The modules demonstrate separation of responsibilities similar to a multi-project C# solution:

| Module | Responsibility |
|---|---|
| `api` | Accepts HTTP requests, calls services and returns response DTOs. |
| `business` | Implements customer and account business rules. |
| `domain` | Contains models, DTOs, enums and service/repository interfaces. |
| `repository` | Stores and retrieves customers and accounts in memory. |
| `utils` | Contains reusable validation and number-generation logic. |

Dependencies point toward the domain contracts. Controllers depend on service interfaces, while services depend on repository interfaces. Spring injects concrete implementations through constructors.

## Techniques demonstrated

### Spring controllers

`@RestController`, `@RequestMapping`, `@PostMapping` and `@GetMapping` expose Java methods as HTTP endpoints. `@RequestBody` tells Spring to deserialize request JSON into a DTO.

### Dependency injection

Controllers and services use constructor injection. Their dependencies are declared as interfaces so that the API and business layers do not depend directly on concrete implementations.

### Request and response DTOs

Request classes inherit from `RequestBase`, and response wrapper classes inherit from `ResponseBase`. These empty base types demonstrate a shared request/response hierarchy that could later hold common metadata.

Controllers unwrap request DTOs before calling the business layer. Static `fromDomain(...)` factory methods build response wrappers and map internal domain models into public DTOs. This prevents the API from directly exposing the structure of domain objects.

`Account` and `Transaction` are standard Java classes with getters and setters. `AccountDto` and `CustomerDto` use Java records because they are small data carriers and do not need to inherit from the request or response base classes.

### Layer boundaries

- Controllers handle HTTP input, output and status mapping.
- Services apply business rules.
- Repository interfaces describe storage operations.
- Repository implementations provide the current in-memory storage.
- DTO factories perform domain-to-API mapping.

### Validation

Jakarta validation annotations such as `@NotNull`, `@NotBlank`, `@Email`, `@Pattern`, `@DecimalMin` and `@Digits` reject malformed HTTP requests.

The business layer also demonstrates operation-specific rules:

- A customer must exist before an account can be opened.
- An account must be open before money can be moved.
- Withdrawals and transfers require sufficient funds.
- Transfers require different source and target accounts.
- An account balance must be zero before the account can be closed.

### Money

Balances and transaction amounts use `BigDecimal`. This avoids the representation errors that binary floating-point types such as `double` can introduce when working with decimal money values.

### Number generation

`AccountUtils` generates 15 random digits and calculates a final Luhn check digit. The Luhn algorithm doubles alternating digits, reduces results greater than nine, and selects a check digit that makes the total divisible by ten. `ValidationUtils` repeats that calculation when validating a supplied number.

The checksum can detect many common typing mistakes, including an incorrect single digit. It does not provide security, establish uniqueness or prove that an account or customer exists; repository lookup is still required. The demonstration currently uses the same 16-digit Luhn-valid format for account and customer numbers.

### Response factories

Response classes expose static factories such as:

```java
AccountCreateResponse.fromDomain(account);
CustomerResponse.fromDomain(customer);
CustomerAccountResponse.fromDomain(customerNumber, accounts);
```

This keeps repetitive mapping code out of controllers and provides one place to define the public response shape.

### Error handling

Controllers translate expected Java exceptions into HTTP responses:

| Exception | HTTP status |
|---|---|
| `IllegalArgumentException` | `400 Bad Request` |
| `NoSuchElementException` | `404 Not Found` |
| `IllegalStateException` | `409 Conflict` |

### Thread coordination

Account service operations and in-memory repository methods use `synchronized`. This prevents overlapping operations inside this single application instance from modifying shared data at the same time. It is a teaching mechanism for this in-memory example, rather than a replacement for database transactions or distributed concurrency controls.

### Server timestamps

The server assigns `transactionDate` using `Instant.now()` when it processes a deposit, withdrawal or transfer. Clients do not supply this field.

## Business-layer unit tests

The automated tests live under `business/src/test/java` and cover `AccountServiceImpl` and `CustomerServiceImpl`. They are unit tests: each service is constructed directly, and Mockito supplies mock implementations of `AccountRepository` and `CustomerRepository`. Spring is not started and the in-memory repository implementations are not used.

JUnit 5 provides the test lifecycle and assertions. Mockito controls repository responses and verifies repository calls. This keeps the tests focused on business behavior, including:

- Customer registration, lookup and account listing.
- Account creation and closure.
- Deposits, withdrawals and transfers.
- Invalid identifiers, missing records and closed accounts.
- Insufficient funds and other rejected operations.
- Generated identifier validity, balance changes and transaction timestamps.
- Whether successful operations are persisted and rejected operations avoid repository writes.

The test classes use fixed Luhn-valid customer and account numbers. Generated numbers are checked for validity instead of being compared with an exact value because number generation is random.

## Running the application

Requirements:

- JDK 17 or later
- Maven 3.9 or the Maven installation bundled with IntelliJ IDEA

From the repository root, build all modules:

```shell
mvn clean package
```

Run the business-layer unit tests and build the domain and utility modules they depend on:

```shell
mvn -pl business -am test
```

Run the Spring Boot API:

```shell
mvn -pl api -am spring-boot:run
```

The API starts at:

```text
http://localhost:8080
```

You can also run `com.example.banking.api.Main` directly from IntelliJ IDEA.

## API endpoints

| Method | Route | Purpose |
|---|---|---|
| `POST` | `/api/customer/register` | Register a customer. |
| `GET` | `/api/customer/get?customerNumber=...` | Get a customer. |
| `GET` | `/api/customer/accounts?customerNumber=...` | List a customer's accounts. |
| `POST` | `/api/account/create` | Open an account for a registered customer. |
| `POST` | `/api/account/close` | Close an account with a zero balance. |
| `POST` | `/api/account/deposit` | Deposit money into an account. |
| `POST` | `/api/account/withdraw` | Withdraw money from an account. |
| `POST` | `/api/account/transfer` | Transfer money between accounts. |

## Example requests

Register a customer:

```http
POST /api/customer/register
Content-Type: application/json

{
  "customer": {
    "firstName": "Taylor",
    "lastName": "Reed",
    "email": "taylor.reed@example.com"
  }
}
```

The registration response wraps a `CustomerDto`:

```json
{
  "customer": {
    "customerNumber": "1234567890123452",
    "firstName": "Taylor",
    "lastName": "Reed",
    "email": "taylor.reed@example.com"
  }
}
```

Open an account using the generated customer number:

```http
POST /api/account/create
Content-Type: application/json

{
  "customerNumber": "1234567890123452"
}
```

Account responses wrap an `AccountDto` in an `account` property:

```json
{
  "account": {
    "accountNumber": "9876543210987658",
    "customerNumber": "1234567890123452",
    "balance": 0.00,
    "closed": false
  }
}
```

Deposit money:

```http
POST /api/account/deposit
Content-Type: application/json

{
  "transaction": {
    "transactionType": "DEPOSIT",
    "amount": 100.00,
    "targetAccountNumber": "9876543210987658"
  }
}
```

Transfer money:

```http
POST /api/account/transfer
Content-Type: application/json

{
  "transaction": {
    "transactionType": "TRANSFER",
    "amount": 25.00,
    "sourceAccountNumber": "9876543210987658",
    "targetAccountNumber": "1111111111111117"
  }
}
```

The files in [`client`](.client/http) contain ordered request sequences for the IntelliJ IDEA HTTP client or compatible REST clients. Run each file from top to bottom because later requests use values returned by earlier requests.

## Demonstration scope and limitations

This code intentionally favors visibility and simplicity so that Java and Spring concepts are easy to follow. Its design choices should not be interpreted as complete engineering standards or controls for a banking system.

- Data is held in memory and is lost when the application stops.
- There is no authentication, authorization, user session management or role-based access control.
- There is no encryption configuration, secrets management or protection for sensitive customer data.
- There is no database transaction support.
- There is no durable ledger, double-entry accounting, reconciliation or immutable transaction history.
- There are no idempotency controls, so repeated requests are not protected from duplicate processing.
- Account and customer number collisions are not checked.
- Transactions are processed but are not stored as an account history.
- There is no audit trail, structured operational logging, monitoring or alerting.
- There are no fraud detection, rate limiting, anti-money-laundering or know-your-customer controls.
- There are no resilience controls such as retries, timeouts, circuit breakers, backups or disaster recovery.
- Validation demonstrates basic input and business rules; it is not a complete financial validation policy.
- Exception handling is simplified and does not define a complete, versioned error contract.
- `synchronized` only coordinates work inside one running application instance.
- Automated tests currently cover the business layer; the `.http` files provide manual API scenarios.
- The API has no versioning, generated OpenAPI documentation or compatibility policy.

These deliberate omissions keep the project focused on basic Java, Spring Boot, DTO mapping, validation, dependency injection and layered application design.
