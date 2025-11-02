# BlueStaq Elevator Control System

A Java-based elevator controller simulator that implements a LOOK scheduling algorithm for efficient request handling.

## Overview

This project simulates a single-elevator system managing passenger requests within a building. The controller uses a discrete time-stepping approach to drive the system, with clean separation between physical elevator state, request scheduling, and control logic.

## Architecture

The system consists of five core components:

- **Elevator.java**: Maintains physical state (current floor, direction, door status, door dwell time)
- **Scheduler.java**: Manages three types of requests using TreeSet for efficient sorted lookup
  - Hall calls (UP and DOWN buttons on each floor)
  - Car calls (destination buttons inside the elevator)
- **Controller.java**: Implements the main control logic and simulation loop
- **Direction.java**: Enum for elevator movement states (UP, DOWN, IDLE)
- **Main.java**: Demo entry point with example scenario

## How It Works

The controller operates in discrete time steps (ticks):

1. **Door Management**: Counts down dwell time; closes doors when timer expires
2. **Stop Logic**: Checks if elevator should stop at current floor
3. **Idle Handling**: Picks an initial direction when idle
4. **Movement**: Moves one floor up/down if requests exist ahead; reverses if requests exist behind
5. **State Check**: Goes idle when no more requests remain

## Scheduling Algorithm

Uses a **LOOK variant** algorithm:
- Services requests in the current direction
- Reverses direction only when no work ahead but requests exist behind
- Prioritizes requests in the current direction of travel
- Efficiently handles direction changes without visiting empty floors

## Key Assumptions

1. **Single Elevator System**: Only one elevator is simulated; multi-elevator coordination not implemented
2. **Discrete Time Steps**: Elevator movement is simulated in discrete ticks (not real-time); each tick represents one floor of movement
3. **No Network Communication**: No REST APIs or server simulation—this is a local, in-memory system suitable for understanding control logic
4. **Instant Floor Transitions**: The elevator moves one floor per tick; acceleration/deceleration not modeled
5. **Fixed Building Bounds**: Building floors are fixed at initialization (default 0-10); dynamic floor addition not supported
6. **Synchronous Request Submission**: Requests are submitted directly via method calls; no async queue or event system
7. **No Capacity Constraints**: Elevator capacity and weight limits are not enforced
8. **Deterministic Behavior**: No randomness or concurrency; the system is fully deterministic given the same input sequence

## Features Not Implemented

- **Multi-Elevator Systems**: No coordination between multiple elevators
- **Network/API Interface**: No REST endpoints or distributed system support
- **Real-Time Simulation**: No actual time delays; all operations are instantaneous per tick
- **Persistence**: No database or state persistence across runs
- **Advanced Scheduling**: No machine learning, predictive algorithms, or traffic flow optimization
- **Passenger Tracking**: No identification or tracking of individual passengers
- **Capacity Management**: Elevator weight/occupancy limits not enforced
- **Error Handling**: No fault tolerance for elevator breakdowns or sensor failures
- **Load Balancing**: Single elevator only; no load distribution logic
- **Accessibility Features**: No special handling for ADA requirements or priority queues

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
6. Adding predictive algorithms based on traffic patterns
7. Creating a UI for visualization and monitoring
