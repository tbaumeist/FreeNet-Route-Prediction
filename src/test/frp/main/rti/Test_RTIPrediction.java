package test.frp.main.rti;

import static org.junit.Assert.*;

import java.io.File;
import org.junit.Test;

import test.frp.Helper;
import frp.main.rti.prediction.RTIPrediction;
import frp.routing.Topology;

public class Test_RTIPrediction {

	@Test
	public void test() throws Exception {
		Topology topology = Helper.load50_4Topology();
		int maxHTL = 4;
		int dhtl = 0;
		File tmp = File.createTempFile("intersetions", "test");
		tmp.delete();

		RTIPrediction rtiPrediction = new RTIPrediction();
		rtiPrediction.run(topology, maxHTL, tmp.getAbsolutePath(), dhtl);
		
		File cmpFile = new File(tmp.getAbsolutePath()
				+ RTIPrediction.FILE_INTERSECTIONS_SUFFIX);

		Boolean filesEqual = Helper.filesAreEqual(Helper.getResourcePath()
				+ "intersections-50-4-4.dat", cmpFile.getAbsolutePath());
		
		cmpFile.delete();

		assertTrue(filesEqual);
	}
}
