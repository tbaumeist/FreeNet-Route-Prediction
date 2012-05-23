package frp.main.rti.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import frp.routing.Node;
import frp.routing.Topology;
import frp.routing.itersection.Intersection;

public class AttackPair implements Comparable<Object> {

	public static List<AttackPair> extractAttackPairs(
			List<Intersection> intersections, Topology topology) {
		
		// Use Hashtable for constructing the list, reduce search time looking for entries
		Hashtable<AttackPair, AttackPair> pairs = new Hashtable<AttackPair, AttackPair>();
		
		// generate all possible pairs of nodes
		for(Node n1: topology.getAllNodes()){
			for(Node n2: topology.getAllNodes()){
				if(n1.equals(n2))
					continue;
				AttackPair pair = new AttackPair(n1, n2);
				if(!pairs.containsKey(pair))
					pairs.put(pair, pair);
			}
		}
		
		for(Intersection inter : intersections){
			AttackPair pair = new AttackPair(inter.getInsertStartNode(), inter.getRequestStartNode());
			pair = pairs.get(pair);
			pair.addTargetNodes(inter.getPossibleTargetNodes());
		}
		
		// Copy to a list, need ordering now
		List<AttackPair> allPairs = new ArrayList<AttackPair>();
		allPairs.addAll(pairs.values());
		Collections.sort(allPairs);

		return allPairs;
	}

	private Node nodeA;
	private Node nodeB;
	private List<Node> targetNodes = new ArrayList<Node>();

	public AttackPair(Node i, Node r) {
		this.nodeA = i;
		this.nodeB = r;
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
	
	public Node getNodeA(){
		return this.nodeA;
	}
	
	public Node getNodeB(){
		return this.nodeB;
	}
	
	public void removeTargetNode(Node n){
		this.targetNodes.remove(n);
	}
	
	public AttackPair minusTargets(List<Node> targets){
		AttackPair n = new AttackPair(this.nodeA, this.nodeB);
		n.targetNodes.addAll(this.getTargetNodes());
		n.targetNodes.removeAll(targets);
		return n;
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
		return (node.nodeA.equals(this.nodeA) && node.nodeB.equals(this.nodeB))
			|| (node.nodeA.equals(this.nodeB) && node.nodeB.equals(this.nodeA));
	}

	@Override
	public int hashCode() {
		return this.nodeA.hashCode() + this.nodeB.hashCode();
	}

	@Override
	public int compareTo(Object o) {
		if(o == null)
			return -1;
		if(!(o instanceof AttackPair))
			return -1;
		AttackPair i = (AttackPair)o;
		return (new Integer(this.getTargetNodes().size()).compareTo(new Integer(i.getTargetNodes().size())) * -1);
	}
	
	@Override
	public String toString(){
		StringBuilder b = new StringBuilder();
		
		b.append(getNodeA());
		b.append(", ");
		b.append(getNodeB());
		b.append(", [ ");
		for(Node n : getTargetNodes()){
			b.append(n);
			b.append(",");
		}
		b.append(" ]");
		return b.toString();
	}

}
