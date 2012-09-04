package test.frp.gephi.rti;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import frp.gephi.rti.AttackNodeAnalysisMulti;
import frp.gephi.rti.AttackNodeAnalysisSingle;

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

			AttackNodeAnalysisMulti ana = new AttackNodeAnalysisMulti();
			ana.run(topDirName, datFileName, outFileName, AttackNodeAnalysisSingle.MAX_HTL_COLUMNS);

			assertTrue(file.delete());
		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(false);
		}
	}
}
