package frp.routing;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import frp.main.rti.analysis.AttackPair;
import frp.main.rti.prediction.RTIPrediction;
import frp.routing.itersection.InsertNodeIntersections;
import frp.routing.itersection.Intersection;
import frp.routing.itersection.RequestNodeIntersections;
import frp.routing.itersection.SubRangeIntersections;
import frp.utils.Pair;
import frp.utils.Progresser;

public class RoutingManager {

	private NetworkRouter networkRouter;
	private int maxHTL, resetHTL;

	private static final int INSERT_HOP_RESET = 3;

	protected RoutingManager() {

	}

	public RoutingManager(int maxHTL) {
		this(maxHTL, 1);
	}

	public RoutingManager(int maxHTL, int dhtl) {
		this.networkRouter = new NetworkRouter(dhtl);
		this.maxHTL = maxHTL;
		this.resetHTL = this.maxHTL - INSERT_HOP_RESET;
	}

	public int getResetHTL() {
		return this.resetHTL;
	}

	public List<PathSet[]> calculateRoutesFromNodes(Progresser prog,
			List<Pair<Double, String>> startNodes, Topology top,
			boolean isInsertPath) throws Exception {
		List<PathSet[]> pathSets = new ArrayList<PathSet[]>();
		startNodes = checkStartNodes(startNodes, top);

		for (Pair<Double, String> startNode : startNodes) {
			pathSets.add(calculateRoutesFromNode(startNode.getFirst(),
					startNode.getSecond(), top, isInsertPath));
			if (prog != null)
				prog.hit();
		}
		return pathSets;
	}

	public List<AttackPair> calculateNodeIntersections(
			List<Pair<Double, String>> startNodes, Topology top,
			String outputFileName) throws Exception {

		// //////////////////////////////////////////////////////////
		// //////////////////////////////////////////////////////////
		System.out.println("Calculating paths ...");

		startNodes = checkStartNodes(startNodes, top);

		Progresser prog = new Progresser(System.out, startNodes.size() * 2);
		List<PathSet[]> pathInsertSets = calculateRoutesFromNodes(prog,
				startNodes, top, true);

		// save the prediction paths
		savePathPredictions(outputFileName, "insert", pathInsertSets);

		// save the predicted insert paths
		File tmpStorage = File.createTempFile("RTI", "Insert");
		PathSet.savePathSetList(pathInsertSets, tmpStorage.getAbsolutePath());
		pathInsertSets = null;
		System.gc();

		List<PathSet[]> pathRequestSets = calculateRoutesFromNodes(prog,
				startNodes, top, false);

		// save the prediction paths
		savePathPredictions(outputFileName, "request", pathRequestSets);

		// //////////////////////////////////////////////////////////
		// //////////////////////////////////////////////////////////
		String outputInterFileName = null;
		if(outputFileName != null && !outputFileName.isEmpty())
			outputInterFileName = outputFileName + RTIPrediction.FILE_INTERSECTIONS_SUFFIX;
		List<AttackPair> pairs = calculateIntersections(top.getAllNodes(),
				tmpStorage.getAbsolutePath(), outputInterFileName, pathRequestSets,
				this.resetHTL);

		PathSet.removeAllSavedPathSetList(tmpStorage.getAbsolutePath());
		pathRequestSets = null;
		System.gc();

		return pairs;
	}

	private void savePathPredictions(String fileName, String postFix,
			List<PathSet[]> pathSets) throws Exception {
		if (fileName == null || fileName.isEmpty())
			return;

		File out = new File(fileName);
		String absolutePath = out.getAbsolutePath();
		String path = absolutePath.substring(0,
				absolutePath.lastIndexOf(File.separator));

		// output the request paths
		File outputFile = new File(path + File.separator + out.getName() + "."
				+ postFix);
		PrintStream writer = new PrintStream(outputFile);
		for (PathSet[] sArray : pathSets) {
			for (PathSet s : sArray)
				writer.println(s);
		}
		writer.close();

	}

