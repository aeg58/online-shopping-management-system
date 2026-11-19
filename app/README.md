# Interface

Spring Boot web interface for the Online Shopping Management System. Owner: Erdem

## Requirements

- Java 21
- Maven (the included `./mvnw` wrapper downloads it automatically)
- Microsoft SQL Server on `localhost:1433`

## Create the database

Run these two files in order, in SQL Server Management Studio or `sqlcmd`:

1. `database/01_schema.sql`
2. `database/02_seed.sql` (select the `shoppingmngmntsystem` database first)

## Set the database login

The application reads the login from two environment variables:

| Variable | Default |
|---|---|
| `DB_USER` | `sa` |
| `DB_PASSWORD` | empty |

macOS / Linux:

```bash
export DB_USER=sa
export DB_PASSWORD='your-password'
```

Windows PowerShell:

```powershell
$env:DB_USER = "sa"
$env:DB_PASSWORD = "your-password"
```

You can also create `app/application-local.properties` for local settings. Git ignores this file.

## Run

From the `app/` folder:

```bash
./mvnw spring-boot:run
```

On Windows use `mvnw.cmd spring-boot:run`.

Open <http://localhost:8080>. The demo accounts and a step-by-step test list are in [TESTING.md](TESTING.md).
