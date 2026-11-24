# Assignment 3 — Design, analysis, and test record

> Fill in the names, register numbers, screenshots, and measurements from each
> partner's own execution before submitting. These are sample implementations
> and reproducible test instructions, not a claim that both partners ran them.

## Q1 — ATM failure handling

Relevant hierarchy:

```text
Throwable
├── Error (for example OutOfMemoryError; not caught by the ATM)
└── Exception
    ├── IOException (checked; card file read failure)
    ├── InsufficientFundsException (checked; ATM business rule)
    └── RuntimeException (unchecked)
        └── IllegalArgumentException
            └── NumberFormatException (invalid numeric text)
```

| Failure point / test | Type | Checked? | Handler |
|---|---|---:|---|
| `java -cp Task01 ATMWithdrawal ACCT-1001 100` | none | — | Dispense and `finally` |
| `ATMWithdrawal ACCT-1001 abc` | `NumberFormatException` | No | Number-format catch |
| `ATMWithdrawal ACCT-1001 -10` | `IllegalArgumentException` | No | Invalid-request catch |
| `ATMWithdrawal ACCT-1001 501` | `InsufficientFundsException` | Yes | Insufficient-funds catch |
| `ATMWithdrawal ACCT-1001 10 missing-card.txt` | `IOException` / `NoSuchFileException` | Yes | Card-reader multi-catch |
| Read denied by the operating system | `SecurityException` | No | Card-reader multi-catch |
| `ATMWithdrawal --uncaught` | `IllegalStateException` | No | Intentionally uncaught demo |

`NumberFormatException` must be caught before `IllegalArgumentException`
because it is a subclass. The multi-catch combines unrelated `IOException` and
`SecurityException` cases. The `finally` block prints session closure whether
the operation succeeds, a caught exception occurs, or an uncaught exception
propagates. It does not guarantee that the JVM can recover from every `Error`
or abrupt process termination.

**Uncaught trace:** run `java -cp Task01 ATMWithdrawal --uncaught`. The last
line names `ATMWithdrawal.main`; the preceding exception line gives the type
and message. The stack frame identifies the method and source line where the
exception was thrown. In the local Java 21 run it was
`ATMWithdrawal.main(ATMWithdrawal.java:18)`. The `finally` message is printed
before the uncaught-exception trace during normal exception unwinding.

Putting `catch (Exception e)` before `catch (IOException e)` makes the latter
unreachable and fails compilation. One broad catch can be acceptable at a
top-level boundary that must log/report any ordinary exception, but it hides
which recovery action is appropriate and can turn programming defects into
misleading user-facing messages. `Error` is not caught by `Exception`.

**Without exceptions:** validate tokens with `Scanner.hasNextDouble()` (then
consume invalid tokens), check amount > 0 and amount <= balance, and return a
status such as `OK`, `BAD_INPUT`, `INVALID_AMOUNT`, `INSUFFICIENT_FUNDS`, or
`CARD_READ_ERROR`. Parsing still takes O(L) for a token of length L; each
business check is O(1), with O(1) extra state. Constructing an exception and
capturing its stack trace costs O(D) time and space for stack depth D; returning
a status avoids that stack-trace cost, though exceptions make unexpected
failure propagation clearer.

## Q2 — Exception hierarchy and propagation

Implemented hierarchy and layers:

```text
ExamException (checked)
├── ExamWindowClosedException
├── StudentNotRegisteredException
└── SubmissionStorageException
    └── cause: IOException

ExamController -> SubmissionService -> Storage
```

The exam-rule failures are checked because they are expected outcomes that
callers must handle. Storage failures are also checked because the service
cannot complete a save and the controller must report failure. An unchecked
exception would be appropriate for a programming error or violated internal
invariant, not an ordinary student submission outcome.

`Storage.save` declares `throws SubmissionStorageException`, catches the
low-level `IOException`, and throws a meaningful domain exception with the
original cause. It propagates through the service; the controller catches
`ExamException` and gives the student a meaningful message. The cause chain
can be verified with `getCause()` and `printStackTrace()`.

