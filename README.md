# Online Banking Application

A comprehensive web-based Online Banking Application built with Java Spring Boot that enables customers to securely perform essential banking operations like transferring funds, checking balances, and maintaining transaction history using XML for structured storage.

## Features

### ✅ Core Features Implemented

1. **User Authentication & Security**
   - Secure login with username and password
   - JWT-based authentication
   - Password encryption using BCrypt
   - Role-based access control (Customer/Admin)

2. **Account Management**
   - View account details and balances
   - Manage multiple accounts (Savings, Current, Fixed Deposit, Recurring Deposit)
   - Create new accounts
   - Real-time balance updates

3. **Fund Transfer Module**
   - Transfer money between accounts (intra-bank)
   - External transfers with IFSC code support
   - Transaction validation (sufficient balance, account verification)
   - Real-time balance updates after transactions

4. **Transaction History (XML-Based)**
   - Store transaction history in XML format
   - Retrieve and display past transactions
   - Filter transactions by date range
   - View transactions by account

5. **Balance Inquiry**
   - Instant account balance check
   - Real-time updates after transactions

6. **Admin Panel**
   - Admin-only endpoints for account management
   - View all transactions
   - Deposit funds to accounts

7. **Web Interface**
   - Modern, responsive UI
   - Dashboard with quick actions
   - Account management interface
   - Fund transfer interface
   - Transaction history viewer

## Technology Stack

### Backend Framework
- **Java 17** - Modern Java with features like records, pattern matching, and improved performance
- **Spring Boot 3.2.0** - Rapid application development framework providing:
  - Auto-configuration for common scenarios
  - Embedded server (Tomcat)
  - Production-ready features (metrics, health checks)
  - Convention over configuration approach

### Security & Authentication
- **Spring Security 6.x** - Comprehensive security framework providing:
  - Authentication and authorization
  - CSRF protection
  - Session management
  - Security filters chain
- **JWT (JSON Web Tokens)** - Stateless authentication using:
  - `jjwt-api` (v0.12.3) - JWT API
  - `jjwt-impl` (v0.12.3) - JWT implementation
  - `jjwt-jackson` (v0.12.3) - JSON serialization support
- **BCrypt** - Password hashing algorithm (included in Spring Security)

### Data Persistence
- **Spring Data JPA** - Simplifies database operations with:
  - Repository pattern
  - Automatic query generation
  - Transaction management
- **Hibernate** - JPA implementation for ORM (Object-Relational Mapping)
- **H2 Database** - Lightweight, in-memory/file-based database:
  - Perfect for development and prototyping
  - Supports SQL standard
  - Can be easily replaced with PostgreSQL/MySQL for production

### Frontend Technologies
- **Thymeleaf** - Server-side Java template engine:
  - Natural templates (HTML-based)
  - Spring Security integration
  - Template fragments and layouts
- **HTML5** - Modern markup language
- **CSS3** - Modern styling with:
  - CSS Custom Properties (variables)
  - Flexbox and Grid layouts
  - Modern design system (shadcn/ui-inspired)
- **Vanilla JavaScript (ES6+)** - Client-side scripting:
  - Fetch API for HTTP requests
  - Async/await for asynchronous operations
  - DOM manipulation
  - LocalStorage for token management

### Data Processing
- **JAXB (Java Architecture for XML Binding)** - XML processing:
  - `jaxb-api` (v2.3.1) - JAXB API
  - `jaxb-runtime` (v4.0.3) - JAXB runtime implementation
  - Used for XML transaction storage and retrieval

### Utilities & Libraries
- **Apache POI** (v5.2.4) - Java library for Microsoft Office documents:
  - Excel file generation
  - Transaction export functionality
- **Bean Validation** - Input validation:
  - `spring-boot-starter-validation`
  - `@Valid`, `@NotNull`, `@Size` annotations
- **Lombok** - Reduces boilerplate code:
  - `@Getter`, `@Setter`, `@Builder` annotations
  - Compile-time code generation

### Build & Dependency Management
- **Maven 3.6+** - Build automation and dependency management:
  - Project structure standardization
  - Dependency resolution
  - Build lifecycle management
  - Plugin ecosystem

### Development Tools
- **Spring Boot DevTools** (implicit) - Development productivity:
  - Automatic restart
  - Live reload
  - Property defaults
- **H2 Console** - Web-based database management interface

### Testing (Available)
- **JUnit 5** - Unit testing framework
- **Spring Boot Test** - Integration testing support
- **Spring Security Test** - Security testing utilities
- **Mockito** - Mocking framework (included in test starter)

### Architecture Patterns
- **MVC (Model-View-Controller)** - Separation of concerns:
  - Controllers handle HTTP requests
  - Services contain business logic
  - Models represent data entities
