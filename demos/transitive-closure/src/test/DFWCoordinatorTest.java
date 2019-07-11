package test;

import main.DFWCoordinator;
import main.DFWPosition;
import org.junit.Test;

public class DFWCoordinatorTest {


    @Test
    public void calculated() {
        DFWCoordinator dfwCoordinator = new DFWCoordinator(4,1);
        DFWPosition pivot0 = new DFWPosition(0, 0 );
        DFWPosition blk10 = new DFWPosition(1, 0 );
        DFWPosition blk01 = new DFWPosition(0, 1 );

        DFWPosition pivot1 = new DFWPosition(1, 1 );
        DFWPosition pivot2 = new DFWPosition(2, 2 );
        DFWPosition pivot3 = new DFWPosition(3, 3 );

        dfwCoordinator.calculated(pivot0);
        dfwCoordinator.calculated(blk10);
        dfwCoordinator.calculated(blk01);
    }

    @Test
    public void getNext() {
    }

    @Test
    public void isDone() {
    }
}
