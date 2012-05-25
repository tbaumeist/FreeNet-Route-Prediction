package frp.main.rti.analysis;

import java.util.*;

import frp.routing.Node;

public class AttackSizeSet {
	private int subSetSize;
	private int totalNodeCount;
	
	private HashSet<Node> curMaxTargetNodes = new HashSet<Node>();
	private HashSet<Node> curMaxAttackNodes = new HashSet<Node>();
	private HashSet<Node> curMinTargetNodes = new HashSet<Node>();
	private HashSet<Node> curMinAttackNodes = new HashSet<Node>();
	private long startTime;

	private AttackSet[] attackSets;

	public AttackSizeSet(int subSetSize, List<AttackPair> attackPair,
			List<Node> allNodes) {
		if (attackPair.size() <= 0)
			return;

		this.subSetSize = subSetSize;
		this.attackSets = new AttackSet[this.subSetSize + 1];
		this.totalNodeCount = allNodes.size();

		for(int i = 0; i <= this.subSetSize;i++)
		{
			System.out.println("RTI analysis for subset " + i);
			
			curMaxTargetNodes.clear();
			curMaxAttackNodes.clear();
			curMinTargetNodes.clear();
			curMinAttackNodes.clear();
			
			this.startTime = System.currentTimeMillis();
			
			calculateTop(attackPair, i , 2);
			this.attackSets[i] = new AttackSet(i, this.totalNodeCount,
					System.currentTimeMillis() - this.startTime,
					this.curMaxTargetNodes, this.curMaxAttackNodes);

			
			calculateBottom(attackPair, i, 2);
			this.attackSets[i].setMin(System.currentTimeMillis() - this.startTime,
					this.curMinTargetNodes, this.curMinAttackNodes);
		}
	}
	
	public int getMax(int subGroupSize){
		return this.attackSets[subGroupSize].getMax();
	}
	
	public int getMin(int subGroupSize){
		return this.attackSets[subGroupSize].getMin();
	}

	private void calculateTop(List<AttackPair> attackPair, int subSetSize, int count) {
		if (count > subSetSize  || this.curMaxAttackNodes.size() >= subSetSize)
			return;
		boolean onlyOne = (count == subSetSize && count != 2);
		AttackPair top = getTopPair(attackPair, onlyOne);
		
		if (top != null) {
			this.curMaxTargetNodes.addAll(top.getTargetNodes());
			this.curMaxTargetNodes.add(top.getNodeA());
			this.curMaxTargetNodes.add(top.getNodeB());

			this.curMaxAttackNodes.add(top.getNodeA());
			this.curMaxAttackNodes.add(top.getNodeB());
		}
		calculateTop(minusTargets(this.curMaxAttackNodes, top, attackPair), subSetSize, count + 1);
	}

	private void calculateBottom(List<AttackPair> attackPair, int subSetSize, int count) {
		if (count > subSetSize || this.curMinAttackNodes.size() >= subSetSize)
			return;
		boolean onlyOne = (count == subSetSize && count != 2);
		AttackPair bottom = getBottomPair(attackPair, onlyOne);
		
		if (bottom != null) {
			this.curMinTargetNodes.addAll(bottom.getTargetNodes());
			this.curMinTargetNodes.add(bottom.getNodeA());
			this.curMinTargetNodes.add(bottom.getNodeB());

			this.curMinAttackNodes.add(bottom.getNodeA());
			this.curMinAttackNodes.add(bottom.getNodeB());
		}
		calculateBottom(minusTargets(this.curMinAttackNodes, bottom, attackPair), subSetSize, count + 1);
	}
	
	private AttackPair getTopPair(List<AttackPair> attackPair, boolean onlyOneNew){
		if(attackPair == null || attackPair.size() == 0)
			return null;
		if(!onlyOneNew)
			return attackPair.get(0);
		
		for(AttackPair p : attackPair){
			if(this.curMaxAttackNodes.contains(p.getNodeA()))
				return p;
			if(this.curMaxAttackNodes.contains(p.getNodeB()))
				return p;
		}
		
		return null;
	}
	
	private AttackPair getBottomPair(List<AttackPair> attackPair, boolean onlyOneNew){
		if(attackPair == null || attackPair.size() == 0)
			return null;
		if(!onlyOneNew)
			return attackPair.get(attackPair.size() - 1);
		
		for(int i = attackPair.size() - 1; i >= 0; i--){
			AttackPair p = attackPair.get(i);
			if(this.curMinAttackNodes.contains(p.getNodeA()))
				return p;
			if(this.curMinAttackNodes.contains(p.getNodeB()))
				return p;
		}
		
		return null;
	}

