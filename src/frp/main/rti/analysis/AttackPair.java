package frp.main.rti.analysis;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import frp.routing.Node;
import frp.routing.itersection.Intersection;

public class AttackPair implements Comparable<Object> {

	public static Hashtable<AttackPair, AttackPair> extractAttackPairs(
			List<Intersection> intersections) {
		
		Hashtable<AttackPair, AttackPair> pairs = new Hashtable<AttackPair, AttackPair>();
		
		for(Intersection inter : intersections){
			AttackPair pair = new AttackPair(inter.getInsertStartNode(), inter.getRequestStartNode());
			if(!pairs.containsKey(pair))
				pairs.put(pair, pair);
			
			pair = pairs.get(pair);
			pair.addTargetNodes(inter.getPossibleTargetNodes());
		}
		return pairs;
	}

	private Node insertNode;
	private Node requestNode;
	private List<Node> targetNodes = new ArrayList<Node>();

	public AttackPair(Node i, Node r) {
		this.insertNode = i;
		this.requestNode = r;
	}

	public void addTargetNodes(List<Node> nodes) {
		for( Node n : nodes){
			if (!this.targetNodes.contains(n))
				this.targetNodes.add(n);
		}
	}
	
	public List<Node> getTargetNodes(){
		return this.targetNodes;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;

		if (!(obj instanceof AttackPair))
			return false;
		AttackPair node = (AttackPair) obj;
		return node.insertNode.equals(this.insertNode) && node.requestNode.equals(this.requestNode);
	}

	@Override
	public int hashCode() {
		return this.insertNode.hashCode() + this.requestNode.hashCode();
	}

	@Override
	public int compareTo(Object o) {
		if(o == null)
			return -1;
		if(!(o instanceof AttackPair))
			return -1;
		AttackPair i = (AttackPair)o;
		int cmp = this.insertNode.compareTo(i.insertNode);
		if(cmp != 0)
			return cmp;
		return this.requestNode.compareTo(i.requestNode);
	}

}
