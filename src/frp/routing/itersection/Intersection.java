package frp.routing.itersection;

import java.util.List;

import frp.routing.Node;
import frp.routing.SubRange;
import frp.utils.DistanceTools;

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

	public boolean canMerge(Intersection i) {
		if (i == null)
			return false;

		// check if the insert, request, and intersect points match
		// between I and J
		// Entries are already grouped together because of processing
		// order
		// so the merging can be shortcut
		if (!this.equals(i)) {
			return false;
		}

		return this.request.getRequestPath().isPathSubset(
				i.request.getRequestPath(),
				this.getIntersectSubRange().getNode());
	}

	@Override
	public int compareTo(Intersection o) {
		int cmpNode = this.getInsertStartNode().compareTo(o.getInsertStartNode());
		if(cmpNode != 0)
			return cmpNode;
		return new Double(this.getConfidence()).compareTo(new Double(o
				.getConfidence())) * -1;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof Intersection))
			return false;
		Intersection i = (Intersection) obj;

		if (!this.getInsertStartNode().equals(i.getInsertStartNode())
				|| !this.getRequestStartNode().equals(i.getRequestStartNode())
				|| !this.getIntersectSubRange()
						.equals(i.getIntersectSubRange())
				|| !this.request.getIntersectNode().equals(i.request.getIntersectNode())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("Confidence: ");
		s.append(DistanceTools.round(this.getConfidence()));

		s.append(", Insert: ");
		s.append(this.getInsertStartNode());

		s.append(", Request: ");
		s.append(this.getRequestStartNode());

		s.append(", Targets:");
		for (Node n : this.request.getPossibleTargetNodes()) {
			s.append(n);
			s.append(" ");
		}

		s.append(", Intersect: ");
		s.append(this.request.getIntersectNode());

		s.append(", Range: ");
		s.append(this.getIntersectSubRange());

		s.append(", Insert Path: ");
		s.append(this.subRange.getInsertPath().toStringSimpleFillPath());

		s.append(", Request Path: ");
		s.append(this.request.getRequestPath().toStringSimpleFillPath());

		return s.toString();
	}
}
