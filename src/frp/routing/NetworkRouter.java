package frp.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NetworkRouter {

	public NetworkRouter() {

	}

	public List<Path> findPaths(int resetHTL, int htl, Topology top,
			double startNode, String startNodeId, boolean isInsertPath)
			throws Exception {

		Node start = top.findNode(startNode, startNodeId);
		if (start == null)
			throw new Exception("Unable to find specified start node");

		List<Path> paths = new ArrayList<Path>();
		List<Node> visited = new ArrayList<Node>();
		Path currentPath = new Path();
		RouteRange startRange = new RouteRange(start, 0, 0);
		int resetHop = -1;

		if (isInsertPath) // reset hop only used for inserts
			resetHop = resetHTL;

		visited.add(start);
		currentPath.addNodeAsRR(startRange, htl);

		_findPaths(paths, currentPath, visited, startRange, htl - 1, resetHop);

		currentPath.removeLastNode();
		assert (currentPath.getNodes().isEmpty());

		return paths;
	}

	private boolean _findPaths(List<Path> paths, Path currentPath,
			List<Node> visited, RouteRange range, int hopsToLive, int resetHop)
			throws Exception {

		currentPath.setRange(range);
		// stop when htl expires
		// stop when the next closest node is the same as the previous node
		// (self route)
		if (shouldStop(hopsToLive) || range.isSelfRoute()) {
			paths.add(currentPath.clone());
			return true;
		}
		List<Node> oldVisited = null;
		if (hopsToLive > 0 && hopsToLive == resetHop) {
			oldVisited = visited;
			visited = new ArrayList<Node>();
			visited.add(range.getNode());
		}

		// List<RouteRange> allRanges = getRanges(range, visited,
		// hopsToLive < resetHop);
		List<RouteRange> allRanges = getRanges(range, visited, false);

		int pathsFound = 0;

		for (RouteRange rr : allRanges) {
			if (range.overlaps(rr)) {
				int hopMod = 0;
				if (rr.getIsRetry() && hopsToLive <= resetHop) {
					hopMod--;
				}
				pathsFound++;
				visited.add(rr.getNode());
				currentPath.addNodeAsRR(rr, hopsToLive + hopMod);
				if (_findPaths(paths, currentPath, visited, rr, hopsToLive - 1
						+ hopMod, resetHop)) {

					removeFromEndUpTo(visited, rr.getNode());
				}
				currentPath.removeLastNode();
			}
		}
		if (pathsFound == 0) {
			Path failed = currentPath.clone();
			failed.setSuccess(false);
			paths.add(failed);
		}

		if (oldVisited != null)
			visited = oldVisited;

		return pathsFound > 0;
	}

	private void removeFromEndUpTo(List<Node> visited, Node n) {
		for (int i = visited.size() - 1; i >= 0; i--) {
			boolean found = visited.get(i).equals(n);
			visited.remove(i);
			if (found)
				return;
		}
	}

	private List<RouteRange> getRangesSimple(RouteRange range,
			List<Node> visited, boolean includeSelf) {

		List<RouteRange> ranges = range.getNode().getPathsOut(visited,
				includeSelf);
		ranges = splitRanges(range, ranges);

		return ranges;
	}

	private List<RouteRange> getRanges(RouteRange range, List<Node> visited,
			boolean includeSelf) {

		List<RouteRange> ranges = getRangesSimple(range, visited, includeSelf);
		List<Node> visitedOnlyMe = new ArrayList<Node>();
		if (visited.size() > 1) // prev node
			visitedOnlyMe.add(visited.get(visited.size() - 2));
		visitedOnlyMe.add(range.getNode()); // current node
		List<RouteRange> allRanges = getRangesSimple(range, visitedOnlyMe,
				includeSelf);

		for (RouteRange rr : allRanges) {
			if (visited.contains(rr.getNode()))
				ranges = splitRanges(rr, ranges);
		}

		for (RouteRange rr : ranges) {
			for (RouteRange rr2 : allRanges) {
				if (visited.contains(rr2.getNode())) {
					if (rr.overlaps(rr2)) {
						rr.setIsRetry(true);
					}
				}
			}
		}

		return ranges;
	}

	private boolean shouldStop(int hopsToLive) {
		// return hopsToLive <= 0;
		return hopsToLive <= -4; // include 4 additional probable storage nodes
	}

	private List<RouteRange> splitRanges(RouteRange range,
			List<RouteRange> ranges) {

		List<RouteRange> newRanges = new ArrayList<RouteRange>();

		for (RouteRange rr : ranges) {
			if (range.overlaps(rr)) {

				newRanges.addAll(range.splitRangeOverMe(rr));
			} else {
				newRanges.add(rr);
			}
		}
		Collections.sort(newRanges);
		return newRanges;
	}

	// private boolean noDuplicatePaths(List<Path> paths) {
	// for (int i = 0; i < paths.size() - 1; i++) {
	// for (int j = i + 1; j < paths.size(); j++) {
	// if (paths.get(i).getRange().overlaps(paths.get(j).getRange()))
	// return false;
	// }
	// }
	// return true;
	// }

}
