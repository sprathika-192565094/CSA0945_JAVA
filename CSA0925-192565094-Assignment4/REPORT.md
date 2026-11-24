# Assignment 4 – Event-Driven GUI Design

## Task 1 – GUI Design Review

### 1.1 Design deficiencies

1. Interface logic and database logic are mixed in a single `ActionListener`, so the UI is hard to read and change.
2. The code compares action commands using string equality rather than button identity or dedicated listeners.
3. Empty or invalid input is not checked before conversion, so `NumberFormatException` can crash the event-thread.
4. Database access and `Thread.sleep(3000)` are executed on the event-dispatch thread, freezing the window and making the form unresponsive.
5. There is no feedback to the user about success, failure, or progress.
6. The `Receipt` button is never implemented; it has no handler and does not produce any result.
7. SQL is built by concatenating user input, which is vulnerable to SQL injection.
8. The window and database connection are never closed or cleaned up.
9. Checked exceptions such as `SQLException` or `InterruptedException` are not handled.

### 1.2 Repair design

The repaired design separates concerns:

- `FeeWindow` is responsible only for collecting input and displaying status.
- `FeeService` validates values, performs payment logic, and returns a result object.
- Each button has its own action handler method and validation is centralized.

Class structure:

```text
FeeWindow
  - TextField regNo, amount
  - Button pay, clear, receipt
  - TextArea status
  + actionPay()
  + actionClear()
  + actionReceipt()

FeeService
  + validateRegistration(String)
  + validateAmount(String)
  + processPayment(String, double)
  + getReceipt(String)
```

Event handling is rewritten as short methods that call the service and update the display, keeping business logic outside the UI.

### 1.3 Design defence

This redesign is easier to test because validation and payment rules are isolated in `FeeService`, which can be exercised directly without launching a UI. It is easier to maintain because UI changes do not require rewriting payment logic, and new validations or payment rules can be added in one place. The event handlers remain small and readable.

### Alternative concept and complexity analysis

If each button used a dedicated listener object or a Swing `Action`, the event dispatch cost is still O(1) from the event source to the listener, but the code becomes more explicit and allows each button to keep its own behaviour. For k buttons, a single if-else chain performs up to k comparisons; a per-button listener can dispatch directly with O(1) look-up. The listener-object version uses O(k) additional objects for k buttons, while the if-else version uses O(1) extra objects.

---

## Task 2 – Event-Driven Design for Hospital Appointment Desk

### 2.1 Event sources and listeners

| Event source | Event object | Listener | Action taken |
|---|---|---|---|
| Department combo box | `ItemEvent` / `ActionEvent` | `ItemListener` / `ActionListener` | Refresh available doctor list for selected department |
| Patient ID field | `ActionEvent` | `ActionListener` | Validate patient ID; allow Enter trigger |
| Doctor list | `ActionEvent` / `ItemEvent` | `ActionListener` / `ItemListener` | Update selected doctor |
| Time slot combo box | `ActionEvent` / `ItemEvent` | `ActionListener` / `ItemListener` | Store selected slot |
| Book button | `ActionEvent` | `ActionListener` | Validate all inputs and create appointment |
| Window close | `WindowEvent` | `WindowListener`/`WindowAdapter` | Ask for confirmation if booking is in progress |

### 2.2 Component hierarchy

```text
JFrame AppointmentWindow
 ├── JPanel formPanel (BorderLayout)
 │   ├── JLabel patientIdLabel
 │   ├── JTextField patientIdField
 │   ├── JLabel departmentLabel
 │   ├── JComboBox<String> departmentCombo
 │   ├── JLabel doctorLabel
 │   ├── JComboBox<String> doctorCombo
 │   ├── JLabel timeLabel
 │   ├── JComboBox<String> slotCombo
 │   └── JButton bookButton
 └── JPanel statusPanel (BorderLayout)
     ├── JLabel statusLabel
     └── JButton closeButton
```

### 2.3 Handler pseudocode

#### (a) Refresh doctor list when department changes

```text
procedure onDepartmentChanged(selectedDept)
    doctorMap <- loadedDoctorsByDepartment
    doctors <- doctorMap.get(selectedDept)
    if doctors is null then
        clear doctor combo
        show error message
    else
        doctorCombo.removeAllItems()
        for each doctor in doctors
            doctorCombo.addItem(doctor)
        end for
        doctorCombo.setSelectedIndex(0)
    end if
end procedure
```

