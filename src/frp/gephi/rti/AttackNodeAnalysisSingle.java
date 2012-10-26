package frp.gephi.rti;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;

import org.gephi.statistics.plugin.ClusteringCoefficient;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

import frp.gephi.GephiHelper;
import frp.utils.CmdLineTools;

public class AttackNodeAnalysisSingle {

	private static final int I_TOP = 0;
	private static final int I_NODE = 1;
	private static final int I_PEER = 2;
	private static final int I_HTL = 3;
	private static final int I_DATASET = 4;
	private static final int I_APP = 5;
	private static final int I_OUT = 6;
	private static final int I_MAX_HTL = 7;
	private static final int I_HELP = 8;
	private static final String[][] PROG_ARGS = {
			{ "-t", "(required) Topology file name." },
			{ "-n", "(required) Total node count." },
			{ "-p", "(required) Peer count." },
			{ "-htl", "(required) Hop To Live count." },
			{ "-ds", "(required) Data Set count." },
			{ "-a", "(optional default = false) Append to output file." },
			{ "-o", "(required) Output file name." },
			{ "-maxhtl", "(optional default 10) Maximum possible htl." },
			{ "-h", "help command. Prints available arguments." } };

	private GephiHelper gHelper;
	private DirectedGraph graph;
	private int totalReachFields = 0;
	
	public static final int MAX_HTL_COLUMNS = 10;

	public static void main(String[] args) {
		try {
			List<String> lwArgs = Arrays.asList(args);

			if (lwArgs.contains(CmdLineTools.getName(PROG_ARGS, I_HELP))) {
				System.out.println(CmdLineTools.toStringProgArgs(PROG_ARGS));
				return;
			}

			// / Arguments
			String appendStr = CmdLineTools.getArg(CmdLineTools.getName(
					PROG_ARGS, I_APP), lwArgs, "");
			boolean append = !appendStr.isEmpty();

			String outputFileName = CmdLineTools.getRequiredArg(CmdLineTools
					.getName(PROG_ARGS, I_OUT), lwArgs);

			String topologyFileName = CmdLineTools.getRequiredArg(CmdLineTools
					.getName(PROG_ARGS, I_TOP), lwArgs);

			String nodeCount = CmdLineTools.getRequiredArg(CmdLineTools
					.getName(PROG_ARGS, I_NODE), lwArgs);

			String peerCount = CmdLineTools.getRequiredArg(CmdLineTools
					.getName(PROG_ARGS, I_PEER), lwArgs);

			String htlStr = CmdLineTools.getRequiredArg(CmdLineTools.getName(
					PROG_ARGS, I_HTL), lwArgs);
			int htl = Integer.parseInt(htlStr);
			
			String maxHTLStr = CmdLineTools.getArg(CmdLineTools
					.getName(PROG_ARGS, I_MAX_HTL), lwArgs, AttackNodeAnalysisSingle.MAX_HTL_COLUMNS +"");
			int maxHTL = Integer.parseInt(maxHTLStr);

			String dataSetCount = CmdLineTools.getRequiredArg(CmdLineTools
					.getName(PROG_ARGS, I_DATASET), lwArgs);

			AttackNodeAnalysisSingle s = new AttackNodeAnalysisSingle(maxHTL, topologyFileName);

			PrintStream outputWriter = new PrintStream(new FileOutputStream(
					new File(outputFileName), append));
			if (!append)
				outputWriter.println(s.printCSVGraphHeader());

			outputWriter.println(s.calcGraphStats(nodeCount, peerCount, htl,
					dataSetCount));
			outputWriter.close();

		} catch (Exception ex) {
			System.out.println(CmdLineTools.toStringProgArgs(PROG_ARGS));
			System.out.println("!!!Error!!!");
			System.out.println(ex.getMessage());
			System.out.println("!!!Closing program!!!");
		}

	}

	public AttackNodeAnalysisSingle(int totalHTLColumns, String topFileName) throws Exception {
		this(totalHTLColumns);
		this.graph = this.gHelper.loadGraphFile(topFileName);
	}

	public AttackNodeAnalysisSingle(int totalHTLColumns) {
		this.gHelper = new GephiHelper();
		this.totalReachFields = totalHTLColumns;
	}
	
	public AttackNodeAnalysisSingle() {
		this(MAX_HTL_COLUMNS);
	}

	public String printCSVGraphHeader() {
		StringBuilder b = new StringBuilder();
		b.append("Node Count,Peer Count,HTL,Set #");
		b.append(",Clustering Coefficient,Network Diameter,Avg Path Length");
		// b.append(",Theoretical Node Reach,Theoretical Node Reach w/ Clustering(T /w C)");
		for (int i = 1; i <= this.totalReachFields; i++)
			b.append(",(Theo Node Reach /w CC) hop " + i);
		// b.append(",Theoretical Node Reach w/ Clustering Method 2");
		for (int i = 1; i <= this.totalReachFields; i++)
			b.append(",Actual Node Reach Avg hop " + i);
		b.append(",Actual Node Reach Min,Actual Node Reach Max");
		b.append(",Actual Node Reach Median");
		return b.toString();
	}

