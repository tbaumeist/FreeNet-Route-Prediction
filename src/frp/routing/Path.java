package frp.routing;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import frp.utils.CircleList;

public class Path implements Comparable<Object> {

	private List<SubRange> ranges = new ArrayList<SubRange>();
	private List<Integer> htls = new ArrayList<Integer>();
	private SubRange range = null;
	// private double preference = 0;
	private boolean successful = true;

	private final DecimalFormat decFormat = new DecimalFormat("0.00000");

	public void setSuccess(boolean b) {
		this.successful = b;
	}

	public boolean getSuccess() {
		return this.successful;
	}

	public void setRange(SubRange range) {
		this.range = range;
	}

	public SubRange getRange() {
		return this.range;
	}

	public void addNodeAsRR(SubRange r, int htl) {
		// if(this.nodes.contains(n))
		// return;
		this.ranges.add(r);
		this.htls.add(htl);
	}

	public void removeLastNode() {
		this.ranges.remove(this.ranges.size() - 1);
		this.htls.remove(this.htls.size() - 1);
	}

	public List<Node> getNodes() {
		List<Node> nodes = new CircleList<Node>();
		for (int i = 0; i < this.ranges.size(); i++) {
			if (this.htls.get(i) <= 0)
				break;
			SubRange rr = this.ranges.get(i);
			nodes.add(rr.getNode());
		}

		return nodes;
	}

	public List<Node> getExtraNodes() {
		List<Node> nodes = new CircleList<Node>();
		for (int i = 0; i < this.ranges.size(); i++) {
			if (this.htls.get(i) > 0)
				continue;
			SubRange rr = this.ranges.get(i);
			nodes.add(rr.getNode());
		}
		return nodes;
	}

	public Path clone() {
		Path p = new Path();
		p.ranges.addAll(this.ranges);
		p.htls.addAll(this.htls);
		p.range = this.range;
		return p;
	}

	public double getPathConfidence() {
		return 1.0 / (double) getTieCount(false);
	}

	public double getPathConfidenceWithProbStoreNodes() {
		return 1.0 / (double) getTieCount(true);
	}

	public int getTieCount(boolean includeProbStore) {
		return getTieCountToNode(null, includeProbStore);
	}

	private int getTieCountToNode(Node stopNode, boolean includeProbStore) {
		int tie = 0;
		int size = 0;
		for (int i = 0; i < this.ranges.size(); i++) {

			// stop if not including the probably storage nodes
			if (!includeProbStore && this.htls.get(i) <= 0)
				break;

			SubRange rr = this.ranges.get(i);
			tie += rr.getTieCount();
			size++;

			if (rr.getNode().equals(stopNode))
				break;
		}
		return tie - size + 1;
	}
	
	/*
	 * Check if either path is a sub set of the other
	 */
	public boolean isPathSubset(Path p, Node stopNode){
		List<Node> myPath = this.getPathUpToCacheAbleNode(stopNode, -1);
		List<Node> theirPath = this.getPathUpToCacheAbleNode(stopNode, -1);
		
		if(myPath.size() != theirPath.size())
			return false;
		
		for( int i =0; i < myPath.size(); i++){
			if(!myPath.get(i).equals(theirPath.get(i)))
				return false;
		}
		
		return true;
	}

	public List<Node> getPathUpToCacheAbleNode(Node stop, int hopReset) {
		List<Node> nodes = new ArrayList<Node>();
		for (int i = 0; i < this.ranges.size(); i++) {
			SubRange rr = this.ranges.get(i);
			nodes.add(rr.getNode());
			if (i > 0 && this.htls.get(i) <= hopReset
					&& rr.getNode().equals(stop))
				break;
		}
		return nodes;
	}

	public List<Node> getProbableStoreNodes(int hopReset) {

		List<Node> nodes = new ArrayList<Node>();
		for (int i = 0; i < this.htls.size(); i++) {
			// htl is positive
			// htl is <= hopReset
			// it is not the first node in path
			if (this.htls.get(i) > 0 && i > 0 && this.htls.get(i) <= hopReset)
				nodes.add(this.ranges.get(i).getNode());
			// first node with htl of 1

		}

		return nodes;
	}

	public List<String> getProbableStoreNodeIds(int hopReset) {

		List<Node> nodes = getProbableStoreNodes(hopReset);
		List<String> nodeIds = new ArrayList<String>();

		for (Node n : nodes) {
			nodeIds.add(n.getID());
		}

		return nodeIds;
	}

	public Node getStartNode() {
		if (this.ranges.isEmpty())
			return null;
		return this.ranges.get(0).getNode();
	}

	private boolean equalPath(Path cmp) {
		if (cmp == null)
			return false;
		if (this.getNodes().size() != cmp.getNodes().size())
			return false;
		for (int i = 0; i < this.getNodes().size(); i++) {
			if (!this.getNodes().get(i).equals(cmp.getNodes().get(i))) {
				return false;
			}
			if (this.ranges.get(i).getTieCount() != cmp.ranges.get(i)
					.getTieCount()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof Path))
			return false;

		Path r = (Path) obj;
		return this.equalPath(r);
	}

	@Override
	public int compareTo(Object obj) {
		if (obj == null)
			return 1;
		if (!(obj instanceof Path))
			return 1;

		Path r = (Path) obj;
		return getRange().compareTo(r.getRange());
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		if (!getSuccess())
			out.append("FAILED ");

		out.append("| 1/");
		out.append(getTieCount(false));
		out.append(" ");
		out.append( this.decFormat.format( 1.0 / getTieCount(false) ));
		out.append(" | ");

		// out += "| w/ extra storage 1/" + getTieCount(true) + " " + 1.0
		// / getTieCount(true) + " | ";

		out.append(getRange());
		out.append(" -> ");

		out.append(toStringSimpleFillPath());

		return out.toString();
	}
	
	public String toStringSimpleFillPath(){
		StringBuilder out = new StringBuilder();
		out.append(toStringSimple());
		out.append(">>EXTRA>> ");
		out.append(toStringExtraNodesSimple());

		return out.toString();
	}

	public String toStringSimple() {
		StringBuilder out = new StringBuilder();

		for (int i = 0; i < this.ranges.size(); i++) {
			if (this.htls.get(i) <= 0)
				break;
			out.append(this.ranges.get(i).getNode().getID());
			out.append("(");
			out.append(this.htls.get(i));
			out.append("), ");
		}

		return out.toString();
	}

	public String toStringExtraNodesSimple() {
		StringBuilder out = new StringBuilder();

		for (int i = 0; i < this.ranges.size(); i++) {
			if (this.htls.get(i) > 0)
				continue;
			out.append(this.ranges.get(i).getNode().getID());
			out.append("(");
			out.append(this.htls.get(i));
			out.append("), ");
		}

		return out.toString();
	}
}
