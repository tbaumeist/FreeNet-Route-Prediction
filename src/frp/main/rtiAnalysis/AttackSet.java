package frp.main.rtiAnalysis;

import java.util.*;

import frp.routing.Node;

public class AttackSet {
	private List<Node> nodes = new ArrayList<Node>();
	private int targets = 0;
	
	public AttackSet(List<Node> usedNodes){
		if(usedNodes != null)
			this.nodes.addAll(usedNodes);
	}
	
	public int getTargets(){
		return this.targets;
	}

	
	public void calcProperties(Hashtable<AttackPair, AttackPair> attackPair){
		List<Node> targets = new ArrayList<Node>();
		
		for(int i = 0; i < this.nodes.size(); i++){
			for(int j = 0; j < this.nodes.size(); j++){
				if(i == j)
					continue;
				AttackPair data = new AttackPair(this.nodes.get(i), this.nodes.get(j));
				if(attackPair.containsKey(data)){
					AttackPair dataFound = attackPair.get(data);
					for(Node t : dataFound.getTargetNodes()){
						if(!targets.contains(t))
							targets.add(t);
					}
				}
			}
		}
		
		this.targets = targets.size();
	}
	
	@Override
	public String toString(){
		String s = "";
		for(Node n : this.nodes)
			s += n +" ";
		return s;
	}
}

