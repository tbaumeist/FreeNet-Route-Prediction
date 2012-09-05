package frp.main.insertModelEval;

import java.util.ArrayList;
import java.util.List;

import frp.dataFileReaders.CSVReader;

public class ActualRoutePath {
	
	private int nodeCount;
	private int peerCount;
	private int htl;
	
	private String origin = "";
	private String word = "";
	private double location;
	private List<String> storing = new ArrayList<String>();
	private List<String> path = new ArrayList<String>();
	
	private ActualRoutePath(int n, int p, int h, String o, String w, double l){
		this.nodeCount = n;
		this.peerCount = p;
		this.htl = h;
		this.origin = o;
		this.word = w;
		this.location = l;
	}
	
	public String getWord(){
		return this.word;
	}
	
	public List<String> getPath(){
		return this.path;
	}
	
	public String getOriginNode(){
		return this.origin;
	}
	
	public double getWordLocation(){
		return this.location;
	}
	
	public int getNodeCount(){
		return this.nodeCount;
	}
	
	public int getPeerCount() {
		return this.peerCount;
	}
	
	public int getHTL() {
		return this.htl;
	}
	
	private void setStorage(String storage){
		String[] arr = storage.split("\\|");
		for(String s : arr){
			if(s.isEmpty())
				continue;
			this.storing.add(s);
		}
	}
	
	private void setPath(String p){
		String[] arr = p.split("\\|");
		for(String s : arr){
			if(s.isEmpty())
				continue;
			this.path.add(s);
		}
	}
	
	
	private static final int NODECOUNT_I = 1;
	private static final int PEERCOUNT_I = 2;
	private static final int HTL_I = 3;
	private static final int ORIGIN_I = 4;
	private static final int WORD_I = 5;
	private static final int LOCATION_I = 6;
	private static final int STORING_I = 7;
	private static final int PATH_I = 8;

	public static List<ActualRoutePath> readFromFile(CSVReader reader) throws Exception{
		
		List<ActualRoutePath> actPaths = new ArrayList<ActualRoutePath>();
		while(reader.readLine()){
			int nodeCount = Integer.parseInt(reader.getColumn(NODECOUNT_I));
			int peerCount = Integer.parseInt(reader.getColumn(PEERCOUNT_I));
			int htl = Integer.parseInt(reader.getColumn(HTL_I));
			String origin = reader.getColumn(ORIGIN_I);
			String word = reader.getColumn(WORD_I);
			String storing = reader.getColumn(STORING_I);
			String path = reader.getColumn(PATH_I);
			double location = Double.parseDouble(reader.getColumn(LOCATION_I));
			
			ActualRoutePath p = new ActualRoutePath(nodeCount, peerCount, htl, origin, word, location);
			p.setStorage(storing);
			p.setPath(path);
			
			actPaths.add(p);
		}
		
		return actPaths;
	}
}
