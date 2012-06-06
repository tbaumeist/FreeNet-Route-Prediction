package frp.main.rti.analysis;

import java.util.*;

import frp.routing.Node;
import frp.utils.Progresser;

public class AttackSizeSet {
	private int subSetSize;
	private int totalNodeCount;

	private HashSet<Node> curMaxTargetNodes = new HashSet<Node>();
	private HashSet<Node> curMaxAttackNodes = new HashSet<Node>();
	private HashSet<Node> curMinTargetNodes = new HashSet<Node>();
	private HashSet<Node> curMinAttackNodes = new HashSet<Node>();
	private long startTime;

	private _AttackSet[] attackSets;

	public AttackSizeSet(int subSetSize, List<AttackPair> attackPair,
			List<Node> allNodes) {
		if (attackPair.size() <= 0)
			return;

		this.subSetSize = subSetSize;
		this.attackSets = new _AttackSet[this.subSetSize + 1];
		this.totalNodeCount = allNodes.size();
		
		System.out.println("Analysing RTI set size " + this.subSetSize + " ...");
		Progresser prog = new Progresser(System.out, this.subSetSize * 2);

		curMaxTargetNodes.clear();
		curMaxAttackNodes.clear();
		curMinTargetNodes.clear();
		curMinAttackNodes.clear();

		this.startTime = System.currentTimeMillis();
		calculateTop(prog, attackPair, 0);

		this.startTime = System.currentTimeMillis();
		calculateBottom(prog, attackPair, 0);

	}

	public int getMax(int subGroupSize) {
		return this.attackSets[subGroupSize].getMax();
	}

	public int getMin(int subGroupSize) {
		return this.attackSets[subGroupSize].getMin();
	}

	private void storeCurrentMax(int location) {
		this.attackSets[location] = new _AttackSet(location,
				this.totalNodeCount, System.currentTimeMillis()
						- this.startTime, this.curMaxTargetNodes,
				this.curMaxAttackNodes);
	}

	private void storeCurrentMin(int location) {
		this.attackSets[location].setMin(System.currentTimeMillis()
				- this.startTime, this.curMinTargetNodes,
				this.curMinAttackNodes);
	}

	private void calculateTop(Progresser prog, List<AttackPair> attackPair, int count) {
		
		if (count > this.subSetSize)
			return;
		
		//System.out.println("RTI Top analysis for subset " + count + "...");
		Collections.sort(attackPair);

		boolean incCount = false;
		HashSet<Node> copyCurMaxAttackNodes = new HashSet<Node>();
		HashSet<Node> copyCurMaxTargetNodes = new HashSet<Node>();

		if (count < 2) {
			storeCurrentMax(count);
			incCount = true;
			if(prog != null)
				prog.hit();
		}

		boolean onlyOne = (count == (this.curMaxAttackNodes.size() + 1));
		AttackPair top = null;
		if (count > 1) {
			top = getTopPair(attackPair, onlyOne);

			if (top != null) {
				copyCurMaxAttackNodes.addAll(this.curMaxAttackNodes);
				copyCurMaxTargetNodes.addAll(this.curMaxTargetNodes);

				this.curMaxTargetNodes.addAll(top.getTargetNodes());
				this.curMaxTargetNodes.add(top.getNodeA());
				this.curMaxTargetNodes.add(top.getNodeB());

				this.curMaxAttackNodes.add(top.getNodeA());
				this.curMaxAttackNodes.add(top.getNodeB());
			}
		}

		// attack nodes are ready to be stored
		if (count > 1 && this.curMaxAttackNodes.size() >= count) {
			storeCurrentMax(count);
			if(prog != null)
				prog.hit();
			// Remove that last selection
			this.curMaxAttackNodes = copyCurMaxAttackNodes;
			this.curMaxTargetNodes = copyCurMaxTargetNodes;
			count++;
			top = null;
		}

		if (incCount)
			count++;

		calculateTop(prog, minusTargets(this.curMaxAttackNodes, top, attackPair),
				count);
	}

	private void calculateBottom(Progresser prog, List<AttackPair> attackPair, int count) {
		if (count > this.subSetSize)
			return;
		
		//System.out.println("RTI Bottom analysis for subset " + count + "...");
		Collections.sort(attackPair);

		boolean incCount = false;
		HashSet<Node> copyCurMinAttackNodes = new HashSet<Node>();
		HashSet<Node> copyCurMinTargetNodes = new HashSet<Node>();

		if (count < 2) {
			storeCurrentMin(count);
			incCount = true;
			if(prog != null)
				prog.hit();
		}

		boolean onlyOne = (count == (this.curMinAttackNodes.size() + 1));
		AttackPair bottom = null;
		if (count > 1) {
			bottom = getBottomPair(attackPair, onlyOne);

			if (bottom != null) {
				copyCurMinAttackNodes.addAll(this.curMinAttackNodes);
				copyCurMinTargetNodes.addAll(this.curMinTargetNodes);

				this.curMinTargetNodes.addAll(bottom.getTargetNodes());
				this.curMinTargetNodes.add(bottom.getNodeA());
				this.curMinTargetNodes.add(bottom.getNodeB());

				this.curMinAttackNodes.add(bottom.getNodeA());
				this.curMinAttackNodes.add(bottom.getNodeB());
			}
		}

		// attack nodes are ready to be stored
		if (count > 1 && this.curMinAttackNodes.size() >= count) {
			storeCurrentMin(count);
			if(prog != null)
				prog.hit();
			// Remove that last selection
			this.curMinAttackNodes = copyCurMinAttackNodes;
			this.curMinTargetNodes = copyCurMinTargetNodes;
			count++;
			bottom = null;
		}

		if (incCount)
			count++;

		calculateBottom( prog,
				minusTargets(this.curMinAttackNodes, bottom, attackPair), count);
	}

