# Assignment 1 — Campus Smart-Parking System

**Student name:** [Enter your name]  
**Register number:** [Enter your register number]  
**GitHub link:** [CSA0925-1924260165-Assingment1](https://github.com/devnarayan729/CSA0925-1924260165-Assingment1)  
**Java:** 17

## Design assumptions

1. Floor and slot indices in Java are zero-based. A jagged array supports a different number of slots on every floor.
2. The standard hourly rate is Rs 10 for a two-wheeler and Rs 30 for a car. Every started hour is charged; zero minutes costs zero. The daily cap is ten hourly rates for each started 24-hour period.
3. Electric cars use the car parking rate. Charging costs Rs 12 per kWh, is charged separately from parking, and chargeable vehicles prefer a free charging slot.
4. An e-bike is charged a flat Rs 25 parking fee for any positive stay, plus the same optional charging fee. An e-bike with a zero-minute stay has no parking charge.
5. Delivery-van billed hours whose start time is from 09:00 inclusive to 17:00 exclusive cost twice the car rate. Daily cap is twice ten car hourly rates per started 24-hour period.
6. A valid monthly or one-day pass waives the parking component for the entire stay only when valid at both entry and exit. It does not waive EV charging energy costs.
7. Ticket numbers are globally unique within one running JVM. Persisting them across application restarts would require a database or durable counter.
8. The implementation uses deterministic registration-based slot search and does not model reservations, payment failures, time zones, or concurrent processes.

## Task 1 — Understand the problem and identify objects

The inputs are a vehicle registration/category, entry and exit timestamps, optional pass information, per-floor slot counts, charging-slot positions, and (for an EV) consumed kWh. Outputs are a ticket with a unique number and allocated floor/slot, a release result/receipt, and the parking plus charging fee.

| Candidate object | Why it is needed / responsibility |
|---|---|
| `Vehicle` and concrete vehicle classes | Own a validated registration identity and expose the appropriate rate/behavior. New vehicle categories do not require changing slot allocation. |
| `ParkingSlot` | Encapsulates a floor/number, charging capability, and the single active ticket; its occupy/release operations prevent double use. |
| `ParkingTicket` | Connects a vehicle, slot, entry time, pass, and unique ticket identifier for the full stay. |
| `ParkingPass` | Represents a time-limited parking entitlement without coupling the facility to a specific pass kind. |
| `ParkingFacility` | Owns the jagged floor/slot structure and the active-registration lookup; accepts entries and settles exits. |
| `ParkingFeePolicy` | Separates rate rules from the vehicle and facility lifecycle. Standard and van rules can be changed independently. |
| `ParkingReceipt` | Carries the closed ticket, exit time, and final fee back to the caller. |

Nouns such as vehicle, ticket, slot, pass, and facility suggest the domain types. Verbs such as `enter`, `allocate`, `occupy`, `calculateFee`, `exit`, and `release` become operations.

Business invariants: a slot has at most one active ticket; each registration has at most one active ticket; a ticket number is never reused in the running process; exit cannot precede entry; fees cannot use negative duration or kWh; a pass only applies during its validity; and invalid slot configurations or unknown categories are reported rather than silently accepted. The active-ticket `HashMap` supports direct registration lookup.

**Alternative — parallel arrays:** `regNo[]`, `type[]`, and `entryTime[]` require a linear scan, O(n), to find a registration; each added attribute requires another array and synchronized index updates. Objects also require O(n) lookup without an index, but centralize related attributes and behavior, so adding an attribute normally changes one type. Per vehicle, both approaches use O(1) storage for a fixed number of fields; objects have reference/object overhead, while parallel arrays need one reference/value slot per field.

## Task 2 — Choose the data representation

| Value | Representation | Reason |
|---|---|---|
| Floor and slot index | `int` | Array indices are integral and indices are bounded by configured sizes. |
| Registration and ticket number | `String` | Registration numbers may contain letters/leading zeroes; ticket IDs are formatted identifiers. |
| Entry and exit | `LocalDateTime` | Avoids manually splitting calendar/time fields and supports elapsed-duration calculations. |
| Duration | `Duration` / integer seconds | Retains exact elapsed seconds; started-hour rounding is then explicit. |
| Fee | `double` in this educational implementation | Supports the provided output format. A production billing system should use `BigDecimal` or integer cents. |
| Floors and slots | `ParkingSlot[][]` jagged array | Directly models unequal floor capacities and keeps floor/slot traversal natural. |

The service reads structured Java values through its API; a command-line UI could use `Scanner` and convert validated input to `LocalDateTime`/vehicle objects. Do not truncate before rounding: `61 / 60` with integer division gives 1 and undercharges a two-hour stay. The implementation uses `(minutes + 59L) / 60L`; 61 minutes therefore bills 2 hours. Casting `minutes / 60` to `double` after division does not restore lost precision.

**Alternative structures:** a flat array is compact, but `floor * slotsPerFloor + slot` assumes a uniform slot count. With uneven floors, an offsets/prefix-sum table or a search over floor lengths is needed. A jagged array uses O(S) slot storage and direct O(1) access by known coordinates; scanning for the first free slot is O(S), where S is total capacity. `ArrayList<String[]>` has the same uneven-floor flexibility and O(S) storage with small list overhead, but indexed lookup remains O(1) and first-free search remains O(S).

## Task 3 — Design the control logic

**Pseudocode (allocation):**

```text
BEGIN ALLOCATE(vehicle)
    IF vehicle is null OR registration is blank THEN
        REPORT invalid vehicle
    END IF
    IF registration already has an active ticket THEN
        REPORT duplicate registration
    END IF
    IF vehicle can charge THEN
        FOR EACH floor, then EACH slot
            IF slot is free AND slot supports charging THEN
                RETURN slot
            END IF
        END FOR
    END IF
    FOR EACH floor, then EACH slot
        IF slot is free AND slot is suitable THEN
            RETURN slot
        END IF
    END FOR
    REPORT facility full
END ALLOCATE
```

**Pseudocode (fee and exit):**

```text
BEGIN EXIT(registration, exitTime, energy)
    FIND active ticket by registration
    IF no ticket OR exitTime is before entry OR energy is negative THEN
        REPORT invalid exit
    END IF
    seconds := elapsed seconds
    billableHours := (seconds + 3599) DIV 3600
    IF pass is valid at entry AND exit THEN
        parkingFee := 0
    ELSE
        parkingFee := vehicle-specific policy(billableHours, entry, exit)
    END IF
    IF vehicle is chargeable THEN
        totalFee := parkingFee + energy * 12
    ELSE
        totalFee := parkingFee
    END IF
    RELEASE ticket's slot and remove active ticket
    RETURN receipt
END EXIT
```

The nested loops stop immediately on the first suitable slot (`return`). A full scan with no match reports an explicit error. The fee policy uses integer ceiling arithmetic, per-policy decisions, and `Math.min` for the daily cap. `continue` is unnecessary because the eligibility conditions can be expressed in one `if`; in a version that checked multiple constraints separately, it could skip an unsuitable slot. `break` could leave an inner loop, but `return` is simpler because allocation is complete as soon as a match is found.

`Task03/ParkingFacility.java` completes the supplied code without changing its `main`. Search and release are O(S) and O(1), respectively. A stack of available slots per floor and free-count per floor could make allocation/release O(1) amortized when a candidate floor is known, but requires O(S) extra slot references plus O(F) counts and correct handling of suitability/charging-slot priority. The current simple scan uses O(S) slot storage and no auxiliary free-slot structure.

## Task 4 — Classes, objects, and constructors

All domain fields are initialized in parameterized constructors and are private/final where their identity should not change. `Vehicle` validates/normalizes its registration before a subclass is constructed; subclass constructors call `super(regNo)`. `ParkingSlot` validates coordinates, and `ParkingTicket` requires its vehicle, slot, and entry time. `this` distinguishes fields from constructor parameters; delegating constructors are used by `ParkingFacility(int[])` to provide a default no-charger configuration.

A default constructor is inappropriate for a valid vehicle/ticket because required values would be missing. Setters would permit temporarily invalid states; factory methods could be useful for input parsing but do not improve the invariant guarantees by themselves. A ticket copy constructor is deliberately omitted: copying a ticket could duplicate identity and confuse the active-slot invariant. A reissue should be a separate audited operation with a new ticket number.

Each local/reference variable holds a reference on the stack; objects and arrays reside on the heap. Constructing n tickets takes O(n) time and O(n) heap space regardless of constructor style. Constructor validation adds O(1) work per ticket. Setter initialization also takes O(1) per field but permits partial state; static factories still allocate one object per ticket.

## Task 5 — Static members and object life cycle

`ParkingTicket.NEXT_NUMBER` is a static `AtomicLong`: one counter shared by all facilities prevents duplicate ticket IDs in a JVM and safely increments across threads. An instance counter would create k counters for k facilities and could issue duplicates (each facility might begin at 1). The global counter occupies O(1) state; k facility-local counters occupy O(k). Neither counter's storage grows with n tickets, though ticket objects themselves use O(n) space.

The active-ticket map and slots are instance state because each facility has different occupancy. A static method is used for factory creation and test helpers; a static initialization block is unnecessary. The entry method receives a vehicle/pass reference and returns a ticket reference. On successful exit, the map and slot release their references; once caller references are also gone, the ticket/vehicle can be garbage-collected. Java does not guarantee when collection occurs.

## Task 6 — Inheritance hierarchy

```text
Vehicle (abstract)
├── TwoWheeler
│   └── EBike implements Chargeable
└── Car
    ├── ElectricCar implements Chargeable
    └── DeliveryVan

ParkingPass (interface)
├── MonthlyPass
└── OneDayPass
```

`Vehicle` owns registration identity and common fee entry points. `TwoWheeler` and `Car` provide rates. `ElectricCar` reuses car behavior and adds charging capability. `EBike` reuses two-wheeler identity/rate data while specializing the flat parking fee. `DeliveryVan` reuses car identity/rate and selects its time-window fee policy. Passes use composition with a ticket rather than inheritance from a user; the brief does not require user identity records.

For `new ElectricCar("TN03C3")`, Java executes `Vehicle(String)` first (normalizes/validates registration), then `Car(String)` (calls `super`), then `ElectricCar(String)`; instance field initializers run along their class's construction stage. `super(regNo)` is required because the superclass has no no-argument constructor; overridden methods provide subtype rates/policies.

The subclasses use `super(regNo)` to initialize inherited state. The registration field is private, so subclasses read it through `getRegNo()` rather than `super.regNo`; `super.method()` would be used when a subclass needs the superclass implementation before adding behavior. This keeps superclass state encapsulated.

**Composition alternative:** a `Vehicle` containing a `VehicleType`/rate strategy can add a category by supplying a new strategy rather than adding a subclass. Both approaches calculate a fee in O(1) for a fixed number of slabs. Inheritance uses the object's existing class reference plus the registration; composition adds a policy reference (O(1) space per vehicle) but allows behavior to be changed/swapped independently. Time-dependent pricing is already delegated to policy objects in this implementation.

## Task 7 — Override and specialize behavior

Subclasses override `ratePerHour()`; `EBike` overrides `calculateFee(int)` for its flat fee and `DeliveryVan` selects a distinct `ParkingFeePolicy` rather than duplicating facility logic. Every override has `@Override`; an override must retain the method signature, use a compatible covariant return type, and not reduce access. `Vehicle` identity, normalized registration, and its `equals`, `hashCode`, and `toString` are final to keep identity consistent. `ParkingTicket` is a final class because ticket identity/lifecycle must not be specialized by subclasses; immutable fields are final.

Equality uses registration number only, so `new Car("TN02B2").equals(new ElectricCar("TN02B2"))` is `true`. The demo's expected result is therefore correct for this system's vehicle identity: one registration denotes one vehicle regardless of classification. The facility separately rejects two active tickets for that registration. If registration numbers were not globally unique across categories, equality would need to include category.

A switch on a type field would make each fee calculation O(k) in the worst case for k categories and would centralize branching. Every new type could force edits to the switch and its callers. Virtual dispatch is O(1) for selecting an override and lets a new subclass supply its behavior without rewriting the existing fee callers.

## Task 8 — Polymorphism

`Vehicle.calculateFee(int)` and the `LocalTime`/`LocalDateTime` overloads demonstrate compile-time overloading: the compiler chooses by argument types. A `Vehicle[]` can hold `TwoWheeler`, `Car`, and `ElectricCar` references through upcasting. Calling `calculateFee(130)` dispatches to the implementation for the actual object (e.g. the e-bike flat-fee override); an `ElectricCar` is chargeable through the `Chargeable` interface.

Trace: declared reference type `Vehicle`; actual object type `ElectricCar`; method call `v.calculateFee(130)`; runtime dispatch selects the inherited car-rate behavior (three billable hours × Rs 30 = Rs 90); interface dispatch selects `chargingFee(10)` for Rs 120; total Rs 210. The same call on a `TwoWheeler` uses its Rs 10 rate and totals Rs 30.

`Task08/FeeDemo.java` keeps the supplied `main` and produces the specified output:

```text
TwoWheeler TN01A1 -> Rs 30.0
Car TN02B2 -> Rs 90.0
ElectricCar TN03C3 -> Rs 210.0
true
```

The alternative `instanceof` chain tests up to k types per call (O(k)), grows with every type, and needs edits at central dispatch sites. Dynamic dispatch has constant-time selection at the abstraction level and avoids that chain.

## Task 9 — Abstraction

`Vehicle` is abstract because a generic vehicle has no concrete hourly rate. The interface `Chargeable` represents a capability that only some vehicles have; it allows a vehicle to inherit from its natural class and implement additional behavior. It supplies a default charging-fee method for the common Rs 12/kWh rule, which a future exceptional charger can override. `ParkingPass` likewise expresses pass validity independently of pass representation. The `ParkingFeePolicy` interface separates the fee algorithm from facility operations and supports a different delivery-van policy.

If `Vehicle` were an interface and `Chargeable` an abstract class, vehicle state (`regNo`), validated constructors, and shared identity implementation could not live in the interface as instance state. A class can implement multiple interfaces but extend only one class; making charging an abstract class would prevent an EV from extending `Car` as well. Method dispatch cost remains O(1) in either design; changing interfaces/classes does not materially change per-vehicle asymptotic space.

## Task 10 — Packages and encapsulation

Package structure: `parking.model` contains vehicles, slots, tickets, passes, and receipts; `parking.pricing` contains capabilities and fee strategies; `parking.service` contains facility operations; `parking.app` contains executable examples/tests. State is private; immutable identifiers and rates are exposed via validated constructors/getters. Only controlled slot operations mutate occupancy, and they reject invalid transitions. `ParkingFacility` synchronizes entry/exit mutations and rejects duplicate registrations, invalid times, unknown vehicle categories, and out-of-range charging-slot positions.

No mutable setters are needed for immutable identity/configuration. The `VehicleFactory` uses `String` type names and rejects unknown values. Primitive `int` indices avoid wrapper nulls; `HashMap` handles registration-key equality. If boxed `Integer` values were used, compare values with `.equals()`/`Objects.equals()`, not `==`, because reference identity and caching make `==` unreliable.

Making fields public would let a caller bypass checks, create two occupants for one slot, or mutate ticket identity; validation would then be repeated at every call site. A central validation costs O(1) per update and is implemented once.

## Task 11 — Implementation, testing, and defence

Implementation files are under `Task11/src/parking/`. `ParkingFacility` uses a jagged array for physical slots and a `HashMap<String, ParkingTicket>` for active registrations. Ticket lookup/insert/remove is expected O(1); first-free allocation is O(S); exiting after lookup is O(1) plus the fee policy. The object-per-slot plus map storage is O(S+n), where S is facility capacity and n is active vehicles.

Run `parking.app.ParkingSystemTest` to execute the following expected/actual checks. Each listed actual result is the asserted result from the passing test runner.

| Case | Expected | Actual |
|---|---|---|
| Car stays exactly 60 minutes | Rs 30 | PASS |
| Two-wheeler stays 61 minutes | Rs 20 | PASS |
| Zero-minute stay | Rs 0 | PASS |
| Allocate last slot, then attempt one more entry | Last slot allocated; next entry rejected | PASS |
| Duplicate active registration | Rejected | PASS |
| Unknown type from factory | Rejected | PASS |
| Exit before entry | Rejected; ticket remains active | PASS |
| Out-of-range charging-slot index | Rejected | PASS |
| Valid monthly and one-day passes | Parking fee waived | PASS |
| EV has a one-day pass and uses 1 kWh | Parking waived; Rs 12 energy charge remains | PASS |
| EV uses charging slot; 60 min + 2 kWh | Rs 54 (30 + 24) | PASS |
| E-bike stays 61 min + 1 kWh | Rs 37 (25 + 12) | PASS |
| Delivery van from 09:00 to 10:00 | Rs 60 | PASS |
| Car stays 11 hours | Rs 300 daily cap | PASS |
| Released slot reused; missing ticket rejected | Both behaviors hold | PASS |

The hand calculation for the `ParkingDemo` day: car (60 minutes) Rs 30; EV (61 minutes, 3 kWh) Rs 60 parking + Rs 36 charging = Rs 96; monthly-pass student Rs 0; one-day-pass visitor Rs 0; van 08:00–10:00 (one off-peak + one peak hour) Rs 30 + Rs 60 = Rs 90. Total = **Rs 216**, matching the printed receipt fees. The run is saved in `Task11/demo-output.txt`. The e-bike, pass, new-vehicle, error, and capacity boundaries are independently covered by the test runner.

The design demonstrates encapsulation, inheritance, runtime polymorphism, interfaces, and interchangeable fee policies. One limitation is that this educational system uses in-memory tickets and `double` currency values; a production system should persist transactions and use integer minor currency units/`BigDecimal`. Active-ticket search via `HashMap` is expected O(1), versus O(n) in an array; both store O(n) tickets, with a map's bucket/reference overhead.

## Task 12 — Re-evaluate and adapt

**Impact:** The changed brief adds e-bikes, delivery vans with a 09:00–17:00 premium, one-day visitor passes, and five uneven floors. Unequal floor sizes require a jagged array rather than a fixed rectangular layout. These new needs affect vehicle behavior, one fee strategy, pass types, and the construction/input layer; slot allocation must continue to be type-agnostic and favor charging points for chargeable vehicles.

**Evaluation/decision — modify, retaining the core design.** The `Vehicle` abstraction, `ParkingPass` interface, jagged slots, `ParkingFacility` entry/exit workflow, and standard fee policy remain reusable. The implementation adds `EBike`, `DeliveryVan`, `OneDayPass`, and `DeliveryVanFeePolicy`, and extends `VehicleFactory` to recognize the two new categories. The facility itself and previously tested vehicle classes do not need category-specific branches. Existing `MonthlyPass` remains unchanged. One existing creation class (`VehicleFactory`) changes to register the new input names; a future type can be added as a subclass/policy, though it must also be registered in this simple factory.

The e-bike has a flat parking fee; the delivery van uses a separate policy that doubles billed hours beginning during the peak window; the one-day pass uses the existing validity abstraction. Test evidence is in Task 11: the e-bike case returns Rs 37, the van peak-hour case returns Rs 60, the one-day pass waives parking, and the five-floor jagged facility is instantiated in `ParkingDemo`. More granular production billing could prorate a billed hour that only partially overlaps the peak period; this assignment uses a documented “billed-hour start time” rule for deterministic results.

This is a justified modification rather than a rewrite: new behavior is isolated in subclasses/policies and the shared facility workflow remains unchanged. The trade-off is an extra strategy type and a factory registration edit in exchange for preserving the tested allocation/exit code and keeping pricing rules out of the facility.
