package frp.main.rtiAnalysis;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import frp.dataFileReaders.TopologyFileReader;
import frp.routing.RoutingManager;
import frp.routing.Topology;
import frp.routing.itersection.InsertNodeIntersections;
import frp.utils.CmdLineTools;

public class RTIAnalysis {

	private static final int I_TOP = 0;
	private static final int I_OUT = 1;
	private static final int I_HTL = 2;
	private static final int I_MAGS = 3;
	private static final int I_HELP = 4;
	private static final String[][] PROG_ARGS = {
			{ "-t", "(required) Topology file name." },
			{ "-o", "(required) Output file name." },
			{ "-htl", "(required) Max hops to live count." },
			{ "-mags", "(required) Max Attack Group Size." },
			{ "-h", "help command. Prints available arguments." } };

	private RTIAnalysis(String[] args) {
		try {
			List<String> lwArgs = Arrays.asList(args);

			if (lwArgs.contains(CmdLineTools.getName(PROG_ARGS, I_HELP))) {
				System.out.println(CmdLineTools.toStringProgArgs(PROG_ARGS));
				return;
			}

			// // Arguments ////
			String outputFileName = CmdLineTools.getRequiredArg(
					CmdLineTools.getName(PROG_ARGS, I_OUT), lwArgs);

			String topologyFileName = CmdLineTools.getRequiredArg(
					CmdLineTools.getName(PROG_ARGS, I_TOP), lwArgs);
			
			// Max Attack Group Size
			int maxAGS = Integer.parseInt(CmdLineTools.getRequiredArg(
					CmdLineTools.getName(PROG_ARGS, I_MAGS), lwArgs));

			// Max HTL
			int maxHTL = Integer.parseInt(CmdLineTools.getRequiredArg(
					CmdLineTools.getName(PROG_ARGS, I_HTL), lwArgs));
			// END Arguments

			// output stream
			File outputFile = new File(outputFileName);
			PrintStream outputWriter = new PrintStream(outputFile);

			// topology
			TopologyFileReader topReader = new TopologyFileReader(
					topologyFileName);
			Topology topology = topReader.readFile();

			// Calculate intersections
			System.out.println("Calculating intersections ...");
			RoutingManager manager = new RoutingManager(maxHTL);
			List<InsertNodeIntersections> intersections = manager
					.calculateNodeIntersections(null, topology);

			Hashtable<AttackPair, AttackPair> attackpairs = AttackPair
					.extractAttackPairs(intersections);

			for (int i = 0; i < maxAGS; i++) {
				System.out.println("Analysing set size "+i+" ...");
				AttackSizeSet attSet = new AttackSizeSet(i, attackpairs,
						topology.getAllNodes());
				outputWriter.println(attSet.toStringCSV());
			}

		} catch (Exception ex) {
			System.out.println(CmdLineTools.toStringProgArgs(PROG_ARGS));
			System.out.println(ex.getMessage());
			System.out.println("!!!Error closing program!!!");
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new RTIAnalysis(args);
	}

}
