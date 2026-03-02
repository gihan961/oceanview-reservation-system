# OceanView Reservation System

A hotel reservation management system built with Java 17, Jakarta EE 10, and MySQL 8.0. Features role-based access control, dynamic pricing strategies, and a responsive web interface.

## Tech Stack

- **Backend:** Java 17, Jakarta Servlet 6.0, JDBC
- **Frontend:** HTML5, CSS3, Vanilla JavaScript
- **Database:** MySQL 8.0
- **Build:** Maven 3.9+
- **Server:** Apache Tomcat 10.1+

## Features

- **Authentication & Authorization** - Session-based login with three roles: Admin, Manager, Staff
- **Room Management** - 15 rooms across Standard, Deluxe, Suite, and Presidential categories (LKR pricing)
- **Reservations** - Create, view, update, and cancel reservations with availability checking
- **Dynamic Pricing** - Standard, weekend, seasonal, long-stay discount, and premium room strategies
- **Dashboard** - Real-time occupancy stats, revenue summaries, and recent activity
- **Reports** - Occupancy and revenue reports with date filtering
- **Invoices** - Printable invoice generation for completed reservations
- **Account Management** - Admin can create new user accounts

## RBAC Permissions

| Feature | Admin | Manager | Staff |
|---------|-------|---------|-------|
| Dashboard | Yes | Yes | Yes |
| View Rooms | Yes | Yes | Yes |
| Manage Rooms | Yes | Yes | No |
| Add Reservations | Yes | Yes | Yes |
| View Reservations | Yes | Yes | Yes |
| Edit/Cancel Reservations | Yes | Yes | No |
| Reports | Yes | Yes | No |
| Invoices | Yes | Yes | Yes |
| Create Accounts | Yes | No | No |

## Prerequisites

- JDK 17+
- Apache Tomcat 10.1+
- MySQL 8.0+
- Maven 3.9+

## Database Setup

1. Create the database and tables:
```sql
mysql -u root -p < database/schema.sql
```

2. Create stored procedures and triggers:
```sql
mysql -u root -p < database/procedures_triggers.sql
```

3. Update `src/main/resources/db.properties` with your MySQL credentials.

## Build & Deploy

```bash
mvn clean package -Dmaven.test.skip=true
```

Copy `target/reservation.war` to Tomcat's `webapps/` directory and start the server.

Access at: `http://localhost:8080/reservation/`

## Default Accounts

| Username | Password | Role |
|----------|----------|------|
| admin | password123 | ADMIN |
| manager | password123 | MANAGER |
| staff | password123 | STAFF |

## Project Structure

```
src/main/java/com/oceanview/
  controller/    - 9 servlets (Login, Logout, Register, Room, Reservation, Dashboard, Invoice, Report, AvailableRooms)
  model/         - User, Room, Reservation, Report, ErrorResponse
  dao/           - DAO interfaces
  dao/impl/      - JDBC implementations
  service/       - AuthService, RoomService, ReservationService, ReportService
  filter/        - Authentication, CORS, RequestLogging, NoCache, ExceptionHandler
  exception/     - Custom exception hierarchy
  reservation/   - Pricing strategies (Standard, Weekend, Seasonal, LongStay, Premium)
  factory/       - ServiceFactory
  util/          - DBConnection, LoggerUtil, RBACUtil, ExceptionHandlerUtil

src/main/webapp/
  pages/         - 11 HTML pages
  js/            - 12 JavaScript files (including auth.js for RBAC)
  css/           - Stylesheets
  WEB-INF/       - web.xml configuration
```