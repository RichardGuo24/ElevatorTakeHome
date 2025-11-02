/**
 * Comprehensive test suite for the elevator system.
 * Tests core functionality, edge cases, and scheduling logic.
 */
public class ElevatorTest {

    // Test counters
    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        // Initialization & Validation Tests
        testElevatorInitialization();
        testSchedulerValidation();

        // Basic Movement Tests
        testSimpleUpMovement();
        testSimpleDownMovement();
        testIdleBehavior();

        // Door Logic Tests
        testDoorOpeningAndClosing();
        testDoorDwellTime();

        // Hall Call Tests
        testHallCallUpDirection();
        testHallCallDownDirection();
        testHallCallMultipleFloors();

        // Car Call Tests
        testCarCallSingleFloor();
        testCarCallMultipleFloors();

        // Direction Reversal Tests
        testDirectionReversalWhenNoWorkAhead();
        testNoReversalWhenWorkAhead();
        testIdlePicksDirectionUp();
        testIdlePicksDirectionDown();

        // Complex Scenarios
        testMixedHallAndCarCalls();
        testRequestsClearedAtFloor();
        testComplexScenarioWithMultipleRequests();

        // Edge Cases
        testTopFloorBoundary();
        testBottomFloorBoundary();
        testDuplicateHallCalls();
        testDuplicateCarCalls();
        testOutOfBoundsCalls();

