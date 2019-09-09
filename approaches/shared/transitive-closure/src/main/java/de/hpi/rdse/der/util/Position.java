package de.hpi.rdse.der.util;

public class Position {

    private int x;
    private int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Position) {
            Position other = (Position) o;
            return this.x == other.x && this.y == other.y;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (x + "-" + y).hashCode();
    }
}
