/**
 * This class will perform the RTI Prediction that finds the
 * possible attack nodes needed to perform an RTI attack.
 */

package frp.main.rti.prediction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import frp.dataFileReaders.TopologyFileReaderManager;
import frp.main.rti.analysis.AttackPair;
import frp.routing.RoutingManager;
import frp.routing.Topology;
import frp.utils.CmdLineTools;

public class RTIPrediction {

    public static final String FILE_INTERSECTIONS_SUFFIX = ".intersections";

    private static final String DHTL_COUNT = "1";

    private static final int I_TOP = 0;
    private static final int I_OUT = 1;
    private static final int I_HTL = 2;
    private static final int I_DHTL = 3;
    private static final int I_HELP = 4;
    private static final String[][] PROG_ARGS = {
            { "-t", "(required) Topology file name." },
            { "-o", "(required) Output file name." },
            { "-htl", "(required) Max hops to live count." },
            {
                    "-dhtl",
                    "(optional) Default:1, Specify the number of deterministic hops to live to account for." },
            { "-h", "help command. Prints available arguments." } };

    public RTIPrediction() {
    }

    private RTIPrediction(String[] args) throws Exception {
        List<String> lwArgs = Arrays.asList(args);

        if (lwArgs.contains(CmdLineTools.getName(PROG_ARGS, I_HELP))) {
            System.out.println(CmdLineTools.toStringProgArgs(PROG_ARGS));
            return;
        }

        // / Arguments
        String outputFileName = CmdLineTools.getRequiredArg(
                CmdLineTools.getName(PROG_ARGS, I_OUT), lwArgs);

        String topologyFileName = CmdLineTools.getRequiredArg(
                CmdLineTools.getName(PROG_ARGS, I_TOP), lwArgs);

        // Max HTL
        int maxHTL = Integer.parseInt(CmdLineTools.getRequiredArg(
                CmdLineTools.getName(PROG_ARGS, I_HTL), lwArgs));

        // Deterministic HTL
        String dhtlString = CmdLineTools.getArg(
                CmdLineTools.getName(PROG_ARGS, I_DHTL), lwArgs, DHTL_COUNT);
        int dhtl = Integer.parseInt(dhtlString);
        // / END: Arguments

        // topology
        TopologyFileReaderManager topReader = new TopologyFileReaderManager();
        Topology topology = topReader.readFromFile(topologyFileName);

        // calculate the intersection points
        this.run(topology, maxHTL, outputFileName, dhtl);
    }

    public List<AttackPair> run(Topology topology, int maxHTL,
            String outputFileName, int dhtl) throws Exception {

        // Calculate intersections
        RoutingManager manager = new RoutingManager(maxHTL, dhtl);
        List<AttackPair> pairs = manager.calculateNodeIntersections(null,
                topology, outputFileName);

        Collections.sort(pairs);

        return pairs;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            new RTIPrediction(args);
        } catch (Exception ex) {
            System.out.println(CmdLineTools.toStringProgArgs(PROG_ARGS));
            System.out.println(ex.getMessage());
            System.out.println("!!!Error closing program!!!");
        }
    }

}
