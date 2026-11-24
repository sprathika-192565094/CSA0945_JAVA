# Programming in Java — Assignment 3

**Student A Vinaay babu A / register number:** [192571034]  
**Student B name / register number:** [Enter details]  
**Java version:** Java 21 (source is kept compatible with Java 11+)  
**Submission date:** [Enter date]  
**Student A GitHub link:** [Add repository link]  
**Student B GitHub link:** [Add repository link]

Detailed design answers, complexity notes, and test evidence are in
[Assignment-Answers.md](./Assignment-Answers.md).

## Compile and run

Open PowerShell in this folder. Compile each task separately:

```powershell
javac Task01\ATMWithdrawal.java
java -cp Task01 ATMWithdrawal ACCT-1001 100

javac Task02\StorageDemo.java
java -cp Task02 StorageDemo

javac Task03\SalesReportDemo.java
java -cp Task03 SalesReportDemo

javac Task04\ScannerStations.java
java -cp Task04 ScannerStations

javac Task05\SeatBooking.java
java -cp Task05 SeatBooking

javac Task06\TokenCounter.java
java -cp Task06 TokenCounter

javac Task07\ResultProcessor.java
java -cp Task07 ResultProcessor
```

Run the commands from the assignment root because the examples use relative input paths. Each task folder contains its source and any small input files. Task 7 generates its own temporary data.

## Task results

| Folder | Result |
|---|---|
| Task01 | Valid withdrawals dispense cash; malformed inputs, negative values, insufficient balance, and missing card files follow distinct exception paths. |
| Task02 | The storage layer wraps an `IOException`; the caller sees a meaningful checked exception and can inspect the original cause. |
| Task03 | The manual `finally` and try-with-resources versions produce matching totals on valid inputs and both report an injected missing-file failure. |
| Task04 | Both `Thread` and `Runnable` stations complete; `join()` ensures the total is printed after the stations finish. Completion order may vary. |
| Task05 | The unsynchronized demonstration permits duplicate bookings; synchronized method/block variants enforce one sale per seat. |
| Task06 | Producer-consumer processing collects all tokens; wallet transfers use consistent lock ordering to avoid circular wait. |
| Task07 | Callable jobs process valid, corrupt, and missing score files independently and produce a final success/failure summary for each pool size. |

## Submission checklist

- Replace the identity and repository-link placeholders above.
- Each partner must compile and execute every task in their own repository and keep their own progressive commit history.
- Capture and add your own output screenshots and record your own repeated-run measurements; machine timing and thread scheduling vary.
- Create/share the faculty-accessible repository and submit its working link. This local folder is not a GitHub repository.

