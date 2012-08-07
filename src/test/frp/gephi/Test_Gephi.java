package test.frp.gephi;

import static org.junit.Assert.*;

import org.gephi.graph.api.DirectedGraph;
import org.junit.Test;

import test.frp.Helper;

import frp.gephi.GephiHelper;

public class Test_Gephi {

	@Test
	public void loadGraph() throws Exception {
		GephiHelper gHelper = new GephiHelper();

		String topFileName = Helper.getResourcePath("proper.topology-55-4.dot");
		DirectedGraph graph = gHelper.loadGraphFile(topFileName);

		assertTrue(graph.getNodeCount() == 55);
		assertTrue( graph.getEdgeCount() == 220);
	}
}
