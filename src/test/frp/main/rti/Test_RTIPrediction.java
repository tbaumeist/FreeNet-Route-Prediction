package test.frp.main.rti;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import org.junit.Test;

import test.frp.Helper;
import frp.main.rti.prediction.RTIPrediction;
import frp.routing.Topology;
import frp.routing.itersection.Intersection;

public class Test_RTIPrediction {

	@Test
	public void test() throws Exception {
		Topology topology = Helper.load50_4Topology();
		int maxHTL = 4;
		int dhtl = 0;
		String outputFileName = null;

		RTIPrediction rtiPrediction = new RTIPrediction();
		List<Intersection> intersections = rtiPrediction.run(topology, maxHTL,
				outputFileName, dhtl);

		// output all the intersection points
		File interFile = File.createTempFile("50-4-4", ".intersections");
		PrintStream interWriter = new PrintStream(interFile);
		for (Intersection i : intersections) {
			interWriter.println(i);
		}
		interWriter.close();

		Boolean filesEqual = Helper.filesAreEqual(Helper.getResourcePath()
				+ "intersections-50-4-4.dat", interFile.getAbsolutePath());
		
		interFile.delete();
		
		assertTrue(filesEqual);
	}

}
