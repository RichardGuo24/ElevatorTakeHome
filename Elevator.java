public final class Elevator {
    public final int minFloor;
    public final int maxFloor;

    int currentFloor = 0;
    Direction direction = Direction.IDLE;
    boolean doorOpen = false;
    int doorDwellTicks = 0; // counts down while doors are open

    public Elevator(int minFloor, int maxFloor, int startFloor) {
        if (minFloor > maxFloor)
            throw new IllegalArgumentException("min > max");
        if (startFloor < minFloor || startFloor > maxFloor)
            throw new IllegalArgumentException("start out of range");
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
        this.currentFloor = startFloor;
    }

    boolean atTop() {
        return currentFloor == maxFloor;
    }

    boolean atBottom() {
        return currentFloor == minFloor;
    }

    void moveOneFloor(Direction d) {
        if (d == Direction.UP && !atTop())
            currentFloor++;
        else if (d == Direction.DOWN && !atBottom())
            currentFloor--;
    }
}
