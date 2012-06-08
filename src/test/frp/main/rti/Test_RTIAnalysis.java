package test.frp.main.rti;

import static org.junit.Assert.*;

import java.util.List;
import org.junit.Test;

import frp.main.rti.analysis.AttackPair;
import frp.main.rti.analysis.AttackSizeSet;
import frp.main.rti.prediction.RTIPrediction;
import frp.routing.Topology;

import test.frp.Helper;

public class Test_RTIAnalysis {

	@Test
	public void testMemoryUsage() throws Exception {
		Topology topology = Helper.load200_10Topology();
		//Topology topology = Helper.load125_10Topology();
		
		int maxHTL = 10;
		int dhtl = 0;
		int maxAGS = topology.getAllNodes().size();
		String outputFileName = null;
		
		RTIPrediction rtiPrediction = new RTIPrediction();
		List<AttackPair> attackPairs = rtiPrediction.run(topology,
				maxHTL, outputFileName, dhtl);		
		
		AttackSizeSet attSet = new AttackSizeSet(maxAGS, attackPairs,
				topology.getAllNodes());
		assertTrue(attSet != null);
		assertTrue(true);
	}
	
	@Test
	public void testAttackPairs() throws Exception {
		Topology topology = Helper.load50_4Topology();
		int maxHTL = 4;
		int dhtl = 0;
		int maxAGS = 8;
		String outputFileName = null;
		
		RTIPrediction rtiPrediction = new RTIPrediction();
		List<AttackPair> attackPairs = rtiPrediction.run(topology,
				maxHTL, outputFileName, dhtl);
		
		// check all permutations are here
		assertTrue(attackPairs.size() == Helper.possiblePairs(50));
		
		AttackSizeSet attSet = new AttackSizeSet(maxAGS, attackPairs,
				topology.getAllNodes());
		
		// should always be zero
		assertTrue(attSet.getMax(0) == 0);
		assertTrue(attSet.getMax(1) == 0);
		assertTrue(attSet.getMin(0) == 0);
		assertTrue(attSet.getMin(1) == 0);
	}
	
	@Test
	public void testAnalysis() throws Exception {
		Topology topology = Helper.load50_4Topology();
		int maxHTL = 4;
		int dhtl = 0;
		int maxAGS = 8;
		String outputFileName = null;
		
		RTIPrediction rtiPrediction = new RTIPrediction();
		List<AttackPair> attackPairs = rtiPrediction.run(topology,
				maxHTL, outputFileName, dhtl);		
		
		AttackSizeSet attSet = new AttackSizeSet(maxAGS, attackPairs,
				topology.getAllNodes());
		
		/*
		 	0,0,0
			1,0,0
			2,2,36
			3,11,48
			4,6,50
			5,27,50
			6,21,50
			7,29,50
			8,23,50
		 */
		
		// should always be zero
		assertTrue(attSet.getMax(0) == 0);
		assertTrue(attSet.getMax(1) == 0);
		assertTrue(attSet.getMin(0) == 0);
		assertTrue(attSet.getMin(1) == 0);
		
		
		assertTrue(attSet.getMax(2) == 36);
		assertTrue(attSet.getMin(2) == 2);
		assertTrue(attSet.getMax(3) == 48);
		assertTrue(attSet.getMin(3) == 11);
		assertTrue(attSet.getMax(4) == 50);
		assertTrue(attSet.getMin(4) == 6);
		assertTrue(attSet.getMax(5) == 50);
		assertTrue(attSet.getMin(5) == 27);
		assertTrue(attSet.getMax(6) == 50);
		assertTrue(attSet.getMin(6) == 21);
		assertTrue(attSet.getMax(7) == 50);
		assertTrue(attSet.getMin(7) == 29);
		assertTrue(attSet.getMax(8) == 50);
		assertTrue(attSet.getMin(8) == 23);
	}

}