Run `java -cp Task02 StorageDemo`. The first submission is saved; the closed
window and unregistered student produce domain messages; the oversized answer
produces `Could not store answer02.pdf` with `Disk quota exceeded` as its
cause. If the `IOException` is not passed to the constructor, `getCause()`
returns `null` and the original low-level message/stack frames are lost.
Re-throwing a new exception without its cause preserves the wrapper message
only; the storage-layer location and reason are no longer connected to it.

| Test scenario | Originating layer | Controller result |
|---|---|---|
| Window closed | Service | `The examination window is closed` |
| Student not registered | Service | `The student is not registered for this course` |
| File exceeds quota | Storage | `Could not store answer02.pdf`; cause is `Disk quota exceeded` |

If these custom exceptions extended `RuntimeException`, `throws` declarations
would not be required and callers would not be forced by the compiler to catch
or declare them. The controller could still catch them, but omission would be
allowed. Capturing a stack trace costs O(D) for D stack frames. A chained
exception retains its cause object and that cause's trace, so retained memory
is approximately the sum of the reachable messages and stack frames until
the exception graph is no longer reachable.

## Q3 — Resource safety and report generation

`SalesReportDemo` has a manual `finally` version and a try-with-resources
version. Both close the current input reader before proceeding and write the
same total for the supplied valid files. Run:

```powershell
java -cp Task03 SalesReportDemo
java -cp Task03 SalesReportDemo --fail
```

The injected failure points at a missing second store file. Both versions log
the file being read and report failure. The manual version has already opened
and truncated its output, so that report is incomplete; the try-with-resources
version does not open its output until all inputs have been read.
The manual writer is closed in `finally`, while each input reader is closed by
its own try-with-resources statement. A common buggy manual draft calls
`close()` only after the processing loop; an exception skips that line, so the
writer remains open. Moving close into `finally` fixes that defect. For a
failure after opening one input and one output, the corrected versions leave
zero of those handles open; a broken normal-path-only close leaves the output
open until later cleanup. Handle counts are OS/JVM dependent, so record an
observed count only when measured on the submission machine.

For a debugger demonstration, set a breakpoint on the writer's `close()` in
`generateManual`, inject `--fail`, and step from the failed second-file open
to the `finally` block. In the buggy normal-path-only draft, execution jumps
out before its close line; in this version, `finally` is reached and closes
the writer.

| Same missing-second-file input | Current reader after failure | Output writer after failure |
|---|---:|---:|
| Corrected manual `finally` implementation | 0 | 0 |
| Try-with-resources implementation | 0 | 0 |
| Buggy draft that closes output only after the loop | 0 | 1 until later cleanup |

For f files and r total records, processing is O(r) time and uses O(1)
application state beyond the current line and report output. These versions
open one input at a time plus the output (at most two handles). If a design
opens all f inputs before processing, a failure can leave O(f) handles unless
all are closed. Try-with-resources closes resources in reverse declaration
order. If the body throws and `close()` also throws, the body exception remains
primary and close failures are attached as suppressed exceptions.

Try-with-resources alone does not make the report robust against partial
output, invalid data, disk-full errors, wrong totals, or retry requirements.
Write to a temporary file and atomically replace the final report only after
all inputs validate when all-or-nothing publication is required.

Observed locally: the valid reports matched (`Reports match: true`). With
`--fail`, both versions logged the missing second input and stopped before
publishing a complete report.

## Q4 — Thread creation and lifecycle

`ScannerStations` demonstrates both extending `Thread` and supplying a
`Runnable` to a `Thread`. Prefer `Runnable` when task logic should remain
separate from thread management or the class already extends another class.
`start()` asks the runtime to schedule a new thread; a direct `run()` call is
an ordinary method call on the current thread. Verify with
`java -cp Task04 ScannerStations --run-inline`.

Lifecycle sketch:

```text
NEW --start--> RUNNABLE <--> (waiting / timed waiting / blocked)
                    \---- run ends ----> TERMINATED
```

