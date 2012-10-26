package frp.gephi.rti;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import frp.dataFileReaders.CSVReader;
import frp.utils.CmdLineTools;

public class AttackNodeAnalysisMulti {

	private static final int I_TOP = 0;
	private static final int I_DAT = 1;
	private static final int I_OUT = 2;
	private static final int I_MAX_HTL = 3;
	private static final int I_HELP = 4;
	private static final String[][] PROG_ARGS = {
			{ "-tDir", "(required) Topology files directory name." },
			{ "-d", "(required) RTI analysis file name." },
			{ "-o", "(required) Output file name." },
			{ "-maxhtl", "(optional default 10) Maximum possible htl." },
			{ "-h", "help command. Prints available arguments." } };

	private final int iNodeCount = 0;
	private final int iPeerCount = 1;
	private final int iHTL = 2;
	private final int iSubSetSize = 8;
	private final int iMinTargetCount = 9;
	private final int iMaxTargetCount = 10;
	private final int iMinAttackNodes = 11;
	private final int iMaxAttackNodes = 12;

	public static void main(String[] args) {
		new AttackNodeAnalysisMulti(args);
	}

	public AttackNodeAnalysisMulti() {

	}

	private AttackNodeAnalysisMulti(String[] args) {
		try {
			List<String> lwArgs = Arrays.asList(args);

			if (lwArgs.contains(CmdLineTools.getName(PROG_ARGS, I_HELP))) {
				System.out.println(CmdLineTools.toStringProgArgs(PROG_ARGS));
				return;
			}

			// / Arguments
			String outputFileName = CmdLineTools.getRequiredArg(CmdLineTools
					.getName(PROG_ARGS, I_OUT), lwArgs);

			String topologyFileName = CmdLineTools.getRequiredArg(CmdLineTools
					.getName(PROG_ARGS, I_TOP), lwArgs);

			String dataFileName = CmdLineTools.getRequiredArg(CmdLineTools
					.getName(PROG_ARGS, I_DAT), lwArgs);
			
			String maxHTLStr = CmdLineTools.getArg(CmdLineTools
					.getName(PROG_ARGS, I_MAX_HTL), lwArgs, AttackNodeAnalysisSingle.MAX_HTL_COLUMNS +"");
			int maxHTL = Integer.parseInt(maxHTLStr);

			this.run(topologyFileName, dataFileName, outputFileName,maxHTL);

		} catch (Exception ex) {
			System.out.println(CmdLineTools.toStringProgArgs(PROG_ARGS));
			System.out.println("!!!Error!!!");
			System.out.println(ex.getMessage());
			System.out.println("!!!Closing program!!!");
		}
	}

	public void run(String topologyDirName, String attackAnalysisFile,
			String outputFileName, int maxHTL) throws Exception {

		AttackNodeAnalysisSingle single = new AttackNodeAnalysisSingle(maxHTL);

		PrintStream outputWriter = new PrintStream(new File(outputFileName));
		outputWriter.print(single.printCSVGraphHeader());
		outputWriter.print(single.printCSVPairHeader("Worst"));
		outputWriter.print(single.printCSVPairHeader("Best"));
		outputWriter.println();

		CSVReader reader = new CSVReader(attackAnalysisFile);

		int dataSetCount = 0;
		String lastUniqueDataSet = "";
		while (reader.readLine(this.iSubSetSize, "2")) {
			String dataSetID = this.getTopologyFileNameBase(reader);
			if (!lastUniqueDataSet.equals(dataSetID)) {
				lastUniqueDataSet = dataSetID;
				dataSetCount = 1;
			}

			String topFile = topologyDirName + File.separator
					+ this.getTopologyFileName(reader, dataSetCount);
			String nodeCountStr = reader.getColumn(this.iNodeCount);
			String peerCountStr = reader.getColumn(this.iPeerCount);
			String htlStr = reader.getColumn(this.iHTL);
			int htl = Integer.parseInt(htlStr);

			single = new AttackNodeAnalysisSingle(maxHTL, topFile);
			
			outputWriter.print(single.calcGraphStats(nodeCountStr,
					peerCountStr, htl, dataSetCount + ""));

			String nodeEntryMin = reader.getColumn(this.iMinAttackNodes);
			String targetCountMin = reader.getColumn(this.iMinTargetCount);
			outputWriter.print(single.calcSinglePairStats(nodeEntryMin, htl, targetCountMin));

			String nodeEntryMax = reader.getColumn(this.iMaxAttackNodes);
			String targetCountMax = reader.getColumn(this.iMaxTargetCount);
			outputWriter.print(single.calcSinglePairStats(nodeEntryMax, htl, targetCountMax));

			outputWriter.println();
			dataSetCount++;
		}
		outputWriter.close();
	}

	private String getTopologyFileNameBase(CSVReader reader) {
		StringBuilder b = new StringBuilder();
		b.append(reader.getColumn(this.iNodeCount));
		b.append("-");
		b.append(reader.getColumn(this.iPeerCount));
		b.append("-");
		b.append(reader.getColumn(this.iHTL));
		return b.toString();
	}

	private String getTopologyFileName(CSVReader reader, int dataSetCount) {
		StringBuilder b = new StringBuilder();
		b.append(this.getTopologyFileNameBase(reader));
		b.append("-");
		b.append(dataSetCount);
		b.append("-top.dot.fixed.dot");
		return b.toString();
	}

}
