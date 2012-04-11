package frp.routing;

import java.util.ArrayList;
import java.util.List;

import frp.utils.DistanceTools;

public class SubRange implements Comparable<Object> {

	private Node toNode;
	private double rangeStart, rangeStop;
	private boolean isRetry = false;
	private int tieCount = 0;
	private boolean isSelfRoute = false;

	public SubRange(Node toNode, double rangeStart, double rangeStop, int tieCount, boolean isSelfRoute){
		this(toNode, rangeStart, rangeStop);
		this.tieCount = tieCount;
		this.isSelfRoute = isSelfRoute;
	}
	public SubRange(Node toNode, double rangeStart, double rangeStop) {
		this.toNode = toNode;
		this.rangeStart = DistanceTools.round(rangeStart);
		this.rangeStop = DistanceTools.round(rangeStop);
		this.tieCount++;
	}
	
	public boolean isSelfRoute(){
		return this.isSelfRoute;
	}

	public Node getNode() {
		return this.toNode;
	}

	public double getStart() {
		return this.rangeStart;
	}
	
	public int getTieCount(){
		return this.tieCount;
	}

	public boolean getIsRetry() {
		return this.isRetry;
	}

	public void setIsRetry(boolean b) {
		this.isRetry = b;
	}

	public double getStop() {
		return this.rangeStop;
	}

	public boolean containsPoint(double pt) {
		if (!wrapsAround())
			return this.rangeStart <= pt && pt < this.rangeStop;
		return this.rangeStart <= pt || pt < this.rangeStop;
	}

	public boolean overlaps(SubRange range) {
		if (range == null)
			return false;

		if (!edgesOverlap(this, range).isEmpty())
			return true;
		if (!edgesOverlap(range, this).isEmpty())
			return true;

		return false;
	}
	
	public boolean areAdjacent(SubRange range){
		if(range == null)
			return false;
		
		if(this.getStart() == range.getStop())
			return true;
		if(this.getStop() == range.getStart())
			return true;
		
		return false;
	}
	
	public SubRange addAdjacentRanges(SubRange range){
		if(this.getStart() == range.getStop())
			this.rangeStart = range.getStart();
		if(this.getStop() == range.getStart())
			this.rangeStop = range.getStop();
			
		return this;
	}

	public boolean isEntireRange() {
		return this.rangeStart == this.rangeStop;
	}

	public List<SubRange> splitRangeOverMe(SubRange range) {
		List<SubRange> ranges = new ArrayList<SubRange>();

		if (range.isEntireRange()) { // special case
			ranges.add(new SubRange(range.getNode(), this.rangeStart,
					this.rangeStop, range.tieCount, range.isSelfRoute()));
			return ranges;
		}

		List<Double> edges = edgesOverlap(this, range);
		edges.remove(range.rangeStart);
		edges.remove(range.rangeStop);

		double prev = range.rangeStart;
		for (double d : edges) {
			ranges.add(new SubRange(range.getNode(), prev, d, range.tieCount, range.isSelfRoute()));
			prev = d;
		}
		ranges.add(new SubRange(range.getNode(), prev, range.rangeStop, range.tieCount, range.isSelfRoute()));

		return ranges;
	}
	
	public SubRange getIntersection(SubRange rr){
		double start = Math.max(this.rangeStart, rr.rangeStart);
		double myStop = wrapsAround() ? this.rangeStop + 1 : this.rangeStop;
		double thereStop = rr.wrapsAround() ? rr.rangeStop + 1 : rr.rangeStop;
		double stop = Math.min(myStop, thereStop);
		return new SubRange(this.toNode, start, stop % 1, this.tieCount, this.isSelfRoute());
	}

	@Override
	public String toString() {
		return "[ " + this.rangeStart + ", " + this.rangeStop + " )";
	}

	@Override
	public int compareTo(Object obj) {
		if (obj == null)
			return 1;
		if (!(obj instanceof SubRange))
			return 1;

		SubRange r = (SubRange) obj;
		return new Double(this.rangeStart).compareTo(new Double(r.rangeStart));
	}

	private List<Double> edgesOverlap(SubRange r1, SubRange r2) {
		List<Double> overlaps = new ArrayList<Double>();
		if (r1.wrapsAround() && r2.wrapsAround()) {
			if (r1.rangeStart >= r2.rangeStart
					&& r1.rangeStart < r2.rangeStop + 1)
				overlaps.add(r1.rangeStart); // r1 front is in r2 ranges
			if (r1.rangeStop + 1 > r2.rangeStart
					&& r1.rangeStop <= r2.rangeStop)
				overlaps.add(r1.rangeStop); // r1 end is in r2 ranges
		} else if (r2.wrapsAround()) {
			if (r1.rangeStart >= r2.rangeStart || r1.rangeStart < r2.rangeStop)
				overlaps.add(r1.rangeStart); // r1 front is in r2 ranges
			if (r1.rangeStop > r2.rangeStart || r1.rangeStop <= r2.rangeStop)
				overlaps.add(r1.rangeStop); // r1 end is in r2 ranges
		} else {
			// normal conditions
			if (r1.rangeStart >= r2.rangeStart && r1.rangeStart < r2.rangeStop)
				overlaps.add(r1.rangeStart); // r1 front is in r2 ranges
			if (r1.rangeStop > r2.rangeStart && r1.rangeStop <= r2.rangeStop)
				overlaps.add(r1.rangeStop); // r1 end is in r2 ranges
		}
		return overlaps;
	}

	private boolean wrapsAround() {
		return this.rangeStart >= this.rangeStop;
	}

}