        // Print Summary
        System.out.println("\n" + "=".repeat(60));
        System.out.printf("Tests Run: %d%n", testsRun);
        System.out.printf("Tests Passed: %d%n", testsPassed);
        System.out.printf("Tests Failed: %d%n", testsRun - testsPassed);
        System.out.println("=".repeat(60));
    }

    // ============ Initialization & Validation ============

    private static void testElevatorInitialization() {
        test("Elevator initializes at correct floor", () -> {
            Elevator e = new Elevator(0, 10, 5);
            assert e.currentFloor == 5 : "Expected floor 5, got " + e.currentFloor;
            assert e.direction == Direction.IDLE : "Expected IDLE, got " + e.direction;
            assert !e.doorOpen : "Expected doors closed";
        });
    }

    private static void testSchedulerValidation() {
        test("Scheduler rejects out-of-bounds floor", () -> {
            Scheduler s = new Scheduler(0, 10);
            Controller c = new Controller(new Elevator(0, 10, 0), s, 1);
            try {
                c.submitHallCall(11, Direction.UP);
                assert false : "Should have thrown exception for floor 11";
            } catch (IllegalArgumentException e) {
                // Expected
            }
        });
    }

    // ============ Basic Movement Tests ============

    private static void testSimpleUpMovement() {
        test("Elevator moves UP when car call is above", () -> {
            Elevator e = new Elevator(0, 10, 0);
            Scheduler s = new Scheduler(0, 10);
            Controller c = new Controller(e, s, 1);

            c.submitCarCall(3);

            // Tick to move up
            c.tick(); // door management
            c.tick(); // decide direction
            assert e.currentFloor == 1 : "Expected floor 1, got " + e.currentFloor;
            assert e.direction == Direction.UP : "Expected UP, got " + e.direction;
        });
    }

    private static void testSimpleDownMovement() {
        test("Elevator moves DOWN when car call is below", () -> {
            Elevator e = new Elevator(0, 10, 5);
            Scheduler s = new Scheduler(0, 10);
            Controller c = new Controller(e, s, 1);

            c.submitCarCall(2);

            // Ticks to set direction and move
            c.tick(); // idle handling
            c.tick(); // move down
            assert e.currentFloor == 4 : "Expected floor 4, got " + e.currentFloor;
            assert e.direction == Direction.DOWN : "Expected DOWN, got " + e.direction;
        });
    }

    private static void testIdleBehavior() {
        test("Elevator stays IDLE with no requests", () -> {
            Elevator e = new Elevator(0, 10, 5);
            Scheduler s = new Scheduler(0, 10);
            Controller c = new Controller(e, s, 1);

            int initialFloor = e.currentFloor;
            c.tick();

            assert e.currentFloor == initialFloor : "Floor should not change when idle";
            assert e.direction == Direction.IDLE : "Should remain IDLE";
        });
    }

    // ============ Door Logic Tests ============

    private static void testDoorOpeningAndClosing() {
        test("Doors open and close at destination", () -> {
            Elevator e = new Elevator(0, 10, 0);
            Scheduler s = new Scheduler(0, 10);
            Controller c = new Controller(e, s, 1);

            c.submitCarCall(0);

            // Car call at current floor should trigger stop and door open
            c.tick();
            assert e.doorOpen : "Doors should be open at destination";

            // Next tick should decrement dwell
            c.tick();
            assert !e.doorOpen : "Doors should close after dwell time";
        });
    }

    private static void testDoorDwellTime() {
        test("Doors stay open for dwell time", () -> {
            Elevator e = new Elevator(0, 10, 0);
            Scheduler s = new Scheduler(0, 10);
            Controller c = new Controller(e, s, 3); // 3 tick dwell

            c.submitCarCall(0);

            c.tick(); // stop and open doors
            assert e.doorOpen && e.doorDwellTicks == 3 : "Expected dwell=3";

            c.tick(); // dwell countdown
            assert e.doorOpen && e.doorDwellTicks == 2 : "Expected dwell=2";

            c.tick();
            assert e.doorOpen && e.doorDwellTicks == 1 : "Expected dwell=1";

            c.tick();
            assert !e.doorOpen : "Doors should close after dwell expires";
        });
    }

    // ============ Hall Call Tests ============

    private static void testHallCallUpDirection() {
        test("Elevator stops for UP hall call when moving UP", () -> {
            Elevator e = new Elevator(0, 10, 0);
            Scheduler s = new Scheduler(0, 10);
            Controller c = new Controller(e, s, 1);

            c.submitHallCall(3, Direction.UP);

            // Move to floor 3
            for (int i = 0; i < 10; i++) {
                c.tick();
                if (e.currentFloor == 3 && e.doorOpen) break;
            }

            assert e.currentFloor == 3 : "Should stop at floor 3";
            assert e.doorOpen : "Doors should open at hall call";
        });
    }

    private static void testHallCallDownDirection() {
        test("Elevator stops for DOWN hall call when moving DOWN", () -> {
            Elevator e = new Elevator(0, 10, 5);
            Scheduler s = new Scheduler(0, 10);
            Controller c = new Controller(e, s, 1);

            c.submitHallCall(2, Direction.DOWN);

            // Move to floor 2
            for (int i = 0; i < 10; i++) {
                c.tick();
                if (e.currentFloor == 2 && e.doorOpen) break;
            }

            assert e.currentFloor == 2 : "Should stop at floor 2";
            assert e.doorOpen : "Doors should open at hall call";
        });
    }

    private static void testHallCallMultipleFloors() {
        test("Elevator services multiple hall calls in sequence", () -> {
            Elevator e = new Elevator(0, 10, 0);
            Scheduler s = new Scheduler(0, 10);
            Controller c = new Controller(e, s, 1);

            c.submitHallCall(2, Direction.UP);
            c.submitHallCall(5, Direction.UP);

            // Simulate until we reach floor 5
            for (int i = 0; i < 20; i++) {
                c.tick();
                if (e.currentFloor == 5 && e.doorOpen) break;
            }

            assert e.currentFloor == 5 : "Should reach floor 5";
            assert e.doorOpen : "Doors should be open";
        });
    }

    // ============ Car Call Tests ============

    private static void testCarCallSingleFloor() {
        test("Elevator reaches car call destination", () -> {
            Elevator e = new Elevator(0, 10, 0);
            Scheduler s = new Scheduler(0, 10);
            Controller c = new Controller(e, s, 1);

            c.submitCarCall(7);

            // Move to floor 7
            for (int i = 0; i < 15; i++) {
                c.tick();
                if (e.currentFloor == 7 && e.doorOpen) break;
            }

            assert e.currentFloor == 7 : "Should reach floor 7";
            assert e.doorOpen : "Doors should open";
        });
    }

    private static void testCarCallMultipleFloors() {
        test("Elevator services multiple car calls in order", () -> {
            Elevator e = new Elevator(0, 10, 0);
            Scheduler s = new Scheduler(0, 10);
            Controller c = new Controller(e, s, 1);

            c.submitCarCall(3);
            c.submitCarCall(7);

            // Reach floor 3
            for (int i = 0; i < 10; i++) {
                c.tick();
                if (e.currentFloor == 3 && e.doorOpen) break;
            }
            assert e.currentFloor == 3 : "Should stop at floor 3 first";

            // Continue to floor 7
            for (int i = 0; i < 15; i++) {
                c.tick();
                if (e.currentFloor == 7 && e.doorOpen && s.viewCarStops().isEmpty()) break;
            }
            assert e.currentFloor == 7 : "Should reach floor 7";
            assert s.viewCarStops().isEmpty() : "All car stops should be cleared";
        });
    }

    // ============ Direction Reversal Tests ============

    private static void testDirectionReversalWhenNoWorkAhead() {
        test("Elevator reverses direction when no work ahead", () -> {
            Elevator e = new Elevator(0, 10, 5);
            Scheduler s = new Scheduler(0, 10);
            Controller c = new Controller(e, s, 1);

            // Call above: go UP
            c.submitCarCall(8);
            // Call below: will require reversal
            c.submitCarCall(2);

            // Move up to floor 8
            for (int i = 0; i < 10; i++) {
                c.tick();
                if (e.currentFloor == 8 && e.doorOpen) break;
            }

            Direction directionAfter8 = e.direction;

            // Continue past floor 8
            for (int i = 0; i < 15; i++) {
                c.tick();
                if (e.currentFloor == 2) break;
            }

            assert e.currentFloor == 2 : "Should reverse and go to floor 2";
        });
    }

    private static void testNoReversalWhenWorkAhead() {
        test("Elevator continues direction when work ahead", () -> {
            Elevator e = new Elevator(0, 10, 0);
            Scheduler s = new Scheduler(0, 10);
            Controller c = new Controller(e, s, 1);

            c.submitCarCall(5);
            c.submitCarCall(3);

            // First stop at floor 3
            for (int i = 0; i < 10; i++) {
                c.tick();
                if (e.currentFloor == 3 && e.doorOpen) break;
            }

            // Direction should still be UP (5 is ahead)
            assert e.direction == Direction.UP : "Should stay UP with work ahead";
        });
    }

    private static void testIdlePicksDirectionUp() {
        test("Idle elevator picks UP when requests are above", () -> {
            Elevator e = new Elevator(0, 10, 3);
            Scheduler s = new Scheduler(0, 10);
            Controller c = new Controller(e, s, 1);

            c.submitCarCall(7);

            c.tick(); // Should pick UP
            assert e.direction == Direction.UP : "Should pick UP direction";
        });
    }

    private static void testIdlePicksDirectionDown() {
        test("Idle elevator picks DOWN when only requests below", () -> {
            Elevator e = new Elevator(0, 10, 7);
            Scheduler s = new Scheduler(0, 10);
            Controller c = new Controller(e, s, 1);

            c.submitCarCall(2);

            c.tick(); // Should pick DOWN
            assert e.direction == Direction.DOWN : "Should pick DOWN direction";
        });
    }

    // ============ Complex Scenarios ============

    private static void testMixedHallAndCarCalls() {
        test("Elevator handles mixed hall and car calls", () -> {
            Elevator e = new Elevator(0, 10, 0);
            Scheduler s = new Scheduler(0, 10);
            Controller c = new Controller(e, s, 1);

            c.submitHallCall(3, Direction.UP);
            c.submitCarCall(5);
            c.submitHallCall(8, Direction.DOWN);

            // Should service in logical order
            for (int i = 0; i < 20; i++) {
                c.tick();
                if (e.currentFloor == 5 && e.doorOpen) break;
            }

            assert e.currentFloor == 5 : "Should reach floor 5";
        });
    }

    private static void testRequestsClearedAtFloor() {
        test("All requests cleared when stopping at floor", () -> {
            Elevator e = new Elevator(0, 10, 0);
            Scheduler s = new Scheduler(0, 10);
            Controller c = new Controller(e, s, 1);

            // Multiple requests at same floor
            c.submitHallCall(3, Direction.UP);
            c.submitHallCall(3, Direction.DOWN);
            c.submitCarCall(3);

            // Move to floor 3
            for (int i = 0; i < 10; i++) {
                c.tick();
                if (e.currentFloor == 3 && e.doorOpen) break;
            }

            c.tick(); // Let doors close

            // All requests at floor 3 should be cleared
            assert !s.viewUpHall().contains(3) : "UP call should be cleared";
            assert !s.viewDownHall().contains(3) : "DOWN call should be cleared";
            assert !s.viewCarStops().contains(3) : "Car stop should be cleared";
        });
    }

    private static void testComplexScenarioWithMultipleRequests() {
        test("Complex scenario: multiple calls, multiple stops", () -> {
            Elevator e = new Elevator(0, 10, 0);
            Scheduler s = new Scheduler(0, 10);
            Controller c = new Controller(e, s, 1);

            c.submitHallCall(2, Direction.UP);
            c.submitCarCall(5);
            c.submitHallCall(7, Direction.DOWN);
            c.submitCarCall(9);

            // Simulate full scenario
            int stopCount = 0;
            for (int i = 0; i < 50; i++) {
                if (e.doorOpen && e.doorDwellTicks == 1) stopCount++;
                c.tick();
                if (!s.hasAnyRequests() && e.direction == Direction.IDLE && !e.doorOpen) break;
            }

            assert stopCount >= 3 : "Should have stopped at least 3 times";
            assert e.direction == Direction.IDLE : "Should end IDLE";
            assert !s.hasAnyRequests() : "All requests should be fulfilled";
        });
    }

    // ============ Edge Cases ============

    private static void testTopFloorBoundary() {
        test("Elevator cannot move above max floor", () -> {
            Elevator e = new Elevator(0, 10, 10);
            e.moveOneFloor(Direction.UP);
            assert e.currentFloor == 10 : "Should stay at floor 10";
        });
    }

    private static void testBottomFloorBoundary() {
        test("Elevator cannot move below min floor", () -> {
            Elevator e = new Elevator(0, 10, 0);
            e.moveOneFloor(Direction.DOWN);
            assert e.currentFloor == 0 : "Should stay at floor 0";
        });
    }

    private static void testDuplicateHallCalls() {
        test("Duplicate hall calls are idempotent (TreeSet dedup)", () -> {
            Elevator e = new Elevator(0, 10, 0);
            Scheduler s = new Scheduler(0, 10);
            Controller c = new Controller(e, s, 1);

            c.submitHallCall(5, Direction.UP);
            c.submitHallCall(5, Direction.UP);
            c.submitHallCall(5, Direction.UP);

            // Should only have one request at floor 5
            assert s.viewUpHall().size() == 1 : "Duplicate calls should be deduplicated";
        });
    }

    private static void testDuplicateCarCalls() {
        test("Duplicate car calls are idempotent (TreeSet dedup)", () -> {
            Elevator e = new Elevator(0, 10, 0);
            Scheduler s = new Scheduler(0, 10);
            Controller c = new Controller(e, s, 1);

            c.submitCarCall(6);
            c.submitCarCall(6);
            c.submitCarCall(6);

            // Should only have one car stop
            assert s.viewCarStops().size() == 1 : "Duplicate calls should be deduplicated";
        });
    }

    private static void testOutOfBoundsCalls() {
        test("Out-of-bounds calls throw exception", () -> {
            Elevator e = new Elevator(0, 10, 0);
            Scheduler s = new Scheduler(0, 10);
            Controller c = new Controller(e, s, 1);

            boolean exceptionThrown = false;
            try {
                c.submitCarCall(-1);
            } catch (IllegalArgumentException ex) {
                exceptionThrown = true;
            }
            assert exceptionThrown : "Should throw for negative floor";

            exceptionThrown = false;
            try {
                c.submitHallCall(15, Direction.UP);
            } catch (IllegalArgumentException ex) {
                exceptionThrown = true;
            }
            assert exceptionThrown : "Should throw for floor > max";
        });
    }

    // ============ Test Helper ============

    private static void test(String name, Runnable testLogic) {
        testsRun++;
        try {
            testLogic.run();
            testsPassed++;
            System.out.printf("✓ %s%n", name);
        } catch (AssertionError e) {
            System.out.printf("✗ %s: %s%n", name, e.getMessage());
        } catch (Exception e) {
            System.out.printf("✗ %s: Unexpected exception: %s%n", name, e.getMessage());
        }
    }
}
