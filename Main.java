public final class Main {
    public static void main(String[] args) {
        // Building 0..10, start at 0, doors dwell for 1 tick when stopping
        Elevator e = new Elevator(0, 10, 0);
        Scheduler s = new Scheduler(0, 10);
        Controller c = new Controller(e, s, 1);

        // Scenario:
        // - Hall UP at 3, Hall DOWN at 8, passenger at 3 presses car 9
        c.submitHallCall(3, Direction.UP);
        c.submitHallCall(8, Direction.DOWN);

        for (int t = 0; t < 30; t++) {
            // When we first open at 3, simulate the rider pressing 9 from inside.
            if (e.currentFloor == 3 && e.doorOpen && !s.viewCarStops().contains(9)) {
                c.submitCarCall(9);
            }
            System.out.printf("t=%02d  %s%n", t, c.snapshot());
            c.tick();
            if (!s.hasAnyRequests() && e.direction == Direction.IDLE && !e.doorOpen)
                break;
        }
    }
}
