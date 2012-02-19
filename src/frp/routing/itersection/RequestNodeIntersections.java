package frp.routing.itersection;


import java.util.*;
import frp.routing.*;

public class RequestNodeIntersections implements Comparable<Object> {
	private Path requestPath;
	private double confidence;
	private SubRange intersectionRange;
	private List<Node> possibleTargetNodes = new ArrayList<Node>();

	public RequestNodeIntersections(Path insertPath, Path requestPath,
			Node intersectNode) {
		this.requestPath = requestPath;
		this.confidence = insertPath.getPathConfidence() * requestPath.getPathConfidence();
		this.intersectionRange = requestPath.getRange().getIntersection(insertPath.getRange());
		for (Node n : requestPath.getNodes()) {
			if (n.equals(intersectNode))
				break;
			if (n.equals(getStartNode()))
				continue;
			this.possibleTargetNodes.add(n);
		}
	}

	public Node getStartNode() {
		return this.requestPath.getStartNode();
	}

	public List<Node> getPossibleTargetNodes() {
		return this.possibleTargetNodes;
	}

	@Override
	public String toString() {
		String s = "Intersect Range: " + this.intersectionRange + ", Request Node: " + getStartNode();
		s += ", Request Range: " + this.requestPath.getRange();
		s += ", Path: " + this.requestPath.toStringSimple();
		s += ", Target Nodes: {";
		for (Node n : this.possibleTargetNodes) {
			s += n + ", ";
		}

		s += "}, Confidence: " + this.confidence;

		return s;
	}
	

	@Override
	public int compareTo(Object obj) {
		if (obj == null)
			return 1;
		if (!(obj instanceof RequestNodeIntersections))
			return 1;

		RequestNodeIntersections node = (RequestNodeIntersections) obj;
		return new Double(node.confidence).compareTo(new Double(this.confidence));
	}
}
