# Online Auction System

A JavaFX desktop application for managing online auctions, supporting multiple user roles (Bidder, Seller, Admin) with real-time bidding, anti-sniping protection, and auto-bidding features.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| UI Framework | JavaFX 17 + MaterialFX 11.13.5 |
| Database | MySQL 8 |
| Build Tool | Maven 3.11.0 |
| Email Service | Jakarta Mail 2.0.1 (Gmail SMTP) |
| Logging | Logback 1.5.6 |
| Testing | JUnit 5 (Jupiter) |
| Code Quality | Spotless + Checkstyle (Google style) |

---

## Features

### Authentication
- Login/Signup with role selection (Bidder or Seller)
- Forgot password with 6-digit OTP verification via email (10-minute expiry)
- Role-based access control: `ADMIN`, `SELLER`, `BIDDER`

### Auction Browsing
- Browse listings with category filters (Jewelry, Watches, Bags, Fine Art, Cars, Others)
- Sort by price, date, or time remaining
- View auction details and full bid history
- Comment/discussion on auction items

### Bidding
- Place manual bids
- Auto-bidding with configurable maximum amount
- Anti-sniping protection (extends auction time on last-minute bids)
- Thread-safe concurrent bidding with `ReentrantLock`

### Seller
- Create new auction listings with image upload
- Manage active listings and view statistics

### Admin Dashboard
- View system statistics (total listings, active auctions, users, revenue)
- Manage all listings and users
- End auctions early

### User Profile
- Update profile information
- Deposit/balance management
- Watchlist
- Light/Dark theme switching

---

## Project Structure

```
src/main/java/com/example/auctionmanagementsystem/
├── Main.java                   # Entry point
├── App.java                    # JavaFX Application class
├── config/
│   └── DatabaseConnection.java # MySQL connection management
├── controller/                 # 17 JavaFX controllers (MVC)
├── dao/                        # Data Access Objects + Mapper pattern
├── exception/                  # Custom exception hierarchy
├── model/                      # Domain entities + Factory pattern
└── service/                    # Business logic (Bidding, AutoBid, AntiSniping, Email)

src/main/resources/
├── View/                       # FXML screen definitions
└── css/                        # Stylesheets (light & dark themes)
```

### Architecture

The project follows a classic N-tier architecture:

```
Controller (JavaFX/FXML) → Service (Business Logic) → DAO (Data Access) → MySQL
```

**Design Patterns used:**
- **MVC** — Controllers + FXML Views + Model entities
- **DAO + Mapper** — Generic `DAOInterface<T>` with `ResultSet` mapper classes
- **Factory** — `ItemFactory` creates typed items (`Electronics`, `Art`, `Vehicle`)
- **Singleton** — `SessionManager`, `ThemeManager`, `DatabaseConnection`

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.0+

### Database Setup

1. Create the database and user:

```sql
CREATE DATABASE auction_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'auction_system'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON auction_system.* TO 'auction_system'@'localhost';
FLUSH PRIVILEGES;
```

2. Import the schema (if a `.sql` file is provided) or run the application to auto-initialize tables.

3. Update credentials in [DatabaseConnection.java](src/main/java/com/example/auctionmanagementsystem/config/DatabaseConnection.java) if needed.

### Build & Run

```bash
# Clone the repository
git clone <repository-url>
cd OnlineAuctionSystem

# Build the project
mvn clean install

# Run the JavaFX application
mvn javafx:run
```

### Email Service Setup

The OTP email feature uses Gmail SMTP. Configure your Gmail credentials in the `EmailService` class or via environment variables before running.

---

## Development

### Code Style

This project enforces Google Java Style via Spotless and Checkstyle.

```bash
# Check formatting
mvn checkstyle:check

# Auto-format code
mvn spotless:apply
```

### Running Tests

```bash
mvn test
```

---

## User Roles

| Role | Capabilities |
|------|-------------|
| **Bidder** | Browse auctions, place bids, auto-bid, watchlist, comments |
| **Seller** | All Bidder features + create listings, manage own auctions |
| **Admin** | Full access: manage all listings, users, end auctions early |

---

## Current Status

Core architecture and UI are complete. Backend integration (persisting bids, listings, comments, profile updates to the database) is in progress. See [ProjectFeatures.md](ProjectFeatures.md) for the detailed feature checklist.
