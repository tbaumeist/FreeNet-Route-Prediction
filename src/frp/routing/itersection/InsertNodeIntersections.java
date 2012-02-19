package frp.routing.itersection;

import java.util.ArrayList;
import java.util.List;
import frp.routing.*;

public class InsertNodeIntersections {
	private Node node;
	private List<SubRangeIntersections> locIntersects = new ArrayList<SubRangeIntersections>();

	public InsertNodeIntersections(Node n, List<PathSet[]> pathInsertSet, List<PathSet[]> pathRequestSets, int hopReset) {
		this.node = n;

		for( PathSet[] psArr : pathInsertSet ){
			if(psArr.length < 1)
				continue;
			if(!psArr[0].getStartNode().equals(n))
				continue;
			for( PathSet ps : psArr){
				for (Path p : ps.getPaths()) {
					this.locIntersects.add(new SubRangeIntersections(p, pathRequestSets, hopReset));
				}
			}
		}
		
	}

	public List<SubRangeIntersections> getSubRangeIntersections() {
		return this.locIntersects;
	}
	
	public Node getStartNode(){ return this.node;}

	@Override
	public String toString() {
		String s = "Insert Node " + this.node;

		for (SubRangeIntersections loc : this.locIntersects) {
			s += "\n\t" + loc;
		}

		return s;
	}
}

