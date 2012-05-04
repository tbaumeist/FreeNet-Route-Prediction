/**
 * This class will perform the RTI Prediction that finds the
 * possible attack nodes needed to perform an RTI attack.
 */

package frp.main.rti.prediction;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import frp.dataFileReaders.TopologyFileReader;
import frp.routing.RoutingManager;
import frp.routing.Topology;
import frp.routing.itersection.Intersection;
import frp.utils.CmdLineTools;

public class RTIPrediction {

	private static final int I_TOP = 0;
	private static final int I_OUT = 1;
	private static final int I_HTL = 2;
	private static final int I_HELP = 3;
	private static final String[][] PROG_ARGS = {
			{ "-t", "(required) Topology file name." },
			{ "-o", "(required) Output file name." },
			{ "-htl", "(required) Max hops to live count." },
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
		// / END: Arguments

		// output stream
		File outputFile = new File(outputFileName);
		PrintStream outputWriter = new PrintStream(outputFile);

		// topology
		TopologyFileReader topReader = new TopologyFileReader(topologyFileName);
		Topology topology = topReader.readFile();

		// calculate the intersection points
		List<Intersection> intersections = this.run(topology, maxHTL, outputFileName);

		// output all the intersection points
		Collections.sort(intersections);
		for (Intersection i : intersections) {
			outputWriter.println(i);
		}
		outputWriter.close();
	}

	public List<Intersection> run(Topology topology, int maxHTL, String outputFileName)
			throws Exception {

		// Calculate intersections
		System.out.println("Calculating intersections ...");
		RoutingManager manager = new RoutingManager(maxHTL);
		List<Intersection> intersections = manager.calculateNodeIntersections(
				null, topology, outputFileName);

		return intersections;
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