- **Repository Pattern** - Data access abstraction
- **DTO (Data Transfer Object)** - Data transfer between layers
- **RESTful API** - Stateless API design

### Frontend-Backend Communication

#### HTTP Client
- **Fetch API (Native JavaScript)** - Modern browser API for HTTP requests:
  - No external dependencies required
  - Promise-based asynchronous requests
  - Support for async/await syntax
  - Built-in JSON parsing
  - Used throughout all JavaScript files for API calls

#### Communication Protocol
- **REST (Representational State Transfer)** - API architecture:
  - JSON for data exchange
  - HTTP methods (GET, POST, PUT, DELETE)
  - Stateless communication
  - Standard HTTP status codes

#### Authentication Mechanism
- **JWT (JSON Web Tokens)** - Token-based authentication:
  - Token stored in browser's `localStorage`
  - Sent in `Authorization: Bearer <token>` header
  - Stateless authentication (no server-side sessions)
  - Token expiration handling

#### Cross-Origin Configuration
- **CORS (Cross-Origin Resource Sharing)** - Configured in `SecurityConfig.java`:
  - Allows all origins (`*`) for development
  - Supports GET, POST, PUT, DELETE, OPTIONS methods
  - Allows all headers
  - Configured for `/api/**` endpoints
  - Max age: 3600 seconds

#### Data Format
- **JSON (JavaScript Object Notation)** - Data exchange format:
  - Request bodies sent as JSON
  - Response bodies received as JSON
  - Automatic serialization/deserialization

#### Request Flow Example
```
Frontend (JavaScript) → Fetch API → HTTP Request → Spring Boot Controller → Service → Repository → Database
                                                      ↓
Frontend (JavaScript) ← JSON Response ← HTTP Response ← Spring Boot Controller ← Service ← Repository
```

#### Key Components:
1. **API Base URL**: `http://localhost:8080/api` (defined in JavaScript files)
2. **Authentication Header**: `Authorization: Bearer <JWT_TOKEN>`
3. **Content-Type Header**: `application/json`
4. **Token Storage**: Browser's `localStorage` (key: `token`)
5. **Error Handling**: HTTP status codes (401 for unauthorized, 400 for bad request, etc.)

### Version Control & Project Management
- **Git** - Version control system
- **Maven POM** - Project Object Model for dependency management

---

### Technology Stack Summary Table

| Category | Technology | Version | Purpose |
|----------|-----------|---------|---------|
| **Language** | Java | 17 | Core programming language |
| **Framework** | Spring Boot | 3.2.0 | Application framework |
| **Security** | Spring Security | 6.x | Authentication & authorization |
| **Authentication** | JWT (jjwt) | 0.12.3 | Token-based auth |
| **Database** | H2 | Latest | Embedded database |
| **ORM** | Hibernate/JPA | Included | Object-relational mapping |
| **Templating** | Thymeleaf | Included | Server-side templates |
| **XML Processing** | JAXB | 2.3.1/4.0.3 | XML binding |
| **Excel Export** | Apache POI | 5.2.4 | Excel file generation |
| **Validation** | Bean Validation | Included | Input validation |
| **Build Tool** | Maven | 3.6+ | Build & dependency management |
| **Frontend** | HTML5/CSS3/JS | ES6+ | Client-side UI |
| **Code Generation** | Lombok | Latest | Boilerplate reduction |

---

### Why These Technologies?

1. **Spring Boot**: Provides rapid development with minimal configuration, embedded server, and production-ready features
2. **JWT**: Enables stateless authentication, perfect for RESTful APIs and scalable applications
3. **H2 Database**: Ideal for development and prototyping; easily replaceable with production databases
4. **Thymeleaf**: Natural templating that works seamlessly with Spring and allows HTML preview
5. **JAXB**: Standard Java API for XML processing, ensuring compatibility and maintainability
6. **Maven**: Industry-standard build tool with excellent dependency management and plugin ecosystem

## Project Structure

```
online-banking-system/
├── src/
│   ├── main/
│   │   ├── java/com/banking/
│   │   │   ├── controller/      # REST and Web controllers
│   │   │   ├── model/           # Entity models (User, Account, Transaction)
│   │   │   ├── repository/      # JPA repositories
│   │   │   ├── service/         # Business logic services
│   │   │   ├── security/        # Security configuration
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   └── OnlineBankingApplication.java
│   │   └── resources/
│   │       ├── static/          # CSS, JavaScript files
│   │       ├── templates/       # Thymeleaf HTML templates
│   │       └── application.properties
│   └── test/                    # Test files
├── pom.xml                      # Maven dependencies
└── README.md
```

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Internet connection (for downloading dependencies)

## Installation & Setup

1. **Clone or download the project**
   ```bash
   cd online-banking-system
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```
   
   Or run the main class:
   ```bash
   java -jar target/online-banking-system-1.0.0.jar
   ```

