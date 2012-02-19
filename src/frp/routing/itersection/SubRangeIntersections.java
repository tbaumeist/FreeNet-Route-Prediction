package frp.routing.itersection;

import java.util.*;
import frp.routing.*;
import frp.utils.ListTools;

public class SubRangeIntersections {
	private Path insertPath;
	private List<Node> insertionNodes;
	private double confidence;
	private List<RequestNodeIntersections> requestNodes = new ArrayList<RequestNodeIntersections>();

	public SubRangeIntersections(Path insertPath, List<PathSet[]> pathRequestSets, int hopReset) {
		this.insertPath = insertPath;
		this.insertionNodes = insertPath.getProbableStoreNodes(hopReset);
		this.confidence = insertPath.getPathConfidence();
		construct(pathRequestSets);
	}

	public List<RequestNodeIntersections> getRequestNodeIntersects() {
		return this.requestNodes;
	}

	public SubRange getRange() {
		return this.insertPath.getRange();
	}

	@Override
	public String toString() {
		String s = "Range: " + getRange() + ", Storage Nodes: {";
		for(Node n : this.insertionNodes)
			s += n;
		s += "}, Confidence: " + this.confidence;
		s += ", Path: " + this.insertPath.toStringSimple();

		for (RequestNodeIntersections req : this.requestNodes) {
			s += "\n\t\t" + req;
		}

		if (this.requestNodes.isEmpty())
			s += "\n\t\tNo request paths found";

		return s;
	}

	private void construct(List<PathSet[]> pathRequestSets) {

		for (PathSet[] psRequestArray : pathRequestSets) {
			if(psRequestArray.length < 1)
				continue;
			if (psRequestArray[0].getStartNode().equals(this.insertPath.getStartNode()))
				continue;
			
			for (PathSet psRequest : psRequestArray) {
				// Request path does not start on a storage node
				// ***NOTE: this could be removed if we controlled the request node
				// and forced it to ignore local storage
				if (this.insertionNodes.contains(psRequest.getStartNode()))
					continue;

				for (Path p : psRequest.getPaths()) {
					List<Node> nodes = p.getNodes();
					// insert & request path intersect
					List<Node> inter = ListTools.intersect(nodes, this.insertionNodes);
					if(inter.isEmpty())
						continue;
					// **Note: inter[0] has the first intersect node. Only really care about that one.
					
					// insert & request path sub-range overlap
					if (!getRange().overlaps(p.getRange()))
						continue;
					
					if (!hasTargetNodes(this.insertPath, p, this.insertionNodes))
						continue;
					if (!isUniquePath(this.insertPath, p, this.insertionNodes))
						continue;

					this.requestNodes.add(new RequestNodeIntersections(
							this.insertPath, p, inter.get(0)));
				}
			}
		}
		Collections.sort(this.requestNodes);
	}

	private boolean hasTargetNodes(Path insertPath, Path requestPath,
			List<Node> intersects) {
		List<Node> nodes = requestPath.getNodes();
		// need at least 3 nodes in request path
		if (nodes.size() <= 2)
			return false;
		// the second node in request path is not a storage node
		if (intersects.contains(nodes.get(1)))
			return false;
		return true;
	}

	private boolean isUniquePath(Path insertPath, Path requestPath,
			List<Node> intersects) {
		for (Node n : requestPath.getNodes()) {
			if (intersects.contains(n))
				return true;

			if (insertPath.getNodes().contains(n))
				return false;
		}
		return true;
	}
}