	public String printCSVPairHeader(String runCase) {
		StringBuilder b = new StringBuilder();
		b.append(",," + runCase + " A," + runCase
				+ " B,Shortest Path HTL,Target Count");
		b.append(",Location Diff,A Actual Reach,B Actual Reach");
		return b.toString();
	}

	public String calcGraphStats(String nodeCount, String peerCount, int htl,
			String dataSetCount) throws Exception {

		StringBuilder b = new StringBuilder();

		// Id info
		b.append(nodeCount + ",");
		b.append(peerCount + ",");
		b.append(htl + ",");
		b.append(dataSetCount + ",");

		// Clustering Coefficient
		ClusteringCoefficient cCoeff = new ClusteringCoefficient();
		AttributeModel attributeModel = Lookup.getDefault().lookup(
				AttributeController.class).getModel();
		GraphModel graphModel = graph.getGraphModel();
		cCoeff.execute(graphModel, attributeModel);
		b.append(cCoeff.getAverageClusteringCoefficient() + ",");

		GraphDistance distance = new GraphDistance();
		distance.execute(graphModel, attributeModel);
		b.append(distance.getDiameter() + 1 + ",");
		b.append(distance.getPathLength() + 1 + ",");

		// theoretical node reach
		double[] theoReach = this.calcTheoreticalReach(htl, peerCount, cCoeff
				.getAverageClusteringCoefficient());
		for (int i = 0; i < this.totalReachFields; i++) {
			if(i < theoReach.length)
				b.append(theoReach[i]);
			b.append(",");
		}

		// b.append(this.calcTheoreticalReach2(htl, peerCount, cCoeff
		// .getAverageClusteringCoefficient())
		// + ",");

		// actual node reach
		int[][] reaches = new int[graph.getNodeCount()][htl];
		Iterator<Node> iter = graph.getNodes().iterator();
		int count = 0;
		while (iter.hasNext()) {
			Node n = iter.next();
			reaches[count] = this.calcActualReach(graph, n.toString(), htl);
			count++;
		}
		
		// Process each column
		Integer[] column = null;
		for( int i = 0; i < this.totalReachFields; i++){
			if(i < htl){
				column = new Integer[reaches.length];
				int total = 0;
				for(int j =0; j < reaches.length; j++){
					column[j] = reaches[j][i];
					total += column[j];
				}
				b.append((double) total / (double) reaches.length);
			}
			b.append(",");
		}

		Arrays.sort(column);
		b.append(column[0] + ",");
		b.append(column[column.length - 1] + ",");

		double median;
		int mid = column.length / 2;
		if (column.length % 2 == 1)
			median = column[mid];
		else
			median = (column[mid] + column[mid - 1]) / 2.0;
		b.append(median);

		return b.toString();
	}

	public String calcSinglePairStats(String nodeTextEntry, int htl,
			String targetCount) throws Exception {
		StringBuilder b = new StringBuilder();

		NodePair p = this.parseTwoNodes(nodeTextEntry);

		b.append(",,");
		b.append(p + ",");
		b.append(this.findShortestPaths(graph, p.getA(), p.getB()) + ",");
		b.append(targetCount + ",");
		b.append(p.getDifference() + ",");

		int[] reach = this.calcActualReach(graph, p.getA(), htl);
		b.append(reach[reach.length-1] + ",");
		
		reach = this.calcActualReach(graph, p.getB(), htl);
		b.append(reach[reach.length-1]);

		return b.toString();
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

	private double[] calcTheoreticalReach(int htl, String peerCount,
			double clusterCoeff) {
		double[] values = new double[htl];
		int pCount = Integer.parseInt(peerCount);
		int k = pCount - 1;
		double cc = (1 - clusterCoeff);
		for (int i = 1; i <= htl; i++)
			values[i-1] = ((((Math.pow(k * cc, i - 1) - 1) / ((k * cc) - 1)) * ((k + 1))) + 1);
		return values;
	}

	// private double calcTheoreticalReach2(String htl, String peerCount,
	// double clusterCoeff) {

	// int h = Integer.parseInt(htl);
	// int pCount = Integer.parseInt(peerCount);
	// double cc = (1 - clusterCoeff);
	// return calcTheoreticalReach2(h-1, pCount - 1, cc) * pCount +1;
	// }

	//private double calcTheoreticalReach2(int htl, int k, double cc) {
	//	if (htl == 0)
	//		return 1;
	//	return Math.pow(k * Math.pow(cc, htl), htl)
	//			+ calcTheoreticalReach2(htl - 1, k, cc);
	//}

	private int[] calcActualReach(DirectedGraph graph, String nodeName, int htl) {

		Node node = graph.getNode(nodeName);
		List<Node> visited = new ArrayList<Node>();
		List<Node> active = new ArrayList<Node>();
		
		int[] reach = new int[htl];

		active.add(node);
		visited.add(node);
		reach[0] = visited.size();

		for (int i = 1; i < htl; i++) {
			List<Node> neighbors = new ArrayList<Node>();
			for (Node n : active) {
				for (Node n1 : graph.getNeighbors(n)) {
					if (!visited.contains(n1)) {
						visited.add(n1);
						neighbors.add(n1);
					}
				}
			}
			active = neighbors;
			reach[i] = visited.size();
		}

		return reach;
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
}