4. **Access the application**
   - Web Interface: http://localhost:8080
   - H2 Console: http://localhost:8080/h2-console
     - JDBC URL: `jdbc:h2:file:./data/banking`
     - Username: `sa`
     - Password: (empty)

## Usage Guide

### 1. Register a New User

1. Navigate to http://localhost:8080/register
2. Fill in the registration form:
   - Username
   - Password
   - Email
   - First Name
   - Last Name
   - Phone Number
3. Click "Register"
4. You'll be redirected to the login page

### 2. Login

1. Navigate to http://localhost:8080/login
2. Enter your username and password
3. Click "Login"
4. You'll be redirected to the dashboard

### 3. Create an Account

1. After logging in, go to "My Accounts"
2. Click "Create New Account"
3. Select account type (Savings, Current, etc.)
4. Click "Create Account"
5. Your new account will be created with a unique account number

### 4. Transfer Funds

1. Go to "Transfer Funds"
2. Select your account from the dropdown
3. Enter the recipient account number
4. Enter the amount
5. (Optional) Add IFSC code for external transfers
6. Add a description
7. Click "Transfer Funds"

### 5. View Transactions

1. Go to "Transactions"
2. Select an account from the dropdown
3. View all transactions for that account
4. Use date filters to view transactions within a specific date range

### 6. Check Balance

1. Go to "My Accounts"
2. View the balance displayed for each account
3. Or use the API endpoint: `/api/accounts/{accountNumber}/balance`

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login and get JWT token
- `GET /api/auth/me` - Get current user details

### Accounts
- `GET /api/accounts` - Get all user accounts
- `POST /api/accounts?accountType={type}` - Create new account
- `GET /api/accounts/{accountNumber}` - Get account details
- `GET /api/accounts/{accountNumber}/balance` - Get account balance

### Transactions
- `POST /api/transactions/transfer` - Transfer funds
- `GET /api/transactions/{accountNumber}` - Get account transactions
- `GET /api/transactions/{accountNumber}/history` - Get transactions by date range
- `GET /api/transactions/{accountNumber}/balance` - Get account balance

### Admin (Requires ADMIN role)
- `GET /api/admin/transactions` - Get all transactions
- `GET /api/admin/transactions/xml` - Get transactions in XML format
- `POST /api/admin/accounts/{accountNumber}/deposit` - Deposit funds

## XML Transaction Storage

All transactions are automatically stored in XML format at `./data/transactions.xml`. The XML structure includes:
- Transaction ID
- From/To Account Numbers
- Amount
- Transaction Type
- Status
- Description
- Transaction Date
- Reference Number

## Security Features

1. **Password Encryption**: All passwords are encrypted using BCrypt
2. **JWT Authentication**: Secure token-based authentication
3. **Role-Based Access**: Different access levels for customers and admins
4. **Account Ownership Verification**: Users can only access their own accounts
5. **Transaction Validation**: Balance checks and account verification before transfers

## Database Schema

### Users Table
- id, username, password, email, firstName, lastName, phoneNumber
- enabled, twoFactorEnabled, role, createdAt, lastLogin

### Accounts Table
- id, accountNumber, accountType, balance, ifscCode
- user_id (foreign key), createdAt, lastUpdated

### Transactions Table
- id, transactionId, fromAccount_id, toAccount_id
- externalAccountNumber, amount, transactionType
- status, description, transactionDate, referenceNumber, remarks

## Configuration

Edit `src/main/resources/application.properties` to configure:
- Server port
- Database settings
- JWT secret and expiration
- XML file path
- Email settings (for notifications)

## Future Enhancements

- [ ] Two-factor authentication (2FA)
- [ ] Email/SMS notifications
- [ ] Bill payment and mobile recharge
- [ ] Scheduled/recurring payments
- [ ] Export transaction history to Excel
- [ ] Mini-statements
- [ ] Suspicious activity monitoring
- [ ] Compliance reports
- [ ] Integration with external payment gateways

## Troubleshooting

### Port Already in Use
If port 8080 is already in use, change it in `application.properties`:
```properties
server.port=8081
```

### Database Connection Issues
The application uses H2 database by default. If you encounter issues:
1. Check that the `data` directory is writable
2. Clear the database by deleting `./data/banking.mv.db` and restarting

### Authentication Issues
- Ensure you're using the correct username/password
- Check that the user account is enabled
- Verify JWT token is being sent in Authorization header

## Contributing

This is a project for learning and demonstration purposes. Feel free to fork and enhance!

## License

This project is open source and available for educational purposes.

## Support

For issues or questions, please check the code comments or create an issue in the repository.

---

**Note**: This application is for educational/demonstration purposes. For production use, additional security measures, testing, and compliance requirements should be implemented.

