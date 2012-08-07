package frp.gephi.rti;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.*;

import org.gephi.data.attributes.api.*;
import org.gephi.graph.api.*;
import org.gephi.statistics.plugin.ClusteringCoefficient;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

import frp.dataFileReaders.CSVReader;
import frp.gephi.GephiHelper;
import frp.utils.CmdLineTools;

public class AttackNodeAnalysis {

	private static final int I_TOP = 0;
	private static final int I_DAT = 1;
	private static final int I_OUT = 2;
	private static final int I_HELP = 3;
	private static final String[][] PROG_ARGS = {
			{ "-tDir", "(required) Topology files directory name." },
			{ "-d", "(required) RTI analysis file name." },
			{ "-o", "(required) Output file name." },
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
		new AttackNodeAnalysis(args);
	}

	public AttackNodeAnalysis() {

	}

	private AttackNodeAnalysis(String[] args) {
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

			this.run(topologyFileName, dataFileName, outputFileName);

		} catch (Exception ex) {
			System.out.println(CmdLineTools.toStringProgArgs(PROG_ARGS));
			System.out.println("!!!Error!!!");
			System.out.println(ex.getMessage());
			System.out.println("!!!Closing program!!!");
		}
	}

	public void run(String topologyDirName, String attackAnalysisFile,
			String outputFileName) throws Exception {

		GephiHelper gHelper = new GephiHelper();

		PrintStream outputWriter = new PrintStream(new File(outputFileName));
		outputWriter.print("Node Count,Peer Count,HTL,Set #");
		outputWriter
				.print(",Clustering Coefficient,Network Diameter,Avg Path Length");
		outputWriter
				.print(",,Worst A,Worst B,Shortest Path HTL,Target Count,Location Diff");
		outputWriter
				.print(",A Theoretical Reach,A Actual Reach,B Theoretical Reach,B Actual Reach");
		outputWriter
				.print(",,Best A,Best B,Shortest Path HTL,Target Count,Location Diff");
		outputWriter
				.print(",A Theoretical Reach,A Actual Reach,B Theoretical Reach,B Actual Reach");
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
			DirectedGraph graph = gHelper.loadGraphFile(topFile);

			// Id info
			String htl = reader.getColumn(this.iHTL);
			outputWriter.print(reader.getColumn(this.iNodeCount) + ",");
			outputWriter.print(reader.getColumn(this.iPeerCount) + ",");
			outputWriter.print(htl + ",");
			outputWriter.print(dataSetCount + ",");

			// Clustering Coefficient
			ClusteringCoefficient cCoeff = new ClusteringCoefficient();
			AttributeModel attributeModel = Lookup.getDefault().lookup(
					AttributeController.class).getModel();
			GraphModel graphModel = graph.getGraphModel();
			cCoeff.execute(graphModel, attributeModel);
			outputWriter.print(cCoeff.getAverageClusteringCoefficient() + ",");

			GraphDistance distance = new GraphDistance();
			distance.execute(graphModel, attributeModel);
			outputWriter.print(distance.getDiameter() + ",");
			outputWriter.print(distance.getPathLength() + ",");

			// worst
			NodePair p = this.parseTwoNodes(reader
					.getColumn(this.iMinAttackNodes));

			outputWriter.print(",");
			outputWriter.print(p + ",");
			outputWriter.print(this
					.findShortestPaths(graph, p.getA(), p.getB())
					+ ",");
			outputWriter.print(reader.getColumn(this.iMinTargetCount) + ",");
			outputWriter.print(p.getDifference() + ",");

			int theoryReach = this.calcTheoreticalReach(htl, reader.getColumn(this.iPeerCount));
			outputWriter.print(theoryReach + ",");
			outputWriter.print(this.calcActualReach(graph, p.getA(), htl) + ",");
			outputWriter.print(theoryReach + ",");
			outputWriter.print(this.calcActualReach(graph, p.getB(), htl) + ",");

			// best
			p = this.parseTwoNodes(reader.getColumn(this.iMaxAttackNodes));

			outputWriter.print(",");
			outputWriter.print(p + ",");
			outputWriter.print(this
					.findShortestPaths(graph, p.getA(), p.getB())
					+ ",");
			outputWriter.print(reader.getColumn(this.iMaxTargetCount) + ",");
			outputWriter.print(p.getDifference() + ",");
			
			outputWriter.print(theoryReach + ",");
			outputWriter.print(this.calcActualReach(graph, p.getA(), htl) + ",");
			outputWriter.print(theoryReach + ",");
			outputWriter.print(this.calcActualReach(graph, p.getB(), htl) + ",");

			outputWriter.println();
			dataSetCount++;
		}
	}

	private int calcTheoreticalReach(String htl, String peerCount) {
		int h = Integer.parseInt(htl);
		int pCount = Integer.parseInt(peerCount);
		int k = pCount - 1;
		int reach = (int) (((Math.pow(k,h) - 1) + (Math.pow(k,h-1) - 1)) / (k - 1));
		return reach;
	}

	private int calcActualReach(DirectedGraph graph, String nodeName, String htl) {

		Node node = graph.getNode(nodeName);
		int h = Integer.parseInt(htl);
		List<Node> visited = new ArrayList<Node>();
		List<Node> active = new ArrayList<Node>();
		
		active.add(node);
		visited.add(node);
		
		for( int i =0; i < h-1; i++){
			List<Node> neighbors = new ArrayList<Node>();
			for(Node n : active){
				for(Node n1 : graph.getNeighbors(n)){
					if(!visited.contains(n1)){
						visited.add(n1);
						neighbors.add(n1);
					}
				}
			}
			active = neighbors;
		}
		
		return visited.size();
	}

	private int findShortestPaths(DirectedGraph graph, String nodeA,
			String nodeB) {
		Node a = graph.getNode(nodeA);
		Node b = graph.getNode(nodeB);

		List<Node> tmpPath = new ArrayList<Node>();
		tmpPath.add(a);

		Queue<List<Node>> q = new LinkedList<List<Node>>();
		q.add(tmpPath);
		int shortestPath = Integer.MAX_VALUE;

		// breadth first search
		while (!q.isEmpty()) {
			tmpPath = q.poll();
			Node lastNode = tmpPath.get(tmpPath.size() - 1);
			if (tmpPath.size() > shortestPath)
				continue;
			if (lastNode.equals(b)) {
				return tmpPath.size();
			}
			for (Node n : graph.getNeighbors(lastNode)) {
				if (!tmpPath.contains(n)) {
					List<Node> newPath = new ArrayList<Node>();
					newPath.addAll(tmpPath);
					newPath.add(n);
					q.add(newPath);
				}
			}
		}

		return 0;
	}

	private class NodePair {
		private double a;
		private double b;

		public NodePair(String nodeA, String nodeB) {
			double one = Double.parseDouble(nodeA.trim());
			double two = Double.parseDouble(nodeB.trim());
			this.a = Math.min(one, two);
			this.b = Math.max(one, two);
		}

		public double getDifference() {
			double a = this.b - this.a;
			double b = (this.a + 1.0) - this.b;
			return Math.min(a, b);
		}

		public String getA() {
			return this.a + "";
		}

		public String getB() {
			return this.b + "";
		}

		@Override
		public String toString() {
			return this.a + "," + this.b;
		}
	}

	private NodePair parseTwoNodes(String text) {
		Pattern pattern = java.util.regex.Pattern
				.compile("[-+]?[0-9]*\\.[0-9]+([eE][-+]?[0-9]+)?");

		Matcher matcher = pattern.matcher(text);
		matcher.find();
		String nodeA = matcher.group();

		matcher.find();
		String nodeB = matcher.group();

		return new NodePair(nodeA, nodeB);
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
