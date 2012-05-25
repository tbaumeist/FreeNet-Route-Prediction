package test.frp;

import frp.dataFileReaders.TopologyFileReader;
import frp.routing.Topology;

public class Helper {
	public static Topology load50_4Topology() throws Exception{
		TopologyFileReader topReader = new TopologyFileReader(
				"bin/test/resources/topology-50-4.dot");
		Topology topology = topReader.readFile();
		return topology;
	}
	
	public static int possiblePairs(int nodeCount){
		int n = (nodeCount * nodeCount) - nodeCount;
		return n / 2;
	}
}
