package test.frp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import frp.dataFileReaders.TopologyFileReader;
import frp.routing.Topology;

public class Helper {
	private final static String resourcePath = "bin/test/resources/";
	
	public static String getResourcePath(){
		return resourcePath;
	}
	
	public static String getResourcePath(String resource){
		return getResourcePath() + resource;
	}
	
	public static Topology load50_4Topology() throws Exception{
		TopologyFileReader topReader = new TopologyFileReader(
				getResourcePath("topology-50-4.dot"));
		Topology topology = topReader.readFile();
		return topology;
	}
	
	public static Topology load125_10Topology() throws Exception{
		TopologyFileReader topReader = new TopologyFileReader(
				getResourcePath("topology-125-10.dot"));
		Topology topology = topReader.readFile();
		return topology;
	}
	
	public static Topology load200_10Topology() throws Exception{
		TopologyFileReader topReader = new TopologyFileReader(
				getResourcePath("topology-200-10.dot"));
		Topology topology = topReader.readFile();
		return topology;
	}
	
	public static int possiblePairs(int nodeCount){
		int n = (nodeCount * nodeCount) - nodeCount;
		return n / 2;
	}
	
	public static boolean filesAreEqual(String f1Name, String f2Name) throws Exception{
		File f1 = new File(f1Name);
		File f2 = new File(f2Name);
		
		if(!f1.exists())
			return false;
		if(!f2.exists())
			return false;
		if(f1.length() != f2.length())
			return false;
		
		BufferedReader in1 = new BufferedReader(new FileReader(f1));
		BufferedReader in2 = new BufferedReader(new FileReader(f2));
		
		String line1, line2;
		while((line1 = in1.readLine()) != null && (line2 = in2.readLine()) != null){
			if(!line1.equals(line2))
				return false;
		}
		return true;
	}
}