Each station is created in NEW, becomes runnable after `start()`, enters timed
waiting in `sleep()`, and terminates when `run()` returns. `join()` waits for
termination; therefore main prints the total only after all station updates
are complete. `isAlive()` is false after each join.

Run `java -cp Task04 ScannerStations` ten times and record the completion
orders. Use `java -cp Task04 ScannerStations --priority-demo` to repeat with
different priorities. The order can differ because scheduling and sleep
durations vary. Changing priority is only a scheduling hint, not an ordering
guarantee.

Ten local completion orders (JDK 21):

| Run | Station completion order |
|---:|---|
| 1 | 4, 2, 1, 3 |
| 2 | 3, 2, 1, 4 |
| 3 | 1, 2, 3, 4 |
| 4 | 2, 4, 3, 1 |
| 5 | 3, 1, 4, 2 |
| 6 | 4, 2, 1, 3 |
| 7 | 3, 4, 2, 1 |
| 8 | 2, 1, 3, 4 |
| 9 | 3, 1, 4, 2 |
| 10 | 1, 2, 3, 4 |

One `--priority-demo` run completed 3, 4, 2, 1; priority did not make order
deterministic. The daemon demonstration printed that main ended and did not
print the daemon's completion message before JVM exit.
`java -cp Task04 ScannerStations --daemon-demo` returns from main without
joining; the JVM may end before the daemon prints its completion message.
Correctness must depend on joins/coordination, not the observed order.

For n similarly sized scripts and p workers, ideal work time is about O(n/p)
plus scheduling and I/O overhead; worst-case work remains O(n). A manually
created thread per station uses O(p) thread stacks. A fixed executor uses up
to p worker stacks and can queue tasks (up to O(n) queued references for a
large submitted batch). An executor centralizes lifecycle and bounds worker
count.

## Q5 — Atomic seat booking

`SeatBooking` includes a barrier-controlled unsynchronized version, a
synchronized method, and a synchronized block on a per-seat lock. The
barrier makes every contender observe a free seat before any contender marks
it booked, so the demonstration reliably records 100 successful attempts for
10 seats. The `bookingsMade++` field itself is also subject to lost updates;
the separate atomic counter is used for reliable demonstration evidence.

The critical section is the combined check (`booked[seat]`) and state update
(`booked[seat] = true; bookingsMade++`). Locking only the write or only the
check is insufficient. Run `java -cp Task05 SeatBooking`; each synchronized
version should sell exactly 10 seats. The timing output is machine-dependent
and uses 10,000 uncontended unique-seat attempts; run repeatedly and record
the observed times rather than treating one run as a performance conclusion.
In one local run, the unsynchronized version allowed 100 successful attempts
for 10 seats, while both synchronized versions sold exactly 10. The repeated
ten-run check recorded 100/10/10 successful attempts or bookings each time
(unsynchronized/method/block). One timing sample was
0.792 ms for the method lock and 0.738 ms for the per-seat block; one
uncontended sample is not enough to conclude that either is generally faster.

Both `synchronized` methods on one object and `synchronized(this)` blocks use
that object's intrinsic monitor. Here the block instead locks the selected
seat's private lock, allowing bookings on different seats to proceed in
parallel. Static synchronization locks the `Class` object and is needed only
when the protected state is static/shared across instances. For 20 screens,
use a per-screen/per-seat lock or atomic seat state so unrelated screens do
not contend on one global lock.

With s seats, a boolean array is O(s); the block variant adds O(s) lock
objects. An `AtomicBoolean[]` also uses O(s) references/objects and
`compareAndSet(false, true)` is O(1) per attempt; retries under contention can
increase work. A `ReentrantLock` per seat has O(s) locks and explicit unlock
in `finally`. `ConcurrentHashMap.putIfAbsent` is expected O(1) per booking
and O(s) space, but is unnecessary for fixed numbered seats.

## Q6 — Deadlock prevention and producer-consumer

