package de.hpi.ddd.transitiveClosure;

public class DFWPosition {

    private int x;
    private int y;

    public DFWPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean sameX(int x) {
        return this.x == x;
    }

    public boolean sameY(int y) {
        return this.y == y;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DFWPosition) {
            DFWPosition other = (DFWPosition) o;
            return this.x == other.x && this.y == other.y;
        }

        return false;
    }

    @Override
    public int hashCode() {
        String hash = x + "-" + y;

        return hash.hashCode();
    }
}