package frp.routing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class PathSet {
	private Node startNode;
	private List<Path> paths = new ArrayList<Path>();
	private int htl;
	
	private PathSet(){
		
	}
	
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
		String s = this.startNode + "|" + this.htl+"|";
		s += "Paths from " + this.startNode + " with HTL of "+this.htl+"\n";
		for (Path p : this.paths) {
			s += p+"\n";
		}
		return s;
	}
	
	private String serialize(){
		StringBuilder b = new StringBuilder();
		
		b.append(this.startNode.serialize());
		b.append("\n");
		b.append(this.htl);
		b.append("\n");
		
		for(Path p : this.paths){
			b.append(p.serialize());
			b.append("\n");
		}
		return b.toString();
	}
	
	private static PathSet deserialize(File file) throws Exception{
		if(file == null || !file.exists())
			return null;
		
		BufferedReader in = new BufferedReader(new FileReader(file));
		
		Node startNode = Node.deserialize(in.readLine());
		int htl = Integer.parseInt(in.readLine());
		
		PathSet ps = new PathSet(startNode, htl);
		
		String line;
		while ((line = in.readLine()) != null) {
			if(line.isEmpty())
				continue;
			Path p = Path.deserialize(line);
			ps.addPath(p);
		}
		in.close();
		
		return ps;
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
	
	public static void savePathSetList(List<PathSet[]> pathSetList,
			String fileNameBase) throws FileNotFoundException {

		// output the paths
		for (int i = 0; i < pathSetList.size(); i++) {
			PathSet[] sArray = pathSetList.get(i);

			for (int j = 0; j < sArray.length; j++) {
				File outputFile = new File(fileNameBase + "." + i + "."
						+ j);
				PrintStream insertWriter = new PrintStream(outputFile);
				insertWriter.println(sArray[j].serialize());
				insertWriter.close();
			}
		}
	}
	
	public static void removeAllSavedPathSetList(String fileNameBase){
		int i = 0;
		boolean foundOne = true;
		while(foundOne){
			int j = 0;
			foundOne = false;
			
			while(true) {
				File outputFile = new File(fileNameBase + "." + i + "."
						+ j);
				if(!outputFile.exists())
					break;
				
				foundOne = true;
				outputFile.delete();
				j++;
			}
			i++;
		}
		File f = new File(fileNameBase);
		f.delete();
	}
	
	public static PathSetReader createPathSetReader(String fileNameBase){
		PathSet ps = new PathSet();
		return ps.new PathSetReader(fileNameBase);
	}
	
	public class PathSetReader{
		private String fileNameBase;
		private int nodeCount = 0;
		private int htlCount = 0;
		
		public PathSetReader(String fileNameBase){
			this.fileNameBase = fileNameBase;
		}
		
		public PathSet readNext() throws Exception{
			File f = tryCurrentPosition();
			if(f != null)
				return PathSet.deserialize(f);
			
			// try next line
			nextLine();
			return PathSet.deserialize(tryCurrentPosition());
		}
		
		private void nextLine(){
			this.nodeCount++;
			this.htlCount = 0;
		}
		
		private File tryCurrentPosition(){
			File outputFile = new File(this.fileNameBase + "." + this.nodeCount + "."
					+ this.htlCount);
			if(!outputFile.exists())
				return null;
			
			this.htlCount++;
			return outputFile;
		}
	}
}

