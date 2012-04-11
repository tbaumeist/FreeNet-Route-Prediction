package frp.routing.itersection;

import java.util.List;

import frp.routing.Node;
import frp.routing.SubRange;

public class Intersection implements Comparable<Intersection> {
	private InsertNodeIntersections insert;
	private SubRangeIntersections subRange;
	private RequestNodeIntersections request;
	private double confidence = 0.0;

	public Intersection(InsertNodeIntersections insert,
			SubRangeIntersections subRange, RequestNodeIntersections request,
			int maxHTL, int hopReset) {
		this.insert = insert;
		this.subRange = subRange;
		this.request = request;
		// custom confidence value, request confidence * (length of paths)
		// shorter the better
		double penalty = 1.0 / (double) (maxHTL + 1);
		int insertLength = this.subRange
				.getInsertPath()
				.getPathUpToCacheAbleNode(this.request.getIntersectNode(),
						hopReset).size();
		int requestLength = this.request
				.getRequestPath()
				.getPathUpToCacheAbleNode(this.request.getIntersectNode(),
						hopReset).size();
		this.confidence = request.getConfidence()
				* (1 - (penalty * insertLength))
				* (1 - (penalty * requestLength));
	}

	public SubRange getIntersectSubRange() {
		return this.request.getIntersectSubRange();
	}

	public Node getInsertStartNode() {
		return this.insert.getStartNode();
	}

	public Node getRequestStartNode() {
		return this.request.getStartNode();
	}

	public List<Node> getPossibleTargetNodes() {
		return this.request.getPossibleTargetNodes();
	}

	public double getConfidence() {
		return this.confidence;
	}

	@Override
	public int compareTo(Intersection o) {
		return new Double(this.getConfidence()).compareTo(new Double(o
				.getConfidence())) * -1;
	}

	@Override
	public String toString() {
		String s = "Confidence: " + this.getConfidence();
		s += ", Insert: " + this.getInsertStartNode();
		s += ", Request: " + this.getRequestStartNode();
		s += ", Targets:";
		for (Node n : this.request.getPossibleTargetNodes())
			s += n + " ";
		s += ", Intersect: " + this.request.getIntersectNode();
		s += ", Range: " + this.getIntersectSubRange();
		s += ", Insert Path: " + this.subRange.getInsertPath().toStringSimple();
		s += ", Request Path: "
				+ this.request.getRequestPath().toStringSimple();

		return s;
	}
}
