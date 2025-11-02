public final class Controller {
    private final Elevator cab;
    private final Scheduler scheduler;
    private final int dwellTicks; // how long to keep doors open when stopping

    public Controller(Elevator cab, Scheduler scheduler, int dwellTicks) {
        this.cab = cab;
        this.scheduler = scheduler;
        this.dwellTicks = Math.max(1, dwellTicks);
    }

    public void submitHallCall(int floor, Direction dir) {
        scheduler.submitHallCall(floor, dir);
    }

    public void submitCarCall(int floor) {
        scheduler.submitCarCall(floor);
    }

    /** Advance the simulation by one tick. */
    public void tick() {
        // 1) Doors currently open? Decrement dwell and close when done.
        if (cab.doorOpen) {
            cab.doorDwellTicks--;
            if (cab.doorDwellTicks <= 0)
                cab.doorOpen = false; // doors close at end of dwell
            return;
        }

        // 2) Should we stop at the current floor?
        if (scheduler.shouldStopHere(cab.currentFloor, cab.direction)) {
            scheduler.clearAt(cab.currentFloor);
            openDoors();
            return; // no movement this tick
        }

        // 3) Decide direction & move.
        if (cab.direction == Direction.IDLE) {
            cab.direction = scheduler.pickFromIdle(cab.currentFloor);
        }

        if (cab.direction == Direction.UP || cab.direction == Direction.DOWN) {
            // Continue in current direction if anything is ahead.
            if (scheduler.hasAhead(cab.currentFloor, cab.direction)) {
                cab.moveOneFloor(cab.direction);
            } else if (scheduler.hasBehind(cab.currentFloor, cab.direction)) {
                // Nothing ahead; reverse if work exists behind.
                cab.direction = (cab.direction == Direction.UP) ? Direction.DOWN : Direction.UP;
                cab.moveOneFloor(cab.direction);
            } else {
                // Nowhere to go.
                cab.direction = Direction.IDLE;
            }
        }
    }

    private void openDoors() {
        cab.doorOpen = true;
        cab.doorDwellTicks = dwellTicks;
    }

    // For demo/logging
    public Snapshot snapshot() {
        return new Snapshot(cab.currentFloor, cab.direction, cab.doorOpen,
                scheduler.viewUpHall(), scheduler.viewDownHall(), scheduler.viewCarStops());
    }

    public static final class Snapshot {
        public final int floor;
        public final Direction direction;
        public final boolean doorOpen;
        public final java.util.SortedSet<Integer> up, down, car;

        Snapshot(int f, Direction d, boolean open,
                java.util.SortedSet<Integer> up, java.util.SortedSet<Integer> down, java.util.SortedSet<Integer> car) {
            this.floor = f;
            this.direction = d;
            this.doorOpen = open;
            this.up = up;
            this.down = down;
            this.car = car;
        }

        @Override
        public String toString() {
            return String.format("floor=%d dir=%s door=%s | up=%s down=%s car=%s",
                    floor, direction, doorOpen ? "OPEN" : "CLOSED", up, down, car);
        }
    }
}
