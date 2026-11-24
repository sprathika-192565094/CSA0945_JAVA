# Assignment 5 – Persistence Engineering

## Part A – Design and pseudocode

### Task A1 – File format design

#### A1.1 Record layout (delimited text)

Field order: `passId, regNo, name, routeNo, boardingStop, validityDate, amountPaid`

Example row:

```text
P1001,192311001,Arun,12,Main Gate,2026-12-31,4500.00
```

- Delimiter: comma `,`
- If the `name` or `boardingStop` contains a comma, escape it as `"..."` or replace the comma with a safe placeholder before writing, then unescape when reading.
- The file is append-only; records are read line by line.

Binary layout (DataOutputStream):

```text
passId     : 10 bytes fixed width
regNo      : 12 bytes fixed width
name       : 30 bytes fixed width
routeNo    : 4 bytes int
boardingStop: 25 bytes fixed width
validDate  : 8 bytes yyyyMMdd
amountPaid : 8 bytes double
```

The binary alternative stores fixed-length fields and avoids delimiter ambiguity, but wastes space if the actual value is shorter than the fixed width.

#### A1.2 Pseudocode for operations

```text
procedure addPass(passId, regNo, name, routeNo, boardingStop, validityDate, amountPaid)
    open file for appending as BufferedWriter
    write passId + "," + regNo + "," + name + "," + routeNo + "," + boardingStop + "," + validityDate + "," + amountPaid
    write newline
    close writer
end procedure

procedure findPassById(passId)
    open file as BufferedReader
    while line = readLine() is not null do
        if line is empty then continue
        fields = split line by comma
        if count(fields) != 7 then continue  // damaged line
        if fields[0] == passId then return fields
    end while
    return null
end procedure

procedure listPassesOnRoute(routeNo)
    open file as BufferedReader
    while line = readLine() is not null do
        if line is empty then continue
        fields = split line by comma
        if count(fields) != 7 then continue
        if fields[3] == routeNo then print fields
    end while
end procedure
```

Why `BufferedReader/BufferedWriter` and `FileReader/FileWriter` (or `Files.newBufferedReader` / `Files.newBufferedWriter`): they reduce the number of operating-system calls and are simpler for line-based access. For tiny files, a buffered approach is still recommended.

#### A1.3 Damaged data handling

- If a line has the wrong number of fields, skip it and log the record number.
- If the file is missing, create it on first write and report a `file not found` condition when reading.
- If a write is interrupted by a power failure, a partially written line may be left behind. A safe write uses a temporary file and rename-on-success, or at least writes complete rows and verifies line shape before accepting the record.

### Task A2 – Don’t read what you don’t need

A single `read()` on `FileInputStream` reads a byte at a time. For a 50 MB file, the program makes roughly 50 million read calls plus boundary handling for `-1`. This is slow because each call crosses the Java-to-OS boundary and triggers repeated system overhead.

A buffered redesign should use `BufferedInputStream` or a byte array buffer (for example 8 KB or 64 KB) so each OS call fills a buffer and the code scans many bytes in memory before the next system read.

Buffering helps when the workload is sequential and large; it does little for a tiny file or a random-access pattern where data is not read in order.

Alternative: `Files.readAllLines()` reads the whole file into memory at once. This makes the time complexity O(file size) and uses O(file size) heap memory, while a buffered stream keeps memory roughly O(buffer size).

### Task A3 – Data access pattern

The repeated pattern is:

1. open connection
2. build SQL
3. execute query
4. map `ResultSet` to objects
5. close resources

Only the table and mapping details differ per class. A generic `Dao<T, K>` interface centralizes common CRUD logic and leaves the entity mapping to the concrete DAO implementation.

Example mapping:

```text
Student class:
  id (primary key)
  name
  registerNo
  dept

student table:
  student_id PK
  name
  register_no
  dept
```

The DAO is responsible for SQL generation and object conversion; service classes no longer embed SQL strings inline.

### Task A4 – What should be serialized?

The fields that must not be serialized are the live `Connection` references and any other runtime-only objects such as database handles or open streams. Mark them with `transient`.

When serializing `Hostel`, Java recursively serializes the entire object graph, including the list of rooms and the list of residents, unless a reference is marked transient or null. On deserialization, non-transient fields are reconstructed; transient fields become default values (`null` for objects). If a new field is added to `Resident`, deserialization of old files will set it to the default value (`null` or `0`); this is why `serialVersionUID` is important because it controls compatibility checks.

### Task A5 – Correctness and safety

```text
procedure transferMoney(hostelWallet, canteenWallet, amount)
    begin transaction
    try
        hostelWallet.balance = hostelWallet.balance - amount
        update hostel_wallet

        canteenWallet.balance = canteenWallet.balance + amount
        update canteen_wallet

        commit
    catch error
        rollback
        throw error
    end try
end procedure
```

This guarantees atomicity: either both updates happen or neither does.

The unsafe login query is vulnerable because input is concatenated directly into SQL. A crafted input such as:

```text
id = admin' -- 
pwd = anything
```

turns the query into:

```sql
SELECT * FROM users WHERE id = 'admin' -- ' AND pwd = 'anything'
```

The comment disables the rest of the condition, and the record is accepted without checking the password. A safe redesign is to use `PreparedStatement` with placeholders and parameter binding.

## Part B – Implementation notes

The repository includes working Java implementations for:

- `TaskB1/PassStore.java` — file-backed bus-pass store with validation and damaged-line skipping
- `TaskB1/BufferedLineCounter.java` — unbuffered and buffered line counting benchmark
- `TaskB2/UnsafeLoginDemo.java` — SQL injection demo using a local SQLite database
- `TaskB3/PassLoader.java` — transaction-safe batch loading
- `TaskB3/BusPassBatchDemo.java` — Statement vs PreparedStatement vs batch measurement
- `TaskB4/MigrationProgram.java` — migration and metadata report

Each of these uses the engineering cycle: understand, design, protect, implement, measure, challenge, interpret.
