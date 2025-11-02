# BlueStaq Elevator Control System

A Java-based elevator controller simulator that implements a basic LOOK scheduling algorithm for request handling. The LOOK scheudling alogrithm essentially means "hey I’m going to LOOK in one direction and keep moving that way, serving all requests that also want to go that direction. I’ll only turn around once there are no more requests in my current direction.”

## Overview

This project simulates a single-elevator system managing passenger requests within a building. The controller runs the elevator in simple time steps, like taking one “turn” at a time. Each step (called a tick) makes the elevator open or close its doors, move one floor, or stay idle.
This setup keeps the code easy to follow, with a clear split between:
- the physical state of the elevator (floor, doors, direction),
- how requests are stored and managed, and
- the decision-making logic that tells the elevator what to do next.

## Architecture

The system consists of five core components, each with a single, clear responsibility:

**1. Elevator.java** – Physical State Manager
- Tracks the elevator's current position (floor), direction of travel, and door status
- Manages door dwell time (how long doors remain open at each stop)
- Enforces building boundaries to prevent moving beyond minimum and maximum floors
- Provides the foundational state that all other components depend on

**2. Scheduler.java** – Intelligent Request Manager
- Maintains a prioritized queue of all passenger requests using efficient sorted data structures
- Distinguishes between two request types:
  - **Hall Calls**: Requests from passengers waiting on specific floors (UP or DOWN direction)
  - **Car Calls**: Requests from passengers inside the elevator (destination floors)
- Uses TreeSet for fast lookups—even with many requests, the system stays responsive

**3. Controller.java** – Decision Engine & Orchestrator
- Implements the core control logic that makes moment-to-moment decisions about elevator movement
- Manages the simulation loop that advances the system one discrete time step at a time
- Coordinates between the Elevator's physical state and the Scheduler's request queue
- Determines when to stop, which direction to move, and how to handle door operations

**4. Direction.java** – State Enumeration
- Defines the three possible elevator movement states: UP, DOWN, and IDLE
- Prevents invalid state transitions and improves code clarity

**5. Main.java** – Demo & Entry Point
- Provides a working example scenario demonstrating the complete system
- Simulates a realistic passenger journey: requests, pickup, and destination

## How It Works

The elevator operates using a **discrete time-step simulation model** (like a game engine). Each iteration represents one unit of time:

1. **Door Management** – If doors are currently open, count down the dwell timer. When it expires, automatically close the doors and prepare for movement
2. **Stop Decision** – Analyze pending requests and determine if the elevator should stop at the current floor to pick up or drop off passengers
3. **Direction Selection** – If the elevator is idle, intelligently pick which direction to move based on where requests are waiting
4. **Movement Execution** – Move the elevator one floor in the selected direction. If no requests are ahead, but work remains behind, reverse direction and move
5. **Idle Check** – If all requests are fulfilled and the elevator is stationary, enter idle mode and await new passenger requests

## Scheduling Algorithm

The system uses a **LOOK scheduling variant**, an efficient algorithm used in real-world elevator systems:

- **Directional Service** – Services all requests in the current direction of travel before reversing direction
- **Efficient Direction Changes** – Only reverses direction when there are no more requests ahead, but work remains behind. This eliminates wasted trips to empty floors
- **Fair Prioritization** – Treats all requests equally; requests in the current direction are always served before requests in the opposite direction
- **No Unnecessary Stops** – The elevator never visits a floor unless a passenger requested it, improving energy efficiency and response time

This approach balances fairness (all passengers are served), efficiency (minimal empty floor visits), and responsiveness (nearby requests are handled quickly).

## Key Assumptions

1. **Single Elevator System**: Only one elevator is simulated; multi-elevator coordination not implemented
2. **Discrete Time Steps**: The elevator moves in simple discrete “ticks.” Each tick is not actually time based but instead represents one small step where it can move one floor, open doors, or stay idle.
3. **No Network Communication**: No REST APIs or server simulation—this is a local, in-memory system suitable for understanding control logic
4. **Instant Floor Transitions**: The elevator instantly moves one floor per tick; acceleration and slowing down aren’t modeled.
5. **Fixed Building Bounds**: The number of floors is set when the simulation starts (default: 0–10) and can’t be changed later.
6. **Synchronous Request Submission**: Requests are submitted directly via method calls; no async queue or event system
7. **No Capacity Constraints**: Elevator capacity and weight limits are not enforced
8. **Predictable Behavior**: The system behaves the same way every time with the same inputs (no randomness).
9. **The Elevator is Built Nicely**: The elevator never breaks down; there’s no simulation of mechanical faults or errors.

## Features Not Implemented

- **Multi-Elevator Systems** – No coordination between multiple elevators.  
- **Network/API Interface** – No REST endpoints or distributed system support.  
- **Real-Time Simulation** – No actual time delays; all actions happen instantly each tick.  
- **Persistence** – No database or saved state between runs.  
- **Advanced Scheduling** – No machine learning, prediction, or traffic optimization algorithms.  
- **Passenger Tracking** – The system doesn’t identify or track individual passengers.  
- **Capacity Management** – The elevator has no weight or occupancy limits.  
- **Error Handling** – No fault tolerance for breakdowns, sensors, or system failures.  
- **Load Balancing** – Only one elevator; no load distribution logic.  
- **Accessibility Features** – No ADA or priority-access support yet.

## Running the Demo

```bash
javac *.java
java Main
```

The demo creates a scenario with hall calls at floors 3 (UP) and 8 (DOWN), then a passenger boarding at floor 3 requesting floor 9. It prints the system state at each tick until the elevator becomes idle or reaches 30 ticks.

## Future Enhancements

To extend this system for production use, consider:
1. Adding a REST API layer for external request submission
2. Implementing multi-elevator support with intelligent scheduling
3. Adding real-time simulation with configurable floor transition times
4. Integrating persistence for audit logs and statistics
5. Implementing capacity and safety constraints
6. Adding predictive algorithms based on traffic patterns (more practical if we use more than 1 elevator for bigger buildings)
7. Creating a UI for visualization, monitoring, and testing new changes or additions
