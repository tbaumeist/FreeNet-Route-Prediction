package frp.routing;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import frp.routing.itersection.InsertNodeIntersections;
import frp.routing.itersection.Intersection;
import frp.routing.itersection.RequestNodeIntersections;
import frp.routing.itersection.SubRangeIntersections;
import frp.utils.Pair;

public class RoutingManager {

	private NetworkRouter networkRouter;
	private int maxHTL, resetHTL;

	private static final int INSERT_HOP_RESET = 3;

	public RoutingManager(int maxHTL){
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

	public List<PathSet[]> calculateRoutesFromNodes(
			List<Pair<Double, String>> startNodes, Topology top,
			boolean isInsertPath) throws Exception {
		List<PathSet[]> pathSets = new ArrayList<PathSet[]>();
		startNodes = checkStartNodes(startNodes, top);

		for (Pair<Double, String> startNode : startNodes) {
			pathSets.add(calculateRoutesFromNode(startNode.getFirst(),
					startNode.getSecond(), top, isInsertPath));
		}
		return pathSets;
	}

	public List<Intersection> calculateNodeIntersections(
			List<Pair<Double, String>> startNodes, Topology top,
			String outputFileName) throws Exception {

		startNodes = checkStartNodes(startNodes, top);
		List<InsertNodeIntersections> nodeIntersects = new ArrayList<InsertNodeIntersections>();

		List<PathSet[]> pathInsertSets = calculateRoutesFromNodes(startNodes,
				top, true);
		List<PathSet[]> pathRequestSets = calculateRoutesFromNodes(startNodes,
				top, false);

		// save the prediction paths
		if (outputFileName != null && !outputFileName.isEmpty()) {
			File out = new File(outputFileName);
			String absolutePath = out.getAbsolutePath();
			String path = absolutePath.substring(0,absolutePath.lastIndexOf(File.separator));
			
			// output the insert paths
			File outputInsertFile = new File(path + File.separator + "insert." + out.getName());
			PrintStream insertWriter = new PrintStream(outputInsertFile);
			for (PathSet[] sArray : pathInsertSets) {
				for (PathSet s : sArray)
					insertWriter.println(s);
			}
			insertWriter.close();

			// output the request paths
			File outputRequestFile = new File(path + File.separator + "request." + out.getName());
			PrintStream requestWriter = new PrintStream(outputRequestFile);
			for (PathSet[] sArray : pathRequestSets) {
				for (PathSet s : sArray)
					requestWriter.println(s);
			}
			requestWriter.close();
		}

		for (Node n : top.getAllNodes()) {
			nodeIntersects.add(new InsertNodeIntersections(n, pathInsertSets,
					pathRequestSets, this.resetHTL));
		}

		// copy to intersections objects
		List<Intersection> intersections = new ArrayList<Intersection>();
		for (InsertNodeIntersections insert : nodeIntersects) {
			for (SubRangeIntersections subRange : insert
					.getSubRangeIntersections()) {
				for (RequestNodeIntersections request : subRange
						.getRequestNodeIntersects()) {
					intersections.add(new Intersection(insert, subRange,
							request, this.maxHTL, this.resetHTL));
				}
			}
		}

		// merge similar intersection points together to reduce output
		// of duplicate entries
		for (int i = intersections.size() - 1; i >= 0; i--) {
			for (int j = i - 1; j >= 0; j--) {

				Intersection iInter = intersections.get(i);
				Intersection jInter = intersections.get(j);
				if (iInter == null || jInter == null)
					continue;

				// Check that j request path is a subset of i request path
				if (iInter.canMerge(jInter)) {
					// remove i 
					intersections.remove(i);
					break;
				}
			}
		}

		return intersections;
	}

	private List<Pair<Double, String>> checkStartNodes(
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
