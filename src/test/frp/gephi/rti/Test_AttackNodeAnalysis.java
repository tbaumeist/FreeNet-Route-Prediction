package test.frp.gephi.rti;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import frp.gephi.rti.AttackNodeAnalysis;

import test.frp.Helper;

public class Test_AttackNodeAnalysis {

	@Test
	public void testAnalysis() throws Exception {

		try {
			String datFileName = Helper
					.getResourcePath("rti.analysis.data.csv");
			String topDirName = Helper.getResourcePath();
			File file = File.createTempFile("ANA", ".csv");
			String outFileName = file.getAbsolutePath();

			AttackNodeAnalysis ana = new AttackNodeAnalysis();
			ana.run(topDirName, datFileName, outFileName);

			assertTrue(file.delete());
		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(false);
		}
	}
}
