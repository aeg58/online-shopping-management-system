# Online Shopping Management System

SEN22321E Database System Design · Fall 2026 · Fatih Sultan Mehmet Vakıf University
Instructor: Dr. Öğr. Üyesi Abdulkerim Mohammed Yibre

A system for managing an online store including products, customers, categories, orders and payments, built on a relational database. Users interact with the system through an interface according to their role (Admin, Seller, Customer).

## Team

| Member | Student ID | Responsibility |
|---|---|---|
| Mohammed Nour | | Database design: ER diagram, relational schema, DDL, business rules |
| Humam Berkdar | 2321051359 | Data and queries: sample data, queries, views, transactions |
| Ali Erdem Gedik | 2321051008 | Interface: login, role-based screens, DB connection; documentation and class diagram |

## Repository layout

```
database/   SQL scripts, run in numeric order
  01_schema.sql        tables, keys and constraints
  02_seed.sql          sample data
  03_views.sql         views
  04_queries.sql       report and demo queries
  05_transactions.sql  order and payment transactions
app/        user interface (technology to be decided)
docs/       project documentation, diagrams and screenshots
```

## Running the database

1. Create an empty database named `OnlineShop`.
2. Run the scripts in `database/` in order: `01` → `05`.

## Deadlines

| Date | Milestone |
|---|---|
| 17 Nov | Table list and technology agreed |
| 19 Nov | ER diagram and `01_schema.sql` done |
| 21 Nov | Sample data and queries done, interface connected to the database |
| 22 Nov | Team test |
| 23 Nov | Documentation complete |
| 24 Nov 23:55 | Each member uploads to LMS as `Fullname_IDNO_DB_OnlineShoppingManagementSystem` |

See [CONTRIBUTING.md](CONTRIBUTING.md) for how we work (Turkish) and [docs/TASKS.md](docs/TASKS.md) for the task list.
