package frp.main.rti.analysis;

import java.util.*;

import frp.routing.Node;

public class AttackSizeSet {
	private int subSetSize;
	private int totalNodeCount;
	private AttackSet maxTargets = null;
	private AttackSet minTargets = null;
	private long runningTotal = 0;
	private long runningCount = 0;
	private double avgTargets = 0;
	private long runTime = 0;

	public AttackSizeSet(int subSetSize,
			Hashtable<AttackPair, AttackPair> attackPair, List<Node> allNodes) {
		this.subSetSize = subSetSize;
		this.totalNodeCount = allNodes.size();
		long t = System.currentTimeMillis();

		tryAllCombinations(attackPair, new ArrayList<Node>(),
				allNodes, 0, this.subSetSize);

		if (this.minTargets == null)
			this.minTargets = new AttackSet(null);
		if (this.maxTargets == null)
			this.maxTargets = new AttackSet(null);
		if (this.runningCount > 0)
			this.avgTargets = ((double) this.runningTotal)
					/ ((double) this.runningCount);

		this.runTime = System.currentTimeMillis() - t;
	}

	@Override
	public String toString() {
		String s = "Sub-set: " + this.subSetSize + " Max{ " + this.maxTargets
				+ "} Avg: " + this.avgTargets;
		s += " Min{ " + this.minTargets + "}";
		s += " Run Time: " + this.runTime;
		return s;
	}
	
	public String toStringCSV() {
		String s = this.subSetSize + "," + this.totalNodeCount;
		s += "," + this.minTargets.getTargets();
		s += ","+this.avgTargets + ","+this.maxTargets.getTargets();
		s += ",Min[" + this.minTargets + "]";
		s += ",Max[" + this.maxTargets + "]";
		s += "," + this.runTime;
		return s;
	}

	private void tryAllCombinations(
			Hashtable<AttackPair, AttackPair> attackPair,
			List<Node> usedNodes, List<Node> allNodes, int start, int count) {

		if (count == 0) {
			if (usedNodes.isEmpty())
				return;

			AttackSet attSet = new AttackSet(usedNodes);

			attSet.calcProperties(attackPair);
			if (this.minTargets == null
					|| attSet.getTargets() < this.minTargets.getTargets())
				this.minTargets = attSet;
			if (this.maxTargets == null
					|| attSet.getTargets() > this.maxTargets.getTargets())
				this.maxTargets = attSet;
			this.runningTotal += attSet.getTargets();
			this.runningCount++;

			return;
		}

		if (start >= allNodes.size())
			return;

		for (int i = start; i < allNodes.size(); i++) {
			Node n = allNodes.get(i);
			if (usedNodes.contains(n))
				continue;

			usedNodes.add(n);
			tryAllCombinations(attackPair, usedNodes, allNodes, i + 1,
					count - 1);
			usedNodes.remove(n);
		}
	}
}
