import java.util.*;

public final class Scheduler {
    // Hall calls are split by direction; car calls are destinations pressed inside
    // the cab.
    private final NavigableSet<Integer> upHall = new TreeSet<>();
    private final NavigableSet<Integer> downHall = new TreeSet<>();
    private final NavigableSet<Integer> carStops = new TreeSet<>();

    private final int minFloor, maxFloor;

    public Scheduler(int minFloor, int maxFloor) {
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
    }

    public void submitHallCall(int floor, Direction dir) {
        validate(floor);
        if (dir == Direction.UP)
            upHall.add(floor);
        else if (dir == Direction.DOWN)
            downHall.add(floor);
        else
            throw new IllegalArgumentException("Hall call must be UP or DOWN");
    }

    public void submitCarCall(int floor) {
        validate(floor);
        carStops.add(floor);
    }

    private void validate(int f) {
        if (f < minFloor || f > maxFloor)
            throw new IllegalArgumentException("Floor out of range: " + f);
    }

    /**
     * Stop here if it’s a car stop OR a matching-direction hall call (or any hall
     * call when idle).
     */
    public boolean shouldStopHere(int floor, Direction dir) {
        if (carStops.contains(floor))
            return true;
        if (dir == Direction.UP)
            return upHall.contains(floor);
        if (dir == Direction.DOWN)
            return downHall.contains(floor);
        // IDLE: if there’s any hall call here, open.
        return upHall.contains(floor) || downHall.contains(floor);
    }

    /** Clear all requests that are satisfied at this floor. */
    public void clearAt(int floor) {
        carStops.remove(floor);
        upHall.remove(floor);
        downHall.remove(floor);
    }

    public boolean hasAnyRequests() {
        return !(upHall.isEmpty() && downHall.isEmpty() && carStops.isEmpty());
    }

    /** Are there requests strictly ahead of currentFloor in the given direction? */
    public boolean hasAhead(int floor, Direction dir) {
        if (dir == Direction.UP) {
            return (firstAbove(upHall, floor) != null) ||
                    (firstAbove(carStops, floor) != null);
        } else if (dir == Direction.DOWN) {
            return (firstBelow(downHall, floor) != null) ||
                    (firstBelow(carStops, floor) != null);
        }
        return false;
    }

    /** Are there requests strictly behind currentFloor in the given direction? */
    public boolean hasBehind(int floor, Direction dir) {
        if (dir == Direction.UP) {
            return (firstBelow(upHall, floor) != null) ||
                    (firstBelow(carStops, floor) != null);
        } else if (dir == Direction.DOWN) {
            return (firstAbove(downHall, floor) != null) ||
                    (firstAbove(carStops, floor) != null);
        }
        return false;
    }

    /** Choose an initial direction from idle (prefer above, then below). */
    public Direction pickFromIdle(int floor) {
        boolean anyAbove = (firstAbove(upHall, floor) != null) || (firstAbove(downHall, floor) != null) ||
                (firstAbove(carStops, floor) != null);
        boolean anyBelow = (firstBelow(upHall, floor) != null) || (firstBelow(downHall, floor) != null) ||
                (firstBelow(carStops, floor) != null);
        if (anyAbove)
            return Direction.UP;
        if (anyBelow)
            return Direction.DOWN;
        return Direction.IDLE;
    }

    private Integer firstAbove(NavigableSet<Integer> s, int floor) {
        return s.higher(floor);
    }

    private Integer firstBelow(NavigableSet<Integer> s, int floor) {
        return s.lower(floor);
    }

    // For logging/inspection
    public SortedSet<Integer> viewUpHall() {
        return Collections.unmodifiableSortedSet(upHall);
    }

    public SortedSet<Integer> viewDownHall() {
        return Collections.unmodifiableSortedSet(downHall);
    }

    public SortedSet<Integer> viewCarStops() {
        return Collections.unmodifiableSortedSet(carStops);
    }
}
