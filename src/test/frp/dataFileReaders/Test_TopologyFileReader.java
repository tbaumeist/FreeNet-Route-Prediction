package test.frp.dataFileReaders;

import static org.junit.Assert.*;

import org.junit.Test;

import test.frp.Helper;

import frp.dataFileReaders.TopologyFileReaderManager;
import frp.routing.Topology;


public class Test_TopologyFileReader {
	@Test
	public void readerDotGml() throws Exception {
		TopologyFileReaderManager topReader = new TopologyFileReaderManager();
		
		Topology topologyDot = topReader.readFromFile(Helper.getResourcePath("topology-50-4-full.dot"));
		assertTrue(topologyDot != null);
		
		Topology topologyGML = topReader.readFromFile(Helper.getResourcePath("topology-50-4-full.gml"));
		assertTrue(topologyGML != null);
		
		Topology topologyNull = topReader.readFromFile(Helper.getResourcePath("topology-50-4.dot"));
		assertTrue(topologyNull == null);
		
		String topDot = topologyDot.toString();
		String topGml = topologyGML.toString();
		assertTrue(topDot.equals(topGml));
	}
}
