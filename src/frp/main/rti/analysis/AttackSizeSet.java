package frp.main.rti.analysis;

import java.util.*;

import frp.routing.Node;

public class AttackSizeSet {
	private int subSetSize;
	private int totalNodeCount;
	private List<AttackPair> maxTargets = new ArrayList<AttackPair>();
	private HashSet<Node> maxTargetNodes = new HashSet<Node>();
	private HashSet<Node> maxAttackNodes = new HashSet<Node>();
	private List<AttackPair> minTargets = new ArrayList<AttackPair>();
	private HashSet<Node> minTargetNodes = new HashSet<Node>();
	private HashSet<Node> minAttackNodes = new HashSet<Node>();
	private long runTime = 0;

	public AttackSizeSet(int subSetSize,
			List<AttackPair> attackPair, List<Node> allNodes) {
		if(attackPair.size() <= 0)
			return;
		
		this.subSetSize = subSetSize;
		this.totalNodeCount = allNodes.size();
		long t = System.currentTimeMillis();

		calculateTop(attackPair, this.subSetSize - 1);
		calculateBottom(attackPair, this.subSetSize - 1);
		
		this.runTime = System.currentTimeMillis() - t;
	}
	
	private void calculateTop(List<AttackPair> attackPair, int count){
		if(count <= 0 || attackPair.size() <= 0)
			return;
		AttackPair top = attackPair.get(0);
		if(top.getTargetNodes().size() <= 0)
			return;
		this.maxTargets.add(top);
		
		this.maxTargetNodes.addAll(top.getTargetNodes());
		this.maxTargetNodes.add(top.getNodeA());
		this.maxTargetNodes.add(top.getNodeB());
		
		this.maxAttackNodes.add(top.getNodeA());
		this.maxAttackNodes.add(top.getNodeB());
		
		calculateTop(minusTargets(top, attackPair), count - 1);
	}
	
	private void calculateBottom(List<AttackPair> attackPair, int count){
		if(count <= 0 || attackPair.size() <= 0)
			return;
		AttackPair bottom = attackPair.get(attackPair.size() - 1);
		this.minTargets.add(bottom);
		
		this.minTargetNodes.addAll(bottom.getTargetNodes());
		this.minTargetNodes.add(bottom.getNodeA());
		this.minTargetNodes.add(bottom.getNodeB());
		
		this.minAttackNodes.add(bottom.getNodeA());
		this.minAttackNodes.add(bottom.getNodeB());
		calculateBottom(minusTargets(bottom, attackPair), count - 1);
	}
	
	private List<AttackPair> minusTargets(AttackPair remove, List<AttackPair> original){
		List<AttackPair> minused = new ArrayList<AttackPair>();
		for(AttackPair n : original){
			if(n.equals(remove))
				continue;
			AttackPair m = n.minusTargets(remove.getTargetNodes());
			m.removeTargetNode(remove.getNodeA());
			m.removeTargetNode(remove.getNodeB());
			minused.add(m);
		}
		return minused;
	}

	@Override
	public String toString() {
		return toStringCSV();
	}
	
	public static String getCSVHeader(){
		return "Subset Size,# Total Nodes,# Min Targets,# Max Targets,Min Targets,Max Targets,Runtime (ms)";
	}
	
	public String toStringCSV() {
		StringBuilder b = new StringBuilder();
		b.append(this.subSetSize);
		b.append(",");
		b.append(this.totalNodeCount);
		b.append(",");
		b.append(this.minTargetNodes.size());
		b.append(",");
		b.append(this.maxTargetNodes.size());
		b.append(",Min[ ");
		for(Node n : this.minAttackNodes){
			b.append(n);
			b.append(",");
		}
		b.deleteCharAt(b.length() - 1);
		b.append("],Max[ ");
		for(Node n : this.maxAttackNodes){
			b.append(n);
			b.append(",");
		}
		b.deleteCharAt(b.length() - 1);
		b.append("],");
		b.append(this.runTime);

		return b.toString();
	}
}
