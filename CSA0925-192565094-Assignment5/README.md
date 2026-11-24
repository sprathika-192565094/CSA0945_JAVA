# CSA09 Assignment 5 – Data Persistence Engineering

Student Name : S.Prathika
Reg No: 192565094

This project contains a working Java implementation of the Assignment 5 tasks in the course PDF.

## Contents

- `TaskB1/PassStore.java` – file-backed pass store with safe reads and damaged-line skipping
- `TaskB1/BufferedLineCounter.java` – line counting benchmark for unbuffered vs buffered reads
- `TaskB2/UnsafeLoginDemo.java` – injection demonstration using a local SQLite database
- `TaskB3/PassLoader.java` – JDBC batch insert logic with rollback support
- `TaskB3/BusPassBatchDemo.java` – Statement vs PreparedStatement performance comparison
- `TaskB4/MigrationProgram.java` – migration from pass file to database with validation and metadata reporting
- `docs/REPORT.md` – concise Part A design explanation with pseudocode

## Build and run

From the project root:

```powershell
javac -cp ".;sqlite-jdbc-3.49.1.0.jar" TaskB1\PassStore.java
java -cp ".;sqlite-jdbc-3.49.1.0.jar;TaskB1" PassStore

javac -cp ".;sqlite-jdbc-3.49.1.0.jar" TaskB1\BufferedLineCounter.java
java -cp ".;sqlite-jdbc-3.49.1.0.jar;TaskB1" BufferedLineCounter

javac -cp ".;sqlite-jdbc-3.49.1.0.jar" TaskB2\UnsafeLoginDemo.java
java -cp ".;sqlite-jdbc-3.49.1.0.jar;TaskB2" UnsafeLoginDemo

javac -cp ".;sqlite-jdbc-3.49.1.0.jar" TaskB3\PassLoader.java TaskB3\BusPassBatchDemo.java
java -cp ".;sqlite-jdbc-3.49.1.0.jar;TaskB3" BusPassBatchDemo

javac -cp ".;sqlite-jdbc-3.49.1.0.jar" TaskB4\MigrationProgram.java
java -cp ".;sqlite-jdbc-3.49.1.0.jar;TaskB4" MigrationProgram
```

On Linux/macOS, replace the classpath separator `;` with `:`.

## Notes

- The examples use SQLite as a local JDBC test database because the assignment requires a local database for testing and demonstration.
- `REPORT.md` gives the required conceptual reasoning for Part A and the engineering interpretation.
- Sample output files are produced when the Java programs are executed.