`TokenCounter` is a five-slot circular buffer. `put` waits while full; `take`
waits while empty; both perform the state update under the object's monitor
and call `notifyAll()` afterwards. The two consumers collect tokens 1 through
20, so their sum is 210. The program prints every observed count after a
put/take; the observed run ranged from 0 to 5 and summed to 210. `wait()` must
be called while owning the same monitor and inside a `while` loop: a wake-up
does not guarantee the condition is now true (another consumer may have taken
the token, or a spurious wake-up may occur). Replacing `while` with `if` can
proceed when the condition is still false. `sleep()` pauses but retains owned
monitors; `wait()` releases the monitor until notified and reacquires it
before returning.

`notifyAll()` is safer when producers and consumers wait on the same monitor:
`notify()` can wake a thread whose condition is still false while leaving a
thread that could progress asleep. All waiters re-check their predicates.
For opposite wallet transfers, the unsafe demo acquires wallet A then B in
one thread and B then A in another; run `java -cp Task06 TokenCounter
--deadlock-demo` to observe both daemon threads still alive. This exhibits
mutual exclusion, hold-and-wait, no preemption, and circular wait. The safe
transfer always locks the lower wallet id before the higher id, preventing
circular wait. The demo uses daemon threads so the demonstration cannot keep
the process alive.

For capacity C=5, each put/take is O(1) and buffer space is O(C); a transfer
uses two locks and O(1) state. `ArrayBlockingQueue<Integer>(5)` replaces the
hand-written wait/notify protocol with a bounded, thread-safe queue. A timed
`tryLock()` approach avoids waiting forever on a lock but must handle timeout,
retry, and possible starvation; lock ordering is usually simpler. Both
problems require coordination for progress: a queue blocks until a predicate
becomes true, while lock ordering prevents a circular wait.

## Q7 — Concurrent fault-tolerant result processing

**Measurable requirements:** process 10,000 paths; validate each score as an
integer in 0..100; one missing or corrupt file must not cancel other work;
the final totals must add to 10,000; with the fixture, expect 9,998 successes
and 2 failures at every pool size.

**Design/class sketch:**

```text
ResultProcessor
  -> ExecutorCompletionService<Outcome>
      -> fixed ExecutorService(p)
          -> Callable<Outcome> per score file
              -> ConcurrentLinkedQueue<Outcome>
```

Each callable reads and validates exactly one file, returning an outcome;
file I/O and parse failures become failure outcomes. The completion service
lets the main thread consume completed jobs without waiting in submission
order. The concurrent queue stores the outcomes shared by worker tasks.
`Future.get()` is used to surface unexpected task failures; the pool is shut
down and awaited in `finally`.

Pseudocode:

```text
for each path: submit Callable(process one file; queue its success/failure outcome)
repeat once per path: take completed Future; get it, recording unexpected failure
count the concurrent result queue and print successes, failures, and elapsed time
```

The program creates and removes a fixture directory automatically. Run
`java -cp Task07 ResultProcessor`; record elapsed milliseconds for pools of
1, 2, 4, and 8 from your own machine. The correct summary at all four sizes is
9,998 success, 2 failure, 10,000 total. More workers may not help when disk
I/O, storage bandwidth, CPU count, thread scheduling, or filesystem caching
is the bottleneck.

One local timing run (milliseconds): pool 1 = 5,807; pool 2 = 1,955;
pool 4 = 813; pool 8 = 552. Every run reported 9,998 successes, 2 failures,
and 10,000 total. This is a single-machine sample, not a guaranteed ranking.

For total input size N, ideal balanced processing is O(N/p), with O(N) total
work. This implementation submits and retains O(n) futures/completion
references and O(n) result objects, in addition to O(p) worker stacks. A
production system can use bounded submission/backpressure to cap queued work.
Parallel streams use a shared common pool and do not provide the same explicit
pool sizing/isolation; exceptions still need handling inside each task.
Creating one thread per file uses O(n) thread stacks and makes 10,000-thread
resource exhaustion likely. If malformed/missing-file exceptions escape a
parallel stream or a future, callers see aggregate/per-task failures rather
than a complete domain summary; converting expected per-file failures into
individual outcomes keeps the rest of the batch running. One remaining
limitation is that the summary is in-memory and not persisted atomically.
