package main;

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

    boolean sameX(int x) {
        return this.x == x;
    }

    boolean sameY(int y) {
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
        return (x + "-" + y).hashCode();
    }
}