	private AttackPair getTopPair(List<AttackPair> attackPair,
			boolean onlyOneNew) {
		if (attackPair == null || attackPair.size() == 0)
			return null;
		if (!onlyOneNew)
			return attackPair.get(0);

		for (AttackPair p : attackPair) {
			if (this.curMaxAttackNodes.contains(p.getNodeA()))
				return p;
			if (this.curMaxAttackNodes.contains(p.getNodeB()))
				return p;
		}

		return null;
	}

	private AttackPair getBottomPair(List<AttackPair> attackPair,
			boolean onlyOneNew) {
		if (attackPair == null || attackPair.size() == 0)
			return null;
		if (!onlyOneNew)
			return attackPair.get(attackPair.size() - 1);

		for (int i = attackPair.size() - 1; i >= 0; i--) {
			AttackPair p = attackPair.get(i);
			if (this.curMinAttackNodes.contains(p.getNodeA()))
				return p;
			if (this.curMinAttackNodes.contains(p.getNodeB()))
				return p;
		}

		return null;
	}

	private List<AttackPair> minusTargets(Collection<Node> curAttackNodes,
			AttackPair remove, List<AttackPair> original) {
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

	private List<AttackPair> merge(Collection<Node> curAttackNodes,
			List<AttackPair> pairs) {
		List<AttackPair> merged = new ArrayList<AttackPair>();
		while (pairs.size() > 0) {
			AttackPair p = pairs.get(0);
			pairs.remove(0);

			for (int i = pairs.size() - 1; i >= 0; i--) {
				AttackPair p2 = pairs.get(i);
				if (canMerge(curAttackNodes, p, p2)) {
					p.addTargetNodes(p2.getTargetNodes());
					pairs.remove(i);
				}
			}
			merged.add(p);
		}
		return merged;
	}

	private boolean canMerge(Collection<Node> curAttackNodes, AttackPair p1,
			AttackPair p2) {

		Boolean common = false;
		// p1 Node A is common node and not already in the selected attack nodes
		if (p1.getNodeA().equals(p2.getNodeA())
				|| p1.getNodeA().equals(p2.getNodeB())) {
			common = true;
			if (curAttackNodes.contains(p1.getNodeA()))
				return false;
		}

		// p1 Node B is common node and not already in the selected attack nodes
		if (p1.getNodeB().equals(p2.getNodeA())
				|| p1.getNodeB().equals(p2.getNodeB())) {
			common = true;
			if (curAttackNodes.contains(p1.getNodeB()))
				return false;
		}

		if (!common)
			return false;

		// One node in p1 exists in already selected attack nodes
		if (!curAttackNodes.contains(p1.getNodeA())
				&& !curAttackNodes.contains(p1.getNodeB()))
			return false;

		// One node in p2 exists in already selected attack nodes
		if (!curAttackNodes.contains(p2.getNodeA())
				&& !curAttackNodes.contains(p2.getNodeB()))
			return false;

		return true;
	}

	@Override
	public String toString() {
		return toStringCSV();
	}

	public static String getCSVHeader() {
		// ,# Total Nodes
		return "Subset Size/Total Node Count,Subset Size,# Min Targets,# Max Targets,Runtime (ms),Min Attack Node Set,Max Attack Node Set";
	}

	public String toStringCSV() {
		StringBuilder b = new StringBuilder();
		for (_AttackSet s : this.attackSets) {
			b.append(s);
			b.append("\n");
		}
		return b.toString();
	}

	private class _AttackSet {
		private int subSetSize, totalNodeCount;
		private long runTime;
		private List<Node> minAttackNodes, maxAttackNodes, minTargetNodes,
				maxTargetNodes;

		public _AttackSet(int s, int t, long r, Collection<Node> maxN,
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

		public int getMax() {
			return this.maxTargetNodes.size();
		}

		public int getMin() {
			return this.minTargetNodes.size();
		}

		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			b.append(this.subSetSize / (double) this.totalNodeCount);
			b.append(",");
			b.append(this.subSetSize);
			// b.append(",");
			// b.append(this.totalNodeCount);
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
