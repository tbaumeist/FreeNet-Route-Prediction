package test.frp.routing;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Test;

import test.frp.Helper;

import frp.routing.PathSet;
import frp.routing.RoutingManager;
import frp.routing.Topology;
import frp.utils.Pair;
import frp.utils.Progresser;

public class Test_MemoryFootPrint extends RoutingManager {
    private int mb = 1024 * 1024;
    private final static int maxHTL = 5;
    private final static int detHTL = 0;

    public Test_MemoryFootPrint() {
        super(maxHTL, detHTL);
    }

    @Test
    public void testNoCache() throws Exception {

        Topology top = Helper.load125_10Topology();
        List<Pair<Double, String>> startNodes = checkStartNodes(null, top);

        System.out.println("Memory footprint no cache ...");
        Progresser prog = new Progresser(System.out, startNodes.size() * 2);
        List<PathSet[]> pathInsertSets = calculateRoutesFromNodes(prog,
                startNodes, top, true);
        assertTrue(pathInsertSets != null);

        List<PathSet[]> pathRequestSets = calculateRoutesFromNodes(prog,
                startNodes, top, false);
        assertTrue(pathRequestSets != null);

        Runtime runtime = Runtime.getRuntime();
        System.out.println("\tUsed memory no cache: "
                + (runtime.totalMemory() - runtime.freeMemory()) / this.mb
                + "mb");
    }

    @Test
    public void testCache() throws Exception {

        Topology top = Helper.load125_10Topology();
        List<Pair<Double, String>> startNodes = checkStartNodes(null, top);

        System.out.println("Memory footprint cache ...");
        Progresser prog = new Progresser(System.out, startNodes.size() * 2);
        List<PathSet[]> pathInsertSets = calculateRoutesFromNodes(prog,
                startNodes, top, true);
        assertTrue(pathInsertSets != null);

        // save the predicted insert paths
        File tmpStorage = File.createTempFile("RTI", "Insert");
        PathSet.savePathSetList(pathInsertSets, tmpStorage.getAbsolutePath());
        pathInsertSets = null;
        System.gc();

        List<PathSet[]> pathRequestSets = calculateRoutesFromNodes(prog,
                startNodes, top, false);

        Runtime runtime = Runtime.getRuntime();
        System.out.println("\tUsed memory with cache: "
                + (runtime.totalMemory() - runtime.freeMemory()) / this.mb
                + "mb");

        assertTrue(pathRequestSets != null);

        PathSet.removeAllSavedPathSetList(tmpStorage.getAbsolutePath());
    }

}
