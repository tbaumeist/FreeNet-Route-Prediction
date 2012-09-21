package frp.main.insertModelEval;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import frp.dataFileReaders.CSVReader;
import frp.dataFileReaders.TopologyFileReaderManager;
import frp.routing.PathSet;
import frp.routing.RoutingManager;
import frp.routing.Topology;
import frp.utils.CmdLineTools;

public class ModelEvaluator {

	/*
	 * Program Parameters and their index location
	 */
	private static final int I_TOP = 0;
	private static final int I_OUT = 1;
	private static final int I_ROUTE = 2;
	private static final int I_DHTL = 3;
	private static final int I_HELP = 4;
	private static final String[][] PROG_ARGS = {
			{ "-tbase", "(required) Topology file base name." },
			{ "-o", "(required) Output file name." },
			{ "-route", "(required) Route prediction experiment file." },
			{
				"-dhtl",
				"(optional) Default:0, Specify the number of deterministic hops to live to account for." },
			{ "-h", "help command. Prints available arguments." } };

	/*
	 * Main entry point into executable
	 */
	public static void main(String[] args) {
		try {
			new ModelEvaluator(args);
		} catch (Exception ex) {
			System.out.println(CmdLineTools.toStringProgArgs(PROG_ARGS));
			System.out.println(ex.getMessage());
			System.out.println("!!!Error closing program!!!");
		}
	}

	private ModelEvaluator(String[] args) throws Exception {
		List<String> lstArgs = Arrays.asList(args);

		// Print out help info
		if (lstArgs.contains(CmdLineTools.getName(PROG_ARGS, I_HELP))) {
			System.out.println(CmdLineTools.toStringProgArgs(PROG_ARGS));
			return;
		}

		// Output file
		String outputFileName = CmdLineTools.getRequiredArg(CmdLineTools
				.getName(PROG_ARGS, I_OUT), lstArgs);

		// Topology file
		String topologyFileNameBase = CmdLineTools.getRequiredArg(CmdLineTools
				.getName(PROG_ARGS, I_TOP), lstArgs);

		// words mapped to storage nodes data file
		String routeFileName = CmdLineTools.getRequiredArg(CmdLineTools
				.getName(PROG_ARGS, I_ROUTE), lstArgs);
		
		String dhtlString = CmdLineTools.getArg(
				CmdLineTools.getName(PROG_ARGS, I_DHTL), lstArgs, "0");
		int dhtl = Integer.parseInt(dhtlString);

		run(outputFileName, topologyFileNameBase, routeFileName, dhtl);
	}

	public ModelEvaluator() {
	}

	public void run(String outputFileName, String topologyFileNameBase,
			String routeFileName, int dhtl) throws Exception {

		// output stream
		File outputFile = new File(outputFileName + ".csv");
		PrintStream outputStream = new PrintStream(outputFile);

		PathComparer comp = new PathComparer();
		comp.writerHeader(outputStream);

		// input file
		CSVReader reader = new CSVReader(routeFileName);
		List<ActualRoutePath> actPaths = ActualRoutePath.readFromFile(reader);
		
		TopologyFileReaderManager topReader = new TopologyFileReaderManager();
		
		String prevDataSet = "";
		Topology topology = null;
		RoutingManager manager = null;
		List<PathSet[]> pathSets = null;

		for (ActualRoutePath actPath : actPaths) {

			String filePostFix = "-" + actPath.getNodeCount() + "-"
					+ actPath.getPeerCount() + "-" + actPath.getHTL();
			String topFileName = topologyFileNameBase + filePostFix + ".dot";

			if(!prevDataSet.equals(filePostFix)){
				prevDataSet = filePostFix;
				// topology file
				topology = topReader.readFromFile(topFileName);
	
				// use insert path only here
				manager = new RoutingManager(actPath.getHTL(), dhtl);
				pathSets = manager.calculateRoutesFromNodes(null,
						null, topology, true);
	
				// Predicted paths output file
				File predictDataFile = new File(outputFile.getAbsoluteFile()
						+ filePostFix + ".predictedPaths");
				PrintStream predictWriter = new PrintStream(predictDataFile);
				predictWriter.println(topology);
	
				for (PathSet[] sArray : pathSets) {
					for (PathSet s : sArray)
						predictWriter.println(s);
				}
			}

			// Compare actual word storage to predicted
			outputStream.println(comp.compareStorageNodes(topology, manager
					.getResetHTL(), actPath.getHTL(), pathSets, actPath));
		}
	}
}