	private List<AttackPair> minusTargets(Collection<Node> curAttackNodes, AttackPair remove,
			List<AttackPair> original) {
		if (remove == null)
			return original;

		List<AttackPair> minused = new ArrayList<AttackPair>();
		for (AttackPair n : original) {
			if (n.equals(remove))
				continue;
			AttackPair m = n.minusTargets(remove.getTargetNodes());
			m.removeTargetNode(remove.getNodeA());
			m.removeTargetNode(remove.getNodeB());
			minused.add(m);
		}
		return merge(curAttackNodes, minused);
	}

	private List<AttackPair> merge(Collection<Node> curAttackNodes, List<AttackPair> pairs) {
		List<AttackPair> merged = new ArrayList<AttackPair>();
		while(pairs.size() > 0){
			AttackPair p = pairs.get(0);
			pairs.remove(0);
			
			for(int i = pairs.size() - 1; i >= 0; i--){
				AttackPair p2 = pairs.get(i);
				if(canMerge(curAttackNodes, p, p2)){
					p.addTargetNodes(p2.getTargetNodes());
					pairs.remove(i);
				}
			}
			merged.add(p);
		}
		return merged;
	}
	
	private boolean canMerge(Collection<Node> curAttackNodes, AttackPair p1, AttackPair p2){
		
		Boolean common = false;
		// p1 Node A is common node and not already in the selected attack nodes
		if(p1.getNodeA().equals(p2.getNodeA()) ||
				p1.getNodeA().equals(p2.getNodeB())){
			common = true;
			if(curAttackNodes.contains(p1.getNodeA()))
				return false;
		}

		// p1 Node B is common node and not already in the selected attack nodes
		if(p1.getNodeB().equals(p2.getNodeA()) ||
				p1.getNodeB().equals(p2.getNodeB())){
			common = true;
			if(curAttackNodes.contains(p1.getNodeB()))
				return false;
		}
		
		if(!common)
			return false;
		
		// One node in p1 exists in already selected attack nodes
		if(!curAttackNodes.contains(p1.getNodeA()) && 
				!curAttackNodes.contains(p1.getNodeB()))
			return false;
		
		// One node in p2 exists in already selected attack nodes
		if(!curAttackNodes.contains(p2.getNodeA()) && 
				!curAttackNodes.contains(p2.getNodeB()))
			return false;
		
		return true;
	}

	@Override
	public String toString() {
		return toStringCSV();
	}

	public static String getCSVHeader() {
		return "Subset Size,# Total Nodes,# Min Targets,# Max Targets,Runtime (ms),Min Attack Node Set,Max Attack Node Set";
	}

	public String toStringCSV() {
		StringBuilder b = new StringBuilder();
		for (AttackSet s : this.attackSets) {
			b.append(s);
			b.append("\n");
		}
		return b.toString();
	}

	private class AttackSet {
		private int subSetSize, totalNodeCount;
		private long runTime;
		private List<Node> minAttackNodes, maxAttackNodes, minTargetNodes,
				maxTargetNodes;

		public AttackSet(int s, int t, long r, Collection<Node> maxN,
				Collection<Node> maxA) {
			this.minAttackNodes = new ArrayList<Node>();
			this.maxAttackNodes = new ArrayList<Node>();
			this.minTargetNodes = new ArrayList<Node>();
			this.maxTargetNodes = new ArrayList<Node>();

			this.subSetSize = s;
			this.totalNodeCount = t;
			this.runTime = r;

			this.maxAttackNodes.addAll(maxA);
			this.maxTargetNodes.addAll(maxN);
		}

		public void setMin(long r, Collection<Node> minN, Collection<Node> minA) {
			this.runTime += r;
			this.minAttackNodes.addAll(minA);
			this.minTargetNodes.addAll(minN);
		}
		
		public int getMax(){
			return this.maxAttackNodes.size();
		}
		public int getMin(){
			return this.minAttackNodes.size();
		}

		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			b.append(this.subSetSize);
			b.append(",");
			b.append(this.totalNodeCount);
			b.append(",");
			b.append(this.minTargetNodes.size());
			b.append(",");
			b.append(this.maxTargetNodes.size());
			b.append(",(");
			b.append(this.runTime);
			b.append("),Min[ ");
			for (Node n : this.minAttackNodes) {
				b.append(n);
				b.append(",");
			}
			b.deleteCharAt(b.length() - 1);
			b.append("],Max[ ");
			for (Node n : this.maxAttackNodes) {
				b.append(n);
				b.append(",");
			}
			b.deleteCharAt(b.length() - 1);
			b.append("]");

			return b.toString();
		}
	}
}
