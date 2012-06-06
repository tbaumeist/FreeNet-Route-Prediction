package test.frp.routing;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import test.frp.Helper;

import frp.routing.PathSet;
import frp.routing.RoutingManager;
import frp.routing.Topology;
import frp.utils.Pair;

public class Test_PathSet extends RoutingManager {
	private final static int maxHTL = 5;
	private final static int detHTL = 0;

	public Test_PathSet() {
		super(maxHTL, detHTL);
	}

	@Test
	public void test() throws Exception {

		Topology top = Helper.load50_4Topology();
		List<Pair<Double, String>> startNodes = checkStartNodes(null, top);
		List<PathSet[]> pathInsertSets = calculateRoutesFromNodes(null,
				startNodes, top, true);

		// write out file
		File tmp1 = File.createTempFile("test", "pathset_storage1");
		PathSet.savePathSetList(pathInsertSets, tmp1.getAbsolutePath());
		
		// manually write out to single file
		PrintStream writer1 = new PrintStream(tmp1);
		for (PathSet[] psArr : pathInsertSets) {
			for (PathSet ps : psArr) {
				writer1.println(ps);
			}
		}
		writer1.close();

		// read back in
		List<PathSet> reloaded = new ArrayList<PathSet>();
		PathSet.PathSetReader reader = PathSet.createPathSetReader(tmp1
				.getAbsolutePath());
		PathSet ps;
		while ((ps = reader.readNext()) != null)
			reloaded.add(ps);

		// write out file
		File tmp2 = File.createTempFile("test", "pathset_storage2");
		PrintStream writer2 = new PrintStream(tmp2);
		for (PathSet psr : reloaded) {
			writer2.println(psr);
		}
		writer2.close();
		
		assertTrue(Helper.filesAreEqual(tmp1.getAbsolutePath(), tmp2.getAbsolutePath()));
		
		PathSet.removeAllSavedPathSetList(tmp1.getAbsolutePath());
		tmp1.delete();
		tmp2.delete();
	}
}
