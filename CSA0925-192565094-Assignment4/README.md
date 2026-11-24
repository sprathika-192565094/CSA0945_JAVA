# Assignment 4 – Event-Driven GUI Design

**Student:** Vinaay babu A
**Register number:** 192571034
**Java version:** 17  
**Repository:** CSA0925-1924260165-Assingment4

This repository contains the completed Java GUI/GUI-event work for Assignment 4.

## Project structure

- `Task01/` – Fee payment redesign and service separation
- `Task02/` – Requirement analysis, event table, component hierarchy, pseudocode, trace
- `Task03/` – Library search screen with nested layouts and resizing behaviour
- `Task04/` – Draw pad fix with repaint-safe click tracking
- `Task05/` – Currency converter in four event-handling styles
- `Task06/` – Event registration desktop application with Swing + JDBC design
- `REPORT.md` – Written design report for all tasks

## Compile and run

From the project root:

```powershell
$build = Join-Path $env:TEMP 'assignment4-build'
New-Item -ItemType Directory -Force -Path $build | Out-Null

$files = @(
  'Task01\FeeWindow.java',
  'Task01\FeeService.java',
  'Task03\LibrarySearchScreen.java',
  'Task04\DrawPad.java',
  'Task05\Converter.java',
  'Task06\EventRegistrationApp.java',
  'Task06\EventRegistrationService.java',
  'Task06\Registration.java',
  'Task06\RegistrationDAO.java',
  'Task06\DatabaseConfig.java'
)

javac -d $build $files
```

Run the demo UIs:

```powershell
java -cp $build FeeWindow
java -cp $build LibrarySearchScreen
java -cp $build DrawPad
java -cp $build Converter
java -cp $build EventRegistrationApp
```

## Notes

- Task 1, 3, 4, 5 and 6 are implemented as working Java programs.
- Task 2 and the design discussion are captured in `REPORT.md`.
- The Task 6 JDBC application is prepared for a MySQL database and falls back to an in-memory repository if the database is unavailable, so the application can still be demonstrated locally.
