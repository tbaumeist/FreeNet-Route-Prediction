package test.frp.main.rti;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import frp.main.rti.analysis.AttackPair;
import frp.main.rti.analysis.AttackSizeSet;
import frp.main.rti.prediction.RTIPrediction;
import frp.routing.Topology;
import frp.routing.itersection.Intersection;

import test.frp.Helper;

public class RTIAnalysis {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAttackPairs() throws Exception {
		Topology topology = Helper.load50_4Topology();
		int maxHTL = 4;
		int dhtl = 0;
		int maxAGS = 8;
		String outputFileName = null;
		
		RTIPrediction rtiPrediction = new RTIPrediction();
		List<Intersection> intersections = rtiPrediction.run(topology,
				maxHTL, outputFileName, dhtl);

		List<AttackPair> attackpairs = AttackPair
				.extractAttackPairs(intersections, topology);
		
		// check all permutations are here
		assertTrue(attackpairs.size() == Helper.possiblePairs(50));
		
		AttackSizeSet attSet = new AttackSizeSet(maxAGS, attackpairs,
				topology.getAllNodes());
		
		// should always be zero
		assertTrue(attSet.getMax(0) == 0);
		assertTrue(attSet.getMax(1) == 0);
		assertTrue(attSet.getMin(0) == 0);
		assertTrue(attSet.getMin(1) == 0);
	}

}
