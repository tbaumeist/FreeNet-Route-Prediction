package frp.routing;

import java.util.ArrayList;
import java.util.List;

import frp.utils.CircleList;

public class Path implements Comparable<Object> {

	private List<SubRange> ranges = new ArrayList<SubRange>();
	private List<Integer> htls = new ArrayList<Integer>();
	private SubRange range = null;
	// private double preference = 0;
	private boolean successful = true;

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
		for (int i = 0 ; i < this.ranges.size(); i++){
			if(this.htls.get(i) <= 0)
				break;
			SubRange rr = this.ranges.get(i);
			nodes.add(rr.getNode());
		}
			
		return nodes;
	}
	
	public List<Node> getExtraNodes() {
		List<Node> nodes = new CircleList<Node>();
		for (int i = 0 ; i < this.ranges.size(); i++){
			if(this.htls.get(i) > 0)
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
	
	public int getTieCountToNode(Node stopNode, boolean includeProbStore) {
		int tie = 0;
		int size = 0;
		for(int i =0 ; i < this.ranges.size();i++){
			
			// stop if not including the probably storage nodes
			if(!includeProbStore && this.htls.get(i) <= 0)
				break;
			
			SubRange rr = this.ranges.get(i);
			tie += rr.getTieCount();
			size++;

			if (rr.getNode().equals(stopNode))
				break;
		}
		return tie - size + 1;
	}
	
public List<Node> getProbableStoreNodes(int hopReset) {
		
		List<Node> nodes = new ArrayList<Node>();
		for (int i = 0; i < this.htls.size(); i++) {
			// htl is positive
			// htl is <= hopReset
			// it is not the first node in path
			if (this.htls.get(i) > 0 && i > 0 &&
					this.htls.get(i) <= hopReset) // first node with htl of 1
				nodes.add( this.ranges.get(i).getNode() );
		}

		return nodes;
	}

	public List<String> getProbableStoreNodeIds(int hopReset) {
		
		List<Node> nodes = getProbableStoreNodes(hopReset);
		List<String> nodeIds = new ArrayList<String>();
		
		for(Node n : nodes){
			nodeIds.add(n.getID());
		}

		return nodeIds;
	}

	public Node getStartNode() {
		if (this.ranges.isEmpty())
			return null;
		return this.ranges.get(0).getNode();
	}

	public boolean equalPath(Path cmp) {
		if (cmp == null)
			return false;
		if (this.getNodes().size() != cmp.getNodes().size())
			return false;
		for (int i = 0; i < this.getNodes().size(); i++) {
			if (!this.getNodes().get(i).getID().equals(
					cmp.getNodes().get(i).getID())) {
				return false;
			}
		}
		return true;
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
		String out = "";
		if (!getSuccess())
			out += "FAILED ";
		out += "| 1/" + getTieCount(false) + " " + 1.0 / getTieCount(false) + " | ";
		out += "| w/ extra storage 1/" + getTieCount(true) + " " + 1.0 / getTieCount(true) + " | ";
		out += getRange() + " -> ";
		for (int i = 0; i < this.ranges.size(); i++) {
			out += this.ranges.get(i).getNode() + "(Tie="
					+ this.ranges.get(i).getTieCount() + ")(HTL="
					+ this.htls.get(i) + "), ";
		}

		return out;
	}

	public String toStringSimple() {
		String out = "";

		for (int i = 0; i < this.ranges.size(); i++) {
			if(this.htls.get(i) <= 0)
				break;
			out += "("+this.htls.get(i)+")"+this.ranges.get(i).getNode().getID() + ", ";
		}

		return out;
	}
	
	public String toStringExtraNodesSimple() {
		String out = "";

		for (int i = 0; i < this.ranges.size(); i++) {
			if(this.htls.get(i) > 0)
				continue;
			out += this.ranges.get(i).getNode().getID() + ", ";
		}

		return out;
	}
}

