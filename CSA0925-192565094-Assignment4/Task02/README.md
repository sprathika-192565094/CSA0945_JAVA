# Task 2 – Hospital Outpatient Appointment Desk

## Event model

| Event source | Event object | Listener | Action |
|---|---|---|---|
| Department combo box | `ItemEvent` | `ItemListener` | Refresh doctor list |
| Patient ID field | `ActionEvent` | `ActionListener` | Validate patient ID |
| Doctor list | `ItemEvent` | `ItemListener` | Update selected doctor |
| Time slot combo box | `ItemEvent` | `ItemListener` | Capture selected slot |
| Book button | `ActionEvent` | `ActionListener` | Validate and book appointment |
| Window close | `WindowEvent` | `WindowAdapter` | Confirm before closing if booking active |

## Component hierarchy

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

## Handler pseudocode

```text
onDepartmentChanged(selectedDept):
    doctors = doctorMap.get(selectedDept)
    if doctors is null then
        clear doctor selection and show error
    else
        populate doctorCombo
        select first doctor
    end if
```

```text
onBookPressed():
    validate patientId, department, doctor, and slot
    if duplicate appointment exists then
        show duplicate message
    else
        create record
        save to database
        show confirmation
    end if
```

## Event trace

1. The frame constructor creates components and registers listeners.
2. When the frame becomes visible, the window is opened and the department list is loaded.
3. Selecting a department triggers the doctor refresh.
4. Clicking Book runs the validation and booking logic.
5. Close request triggers `windowClosing`, which asks for confirmation if a booking is in progress.

The event-driven lifecycle is more granular than the applet lifecycle (`init`, `start`, `paint`, `stop`, `destroy`), but the same principle applies: initialization at startup, display during active use, and cleanup on close.