#### (b) Validate and book appointment

```text
procedure onBookPress()
    patientId <- patientIdField.getText().trim()
    department <- departmentCombo.getSelectedItem()
    doctor <- doctorCombo.getSelectedItem()
    slot <- slotCombo.getSelectedItem()

    if patientId is empty then
        show error
        return
    end if

    if department is null or doctor is null or slot is null then
        show error
        return
    end if

    if appointment already exists for same patient and same slot then
        show duplicate error
        return
    end if

    create appointment record
    save to database
    show confirmation
end procedure
```

### 2.4 Event trace

When the window opens, the constructor creates the components and registers listeners. The `windowOpened` event runs after the frame is visible, and it can preload the Department list and initialize fields. The user selects a department, which raises `ItemEvent` and triggers the doctor list refresh. Selecting a time and clicking Book fires `ActionEvent`. The handler validates the data and then either creates the booking or shows an error. When the user closes the window, `windowClosing` runs, and if there is an active booking, it asks for confirmation before disposal.

This is similar to the applet lifecycle: `init` and `start` correspond to initialization and first use, while `paint` is for display; `stop` and `destroy` correspond to closing and cleanup, but the event model is more granular and response-driven.

### Alternative concept and complexity analysis

If the doctor list were fetched from the database on every department change, the work per change is O(m) for a department's doctor count and the database call itself adds expensive I/O. With a loaded `Map<String, List<Doctor>>`, the refresh becomes O(1) lookup plus O(m) to populate the combo box, and memory is O(d + total doctors) = O(d + m) for d departments and m doctors. Fetching each time is O(d × m) repeated work and less responsive; the in-memory map is much more efficient because it uses a single cached structure.

---

## Task 3 – Library Search Screen

### 3.1 Layout choices

- Outer frame: `BorderLayout` because the top search row, the centre results area, and the bottom button row are arranged in three principal regions.
- Search row panel: `FlowLayout` because the controls are naturally kept together and should not stretch vertically.
- Results area: `BorderLayout` with a large `JTextArea` or `JTable` in the centre so it can expand as the window is resized.
- Bottom row: `FlowLayout` because the buttons stay grouped and retain their preferred size.

### 3.2 Nested panel design

```text
JFrame LibraryWindow
 └── JPanel rootPanel (BorderLayout)
     ├── JPanel searchPanel (FlowLayout)
     │   ├── JTextField searchText
     │   ├── Choice category
     │   └── JButton searchButton
     ├── JScrollPane resultsScroll (BorderLayout.CENTER)
     │   └── JTextArea resultsArea
     └── JPanel actionPanel (FlowLayout)
         ├── JButton prev
         ├── JButton next
         └── JButton reserve
```

### 3.3 Implementation

A working Java implementation is provided in `Task03/LibrarySearchScreen.java`.

### 3.4 Resizing behaviour

The search row and button row remain compact while the results area expands, matching the requirement. The results region is the only component that grows when the window is resized.

### Alternative concept and complexity analysis

Using `GridBagLayout` or `setLayout(null)` makes the screen more fragile and harder to maintain. `GridBagLayout` performs layout computation proportional to the number of components, usually O(n), while nested layout panels keep the logic simpler and the cost low. Absolute positioning uses no automatic recalculation, but the size and position must be manually managed for every resize. The nested design also creates fewer style-dependent code paths and a more robust container structure.

---

## Task 4 – Event Handling Forensics

### 4.1 Defect location

The original code uses `mouseclicked` instead of `mouseClicked`, so the method is never called. Since `mouseClicked` is the correct callback, the event is ignored. The label is also never updated because the increment logic is inside the uncalled event method.

### 4.2 Failing interaction

The failing interaction is: click once, then minimise and restore the window. The user then sees the dots disappear because they were drawn directly in the mouse handler using `getGraphics()` instead of being stored and redrawn by `paint()`. The graphics context is temporary and is invalidated on repaint.

### 4.3 Repair

The corrected program stores each click point in a list and redraws all points in `paint(Graphics)`. The label is updated in `mouseClicked` and the repaint is requested after each click.

### 4.4 Prevent recurrence

