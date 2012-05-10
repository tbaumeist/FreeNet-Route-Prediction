/*
 * This class will perform an analysis of how effective the RTI
 * attack will be against a given topology. It looks at different
 * attack node set sizes and calculates the number of nodes that
 * can be attacked { minimum, maximum, average } number of nodes 
 * that can be attacked.
 * 
 * Note: This is very slow code. It performs in combinatorial time.
 * As the size of the attack set grows so does the time to calculate
 * the effectiveness.
 */
package frp.main.rti.analysis;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import frp.dataFileReaders.TopologyFileReader;
import frp.main.rti.prediction.RTIPrediction;
import frp.routing.Topology;
import frp.routing.itersection.Intersection;
import frp.utils.CmdLineTools;

public class RTIAnalysis {
	private static final String DHTL_COUNT = "1";

	private static final int I_TOP = 0;
	private static final int I_OUT = 1;
	private static final int I_HTL = 2;
	private static final int I_DHTL = 3;
	private static final int I_MAGS = 4;
	private static final int I_HELP = 5;
	private static final String[][] PROG_ARGS = {
			{ "-t", "(required) Topology file name." },
			{ "-o", "(required) Output file name." },
			{ "-htl", "(required) Max hops to live count." },
			{"-dhtl", "(optional) Default:1, Specify the number of deterministic hops to live to account for." },
			{ "-mags", "(required) Max Attack Group Size." },
			{ "-h", "help command. Prints available arguments." } };

	private RTIAnalysis(String[] args) {
		try {
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

			// Max Attack Group Size
			int maxAGS = Integer.parseInt(CmdLineTools.getRequiredArg(
					CmdLineTools.getName(PROG_ARGS, I_MAGS), lwArgs));

			// Max HTL
			int maxHTL = Integer.parseInt(CmdLineTools.getRequiredArg(
					CmdLineTools.getName(PROG_ARGS, I_HTL), lwArgs));
			
			// Deterministic HTL
			String dhtlString = CmdLineTools.getArg(CmdLineTools.getName(
					PROG_ARGS, I_DHTL), lwArgs, DHTL_COUNT);
			int dhtl = Integer.parseInt(dhtlString);
			// / END: Arguments

			// output stream
			File outputFile = new File(outputFileName);
			PrintStream outputWriter = new PrintStream(outputFile);

			// topology
			TopologyFileReader topReader = new TopologyFileReader(
					topologyFileName);
			Topology topology = topReader.readFile();

			// / Get inputs to the analysis code
			// Calculate intersections
			RTIPrediction rtiPrediction = new RTIPrediction();
			List<Intersection> intersections = rtiPrediction.run(topology,
					maxHTL, outputFileName, dhtl);

			// output all the intersection points
			Collections.sort(intersections);
			File topXFile = new File(outputFileName + ".intersections");
			PrintStream topXWriter = new PrintStream(topXFile);
			for (Intersection i : intersections) {
				topXWriter.println(i);
			}
			topXWriter.close();
			// / END: Get inputs to the analysis code

			// / start of the interesting part of the code
			// Calculate the effectiveness for different attack node set sizes
			List<AttackPair> attackpairs = AttackPair
					.extractAttackPairs(intersections, topology);

			outputWriter.println(AttackSizeSet.getCSVHeader());
			for (int i = 0; i <= maxAGS; i++) {
				System.out.println("Analysing set size " + i + " ...");
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
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		new RTIAnalysis(args);
	}

}
