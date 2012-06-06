package frp.routing.itersection;

import java.util.ArrayList;
import java.util.List;
import frp.routing.*;

public class InsertNodeIntersections {
	private Node node;
	private List<SubRangeIntersections> locIntersects = new ArrayList<SubRangeIntersections>();

	public InsertNodeIntersections(Node n) {
		this.node = n;
	}

	public void calculateIntersection(PathSet pathInsertSet,
			List<PathSet[]> pathRequestSets, int hopReset) {

		if (!pathInsertSet.getStartNode().equals(this.node))
			return;
		for (Path p : pathInsertSet.getPaths()) {
			this.locIntersects.add(new SubRangeIntersections(p,
					pathRequestSets, hopReset));
		}
	}

	public List<SubRangeIntersections> getSubRangeIntersections() {
		return this.locIntersects;
	}

	public Node getStartNode() {
		return this.node;
	}

	@Override
	public String toString() {
		String s = "Insert Node " + this.node;

		for (SubRangeIntersections loc : this.locIntersects) {
			s += "\n\t" + loc;
		}

		return s;
	}
}
