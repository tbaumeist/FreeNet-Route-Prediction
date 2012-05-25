package frp.routing.itersection;

import java.util.*;
import frp.routing.*;
import frp.utils.ListTools;

public class SubRangeIntersections {
	private Path insertPath;
	private List<Node> dataStorageNodes;
	private double confidence;
	private List<RequestNodeIntersections> requestNodes = new ArrayList<RequestNodeIntersections>();

	public SubRangeIntersections(Path insertPath,
			List<PathSet[]> pathRequestSets, int hopReset) {
		this.insertPath = insertPath;
		this.dataStorageNodes = insertPath.getProbableStoreNodes(hopReset);
		this.confidence = insertPath.getPathConfidence();
		construct(pathRequestSets);
	}

	public Path getInsertPath() {
		return this.insertPath;
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
		for (Node n : this.dataStorageNodes)
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

		//short circuit
		if(this.dataStorageNodes.isEmpty())
			return;
		
		for (PathSet[] psRequestArray : pathRequestSets) {
			if (psRequestArray.length < 1)
				continue;
			if (psRequestArray[0].getStartNode().equals(
					this.insertPath.getStartNode()))
				continue;

			for (PathSet psRequest : psRequestArray) {
				// Request path does not start on a storage node
				// ***NOTE: this could be removed if we controlled the request
				// node
				// and forced it to ignore local storage
				if (this.dataStorageNodes.contains(psRequest.getStartNode()))
					continue;

				for (Path p : psRequest.getPaths()) {
					List<Node> nodes = p.getNodes();
					// insert & request path intersect
					List<Node> inter = ListTools.intersect(nodes,
							this.dataStorageNodes);
					if (inter.isEmpty())
						continue;
					// **Note: inter[0] has the first intersect node. Only
					// really care about that one.

					// insert & request path sub-range overlap
					if (!getRange().overlaps(p.getRange()))
						continue;

					if (!hasTargetNodes(this.insertPath, p,
							this.dataStorageNodes))
						continue;
					if (!isUniquePath(this.insertPath, p, this.dataStorageNodes))
						continue;

					RequestNodeIntersections r = new RequestNodeIntersections(
							this.insertPath, p, inter.get(0));

					// Request Node = Announce Node Change: The request node
					// is not a direct peer of at least one of the target nodes
					if (r.targetNodeNotConnectedToRequestNodeExists())
						this.requestNodes.add(r);
				}
			}
		}
		Collections.sort(this.requestNodes);
	}

	private boolean hasTargetNodes(Path insertPath, Path requestPath,
			List<Node> intersects) {
		List<Node> nodes = requestPath.getNodes();
		// need at least 2 nodes in request path
		if (nodes.size() < 2)
			return false;

		// the second node in request path is not a storage node
		if (intersects.contains(nodes.get(1)))
			return false;
		return true;
	}

	private boolean isUniquePath(Path insertPath, Path requestPath,
			List<Node> intersects) {

		List<Node> insertNodes = insertPath.getNodes();

		// Check that the EXTRA nodes on the insert path don't overlap
		// in addition to the regular nodes
		insertNodes.addAll(insertPath.getExtraNodes());

		for (Node n : requestPath.getNodes()) {
			if (intersects.contains(n))
				return true;

			if (insertNodes.contains(n))
				return false;
		}
		return true;
	}
}