	private List<AttackPair> calculateIntersections(List<Node> allNodes,
			String insertPathSetNameBase, String outputFileName,
			List<PathSet[]> pathRequestSets, int htlReset) throws Exception {

		Hashtable<AttackPair, AttackPair> pairs = new Hashtable<AttackPair, AttackPair>();
		AttackPair.generateAllAttackPairs(pairs, allNodes);

		System.out.println("Calculating intersections ...");
		Progresser progInter = new Progresser(System.out, allNodes.size()
				* this.maxHTL);

		PathSet.PathSetReader reader = PathSet
				.createPathSetReader(insertPathSetNameBase);

		File interStore = null;
		PrintStream interWriter = null;
		if (outputFileName != null && !outputFileName.isEmpty()) {
			interStore = new File(outputFileName);
			interWriter = new PrintStream(interStore);
		}

		Node startNode = null;
		InsertNodeIntersections currentInter = null;
		PathSet ps;
		while ((ps = reader.readNext()) != null) {

			// moved on to next start node
			if (!ps.getStartNode().equals(startNode)) {
				startNode = ps.getStartNode();
				if (currentInter != null)
					convertToIntersections(pairs, currentInter, interWriter);

				currentInter = new InsertNodeIntersections(startNode);
			}

			currentInter.calculateIntersection(ps, pathRequestSets, htlReset);

			progInter.hit();
		}

		if (currentInter != null)
			convertToIntersections(pairs, currentInter, interWriter);

		if(interWriter != null)
			interWriter.close();

		// Copy to a list, need ordering now
		List<AttackPair> allPairs = new ArrayList<AttackPair>();
		allPairs.addAll(pairs.values());
		Collections.sort(allPairs);

		return allPairs;
	}

	private void convertToIntersections(Hashtable<AttackPair, AttackPair> pairs, InsertNodeIntersections currentInter,
			PrintStream interWriter) {

		saveIntersections(pairs, interWriter, currentInter);
	}

	private void saveIntersections(Hashtable<AttackPair, AttackPair> pairs, PrintStream interWriter,
			InsertNodeIntersections currentInter) {
		// copy to intersections objects
		List<Intersection> intersections = new ArrayList<Intersection>();

		for (SubRangeIntersections subRange : currentInter
				.getSubRangeIntersections()) {
			for (RequestNodeIntersections request : subRange
					.getRequestNodeIntersects()) {
				intersections.add(new Intersection(currentInter, subRange,
						request, this.maxHTL, this.resetHTL));
			}
		}

		mergeAdjacentIntersections(intersections);

		Collections.sort(intersections);

		// write out intersections
		if (interWriter != null) {
			for (Intersection i : intersections) {
				interWriter.println(i);
			}
		}
		
		// add to attack pairs
		for (Intersection inter : intersections) {
			AttackPair pair = new AttackPair(inter.getInsertStartNode(),
					inter.getRequestStartNode());
			pair = pairs.get(pair);
			pair.addTargetNodes(inter.getPossibleTargetNodes());
		}
	}

	private void mergeAdjacentIntersections(List<Intersection> intersections) {
		for (int i = intersections.size() - 1; i >= 0; i--) {
			for (int j = i - 1; j >= 0; j--) {

				Intersection iInter = intersections.get(i);
				Intersection jInter = intersections.get(j);
				if (iInter == null || jInter == null)
					continue;

				// Short circuit check
				if (!iInter.equals(jInter))
					break;

				// Check that j request path is a subset of i request path
				if (iInter.canMerge(jInter)) {
					// remove i
					intersections.remove(i);
					break;
				}
			}
		}
	}

	protected List<Pair<Double, String>> checkStartNodes(
			List<Pair<Double, String>> startNodes, Topology top) {
		if (startNodes == null) { // all nodes
			startNodes = new ArrayList<Pair<Double, String>>();
			for (Node n : top.getAllNodes())
				startNodes.add(new Pair<Double, String>(n.getLocation(), n
						.getID()));
		}
		return startNodes;
	}

	private PathSet[] calculateRoutesFromNode(double startNode,
			String startNodeId, Topology top, boolean isInsertPath)
			throws Exception {
		PathSet[] pathSetByHTL = new PathSet[this.maxHTL];
		for (int i = 0; i < this.maxHTL; i++) {
			PathSet pathSet = new PathSet(top.findNode(startNode, startNodeId),
					i + 1);
			pathSet.addPaths(this.networkRouter.findPaths(this.resetHTL, i + 1,
					top, startNode, startNodeId, isInsertPath));
			pathSetByHTL[i] = pathSet;
		}
		return pathSetByHTL;
	}
}
