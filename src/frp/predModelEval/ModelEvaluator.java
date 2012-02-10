package frp.predModelEval;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import frp.dataFileReaders.CommLogFileReader;
import frp.dataFileReaders.WordMapFileReader;
import frp.dataFileReaders.TopologyFileReader;
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
	private static final int I_HTL = 2;
	private static final int I_MAP = 3;
	private static final int I_WORDS = 4;
	private static final int I_COMM = 5;
	private static final int I_HELP = 6;
	private static final String[][] PROG_ARGS = {
			{ "-t", "(required) Topology file name." },
			{ "-o", "(required) Output file name." },
			{ "-htl", "(required) Max hops to live count." },
			{ "-map", "(required) Words mapped to storage nodes." },
			{ "-words", "(required) Words inserted." },
			{ "-comm", "(optional) Full communications log." },
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
		String outputFileName = CmdLineTools.getRequiredArg(
				CmdLineTools.getName(PROG_ARGS, I_OUT), lstArgs);

		// Topology file
		String topologyFileName = CmdLineTools.getRequiredArg(
				CmdLineTools.getName(PROG_ARGS, I_TOP), lstArgs);

		// words mapped to storage nodes data file
		String wordStorageMapFileName = CmdLineTools.getRequiredArg(
				CmdLineTools.getName(PROG_ARGS, I_MAP), lstArgs);

		// words data file
		String wordsFileName = CmdLineTools.getRequiredArg(
				CmdLineTools.getName(PROG_ARGS, I_WORDS), lstArgs);

		// communication log file
		String commLogFileName = CmdLineTools.getArg(
				CmdLineTools.getName(PROG_ARGS, I_COMM), lstArgs, "");

		// Max HTL
		int htl = Integer.parseInt(CmdLineTools.getRequiredArg(
				CmdLineTools.getName(PROG_ARGS, I_HTL), lstArgs));

		run(outputFileName, topologyFileName, wordsFileName,
				wordStorageMapFileName, commLogFileName, htl);
	}

	public ModelEvaluator() {
	}

	public void run(String outputFileName, String topologyFileName,
			String wordsFileName, String wordStorageMapFileName,
			String commLogFileName, int maxHTL) throws Exception {

		// output stream
		File outputFile = new File(outputFileName);
		PrintStream outputStream = new PrintStream(outputFile);

		// topology file
		TopologyFileReader topReader = new TopologyFileReader(topologyFileName);
		Topology topology = topReader.readFile();

		WordMapFileReader mapReader = new WordMapFileReader(
				wordStorageMapFileName, wordsFileName);

		List<StoredWordData> storedData = mapReader.readData(outputStream);

		// process communication log
		CommLog log = null;
		if (!commLogFileName.isEmpty()) {
			CommLogFileReader commReader = new CommLogFileReader(
					commLogFileName);
			log = commReader.readFile();
			// communication log output file
			File logDataFile = new File(outputFile.getAbsoluteFile()
					+ ".log_process");
			PrintStream logWriter = new PrintStream(logDataFile);
			logWriter.println(log);
		}

		// use insert path only here
		RoutingManager manager = new RoutingManager(maxHTL);
		List<PathSet[]> pathSets = manager.calculateRoutesFromNodes(null,
				topology, true);

		// full data output file
		File fullDataFile = new File(outputFile.getAbsoluteFile() + ".csv");
		PrintStream fullWriter = new PrintStream(fullDataFile);

		// Predicted paths output file
		File predictDataFile = new File(outputFile.getAbsoluteFile()
				+ ".predictedPaths");
		PrintStream predictWriter = new PrintStream(predictDataFile);
		predictWriter.println(topology);

		for (PathSet[] sArray : pathSets) {
			for (PathSet s : sArray)
				predictWriter.println(s);
		}

		// Compare actual word storage to predicted
		PathComparer comp = new PathComparer();
		comp.compareStorageNodes(outputStream, fullWriter, topology,
				storedData, pathSets, log, manager.getResetHTL(), maxHTL);
	}
}
