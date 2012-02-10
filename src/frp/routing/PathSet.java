package frp.routing;

import java.util.ArrayList;
import java.util.List;

public class PathSet {
	private Node startNode;
	private List<Path> paths = new ArrayList<Path>();
	private int htl;
	
	public PathSet(Node start, int htl){
		this.startNode = start;
		this.htl = htl;
	}
	
	public void addPath(Path p){
		this.paths.add(p);
	}
	
	public void addPaths(List<Path> paths){
		this.paths.addAll(paths);
	}
	
	public Node getStartNode(){
		return this.startNode;
	}
	
	public List<Path> getPaths(){
		return this.paths;
	}
	
	@Override
	public String toString() {
		String s = "Paths from " + this.startNode + " with HTL of "+this.htl+"\n";
		for (Path p : this.paths) {
			s += p+"\n";
		}
		return s;
	}
	
	public List<Path> findPaths(double dataLocation) {
		List<Path> paths = new ArrayList<Path>();
		for (Path p : this.getPaths()) {
			if (p.getRange().containsPoint(dataLocation))
				paths.add(p);
		}
		return paths;
	}
	
	public static PathSet findPathSet(String node, int htl, List<PathSet[]> sets) {
		for (PathSet[] sArray : sets) {
			if(sArray.length < htl)
				continue;
			if (sArray[htl-1].getStartNode().getID().equals(node))
				return sArray[htl-1];
		}
		return null;
	}
}