Using `@Override` on the event callback causes a compile-time error when the method signature is wrong, such as `mouseclicked` instead of `mouseClicked`. This prevents the bug from reaching runtime.

The implementation is provided in `Task04/DrawPad.java`.

---

## Task 5 – Same Screen, Different Event Handling Styles

### 5.1 Four implementations

The converter can be implemented with:

1. the frame itself implementing `ActionListener`
2. a named inner class
3. an anonymous inner class
4. a lambda expression

Each implementation uses the same convert logic but differs in how the handler is attached.

### 5.2 Requirement change

After adding Enter-in-field support and dialog-box output, the lambda version required minimal change because one listener can handle both the button and the field key event. The remaining styles also support the change, but the amount of code changed is smallest with the lambda version and larger with the frame and named-inner versions because they require extra event registration or method updates.

### 5.3 Comparison

| Style | Readability | Access to fields | Reuse | Ease of change |
|---|---|---|---|---|
| Frame as listener | Fair | Direct | Low | Moderate |
| Named inner class | Good | Direct | Medium | Good |
| Anonymous inner class | Good but noisy | Direct | Low | Moderate |
| Lambda | Very clear | Direct | High | Best |

For a large application, a lambda or a named handler object is preferable because it keeps the UI code readable while making cross-trigger logic easy to centralize.

The lambda version is implemented in `Task05/Converter.java`.

---

## Task 6 – Event Registration Desktop Application

### Stage 1 – Interpret the problem

Users:
- Student users register for events
- Committee members view and filter registrations
- Administrator is expected to export or inspect the data list

Screens:
- Registration form
- Registration table view
- Filter panel
- Menu bar with export action

Data:
- studentName
- studentId
- eventName
- registrationTime

Rules:
- A student cannot register twice for the same event.
- Invalid input is rejected clearly.
- Database access must be off the event-dispatch thread.
- Filtering and export should remain responsive.

### Stage 2 – Screen flow

```text
MainWindow
 ├── MenuBar
 │   ├── File > Export CSV
 │   └── Help > About
 ├── RegistrationForm Panel
 │   ├── Student ID TextField
 │   ├── Student Name TextField
 │   ├── Event ComboBox
 │   └── Register Button
 └── Table Panel
     ├── Filter ComboBox
     └── JTable registrations
```

### Stage 3 – Critical design decisions

- Input validation occurs in the service layer before the data is sent to the repository.
- Duplicate checking is in the service or DAO, based on the pair `(studentId, eventName)`.
- Database work is done in a background `SwingWorker` so the UI stays responsive.
- The event-dispatch thread only updates the table and label after the background job completes.

### Stage 4 – Class design and pseudocode

```text
EventRegistrationApp (Swing UI)
  - form, table, filter combo, status label
  + actionRegister()
  + actionFilter()
  + refreshTableAsync()

EventRegistrationService
  + registerStudent(String studentId, String name, String event)
  + loadFiltered(String event)

RegistrationDAO
  + insert(Registration)
  + findAll(String event)
  + exists(String studentId, String event)
```

#### Register pseudocode

```text
procedure registerStudent(studentId, studentName, event)
    if studentId blank or name blank then
        show invalid input
        return
    end if

    if exists(studentId, event) then
        show duplicate error
        return
    end if

    save record to database
    refresh table
    show confirmation
end procedure
```

#### Filter pseudocode

```text
procedure loadFiltered(event)
    if event is "All" then
        load all registrations
    else
        load registrations for that event only
    end if
    update table model
end procedure
```

### Stage 5 – Implementation

A Swing–JDBC desktop application is provided in `Task06/`.

The project includes a SQL script for the table definitions in `Task06/event_registration.sql`.

The code is deliberately designed for a MySQL database and includes a fallback in-memory repository so it can still be demonstrated when a database server is not present.

---

## Evidence summary

The repository contains a working Java implementation for the required GUI tasks:

- Task 1 repair: `Task01/FeeWindow.java`
- Task 3 layout demo: `Task03/LibrarySearchScreen.java`
- Task 4 fixing the event bug: `Task04/DrawPad.java`
- Task 5 lambda converter: `Task05/Converter.java`
- Task 6 Swing–JDBC app: `Task06/EventRegistrationApp.java`

The written analysis for Task 2 and design-defence tasks is included in this document.
