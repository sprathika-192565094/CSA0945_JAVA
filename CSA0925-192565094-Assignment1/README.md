# Assignment 1 — Campus Smart-Parking System

Student: S.Prathika
Register number:192565094
GitHub repository: [CSA0925-192565094-Assingment1](https://github.com/sprathika-192565094/CSA0945_JAVA/edit/main/CSA0925-192565094-Assignment1)  
Java version: 17

## Assignment report

[`REPORT.md`](REPORT.md) contains the written responses for Tasks 1–12, including design assumptions, pseudocode, UML-style hierarchy, complexity comparisons, test cases, and the Task 12 design decision.

## Task folders

| Folder | Source code | What it demonstrates |
|---|---|---|
| `Task01` | `Task01Demo.java` | Domain objects and responsibilities |
| `Task02` | `Task02Demo.java` | Types, jagged arrays, and fee rounding |
| `Task03` | `ParkingFacility.java` | Completed allocation/release program from the assignment |
| `Task04` | `Task04Demo.java` | Parameterized, overloaded, and copy constructors |
| `Task05` | `Task05Demo.java` | Shared static ticket counter |
| `Task06` | `Task06Demo.java` | Inheritance and constructor order |
| `Task07` | `Task07Demo.java` | Overriding, equality, and fee calculation |
| `Task08` | `FeeDemo.java` | Completed vehicle/interface program from the assignment |
| `Task09` | `Task09Demo.java` | Abstract class and capability interface |
| `Task10` | `Task10Demo.java` | Encapsulation and validation |
| `Task11` | `Task11Runner.java` | Runs the complete implementation tests and parking scenario |
| `Task12` | `Task12Runner.java` | Runs the adapted design tests and parking scenario |

The full parking-system implementation is kept **once** in [`Task11/src/`](Task11/src). Task12 reuses these model, pricing, service, and test classes; its folder contains only its own entry-point source file, avoiding duplicate copies.

## Compile and run

Open PowerShell in this folder:

```powershell
$build = Join-Path $env:TEMP 'parking-assignment-build'
New-Item -ItemType Directory -Force -Path $build | Out-Null
$roots = @('Task01','Task02','Task03','Task04','Task05','Task06',
    'Task07','Task08','Task09','Task10','Task11','Task12')
$sources = foreach ($root in $roots) {
    Get-ChildItem -LiteralPath $root -Recurse -Filter '*.java' |
        ForEach-Object { $_.FullName }
}
javac -Xlint:all -d $build $sources
```

Run the complete assignment tests and demos:

```powershell
java -cp $build Task11Runner
java -cp $build Task12Runner
```

Both runners use the one implementation in `Task11/src/parking/`; each runs the same 15 test cases and demo. `Task11Runner` is the primary run.

Run an individual task example:

```powershell
java -cp $build Task01Demo
java -cp $build Task02Demo
java -cp $build ParkingFacility
java -cp $build Task04Demo
java -cp $build Task05Demo
java -cp $build Task06Demo
java -cp $build Task07Demo
java -cp $build FeeDemo
java -cp $build Task09Demo
java -cp $build Task10Demo
```

The test runner should report `Passed 15 tests.` Verified console transcripts are in `Task03/output.txt`, `Task08/output.txt`, and `Task11/`.

## Before submission

Replace the student and repository placeholders above and on the report. Create the required GitHub repository and add progressive commits yourself. The text transcripts are included; capture screenshots of actual runs if required by the instructor.
