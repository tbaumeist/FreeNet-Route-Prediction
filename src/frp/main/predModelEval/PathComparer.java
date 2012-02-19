package frp.main.predModelEval;

import java.io.PrintStream;
import java.util.List;

import frp.routing.Path;
import frp.routing.PathSet;
import frp.routing.Topology;

public class PathComparer {
	private int total = 0;
	private int storageLocationActual = 0;
	private int storageLocationActualPart = 0;
	private double totalStorageNodes = 0;

	public void compareStorageNodes(PrintStream writer, PrintStream fullData,
			Topology topology, List<StoredWordData> theData,
			List<PathSet[]> pathSets, CommLog log, int hopReset, int maxHTL) {

		int[][] perHopStats = new int[maxHTL][3];
		printFullDataHeader(fullData);

		for (StoredWordData data : theData) {
			try {
				PathSet ps = PathSet.findPathSet(data.getOriginNode(),
						data.getHtl(), pathSets);
				if (ps == null)
					continue;

				List<Path> paths = ps.findPaths(Double.parseDouble(data
						.getLocation()));
				int foundCnt = 0;
				int foundPartCnt = 0;
				double guessConvidence = 0.0;
				double guessExtraConvidence = 0.0;
				String storageNodes = "";
				for (Path p : paths) {
					guessConvidence += p.getPathConfidence()
							/ (double) paths.size();
					guessExtraConvidence += p.getPathConfidenceWithProbStoreNodes()
							/ (double) paths.size();

					for(String s : p.getProbableStoreNodeIds(hopReset)){
						storageNodes += s + " ";
					}

					this.total++;
					if (data.getActualStorageNodes().containsAll(
							p.getProbableStoreNodeIds(hopReset))) {
						this.storageLocationActual++;
						foundCnt++;
					}
					// check partially correct
					for(String s : data.getActualStorageNodes()){
						if(p.getProbableStoreNodeIds(hopReset).contains(s)){
							foundPartCnt++;
						}
					}
					if(foundPartCnt> 0)
						this.storageLocationActualPart++;
					
					// store per htl stats
					perHopStats[data.getHtl()-1][0]++;
					if(foundCnt > 0)
						perHopStats[data.getHtl()-1][1]++;
					if(foundPartCnt > 0)
						perHopStats[data.getHtl()-1][2]++;
					
					// should only ever be one path per inserted data
					printFullData(fullData, foundCnt>0, foundPartCnt, data, guessConvidence, guessExtraConvidence,
							storageNodes, p.toStringSimple(), p.toStringExtraNodesSimple(), log);
				}
				this.totalStorageNodes += data.getActualStorageNodes().size();

			} catch (Exception e) {

			}
		}
		writer.println("Total inserts: " + this.total);
		writer.println("Total correct predictions: "
				+ this.storageLocationActual + " "
				+ ((double) this.storageLocationActual / (double) this.total)
				* 100.0 + "%");
		writer.println("Total partially correct predictions: "
				+ this.storageLocationActualPart + " "
				+ ((double) this.storageLocationActualPart / (double) this.total)
				* 100.0 + "%");
		writer.println("Average # actual storage nodes per node: "
				+ ((double) this.totalStorageNodes / (double) this.total));
		writer.println("Per HTL Stats");
		
		for(int i =0; i < perHopStats.length; i++){
			if(i == 0)
				continue;
			writer.println((i+1)+","+perHopStats[i][0] + ","+perHopStats[i][1]+ ","+perHopStats[i][2]);
		}
	}

	private void printFullDataHeader(PrintStream writer) {
		writer.println("Word, Key, Location, Insert Node, Guessed Storage Nodes, Confidence, Confidence w/ extra nodes, Hit, Partial Hit Count, Actual Storage Nodes, Actual Storage Node Count, HTL, Guessed Path, Extra Guessed Nodes, Actual Paths, Seen Reject");
	}

	private void printFullData(PrintStream writer, boolean fullHit, int foundPartCnt,
			StoredWordData data, double guessConvidence, double guessExtraConvidence, String storageNodes,
			String guessPath, String extraPath, CommLog log) {

		// word, key, location, insert node, guessed storage nodes, confidence,
		// hit,
		// hit count, actual storage nodes, Seen Reject
		writer.println(data.getWord() + "," + data.getKey() + ","
				+ data.getLocation() + "," + data.getOriginNode() + ","
				+ storageNodes + "," + guessConvidence + "," + guessExtraConvidence +","
				+ (fullHit ? "TRUE" : "FALSE") + "," + foundPartCnt + ","
				+ data.getActualStorageNodesToString() + ","
				+ data.getActualStorageNodes().size() + "," + data.getHtl()
				+ "," + guessPath.replace(",", "->").replace(" ", "") + ","
				+ extraPath.replace(",", "->").replace(" ", "") + ","
				+ log.toStringPath(data.getKey()) + ","
				+ getRejectionStatus(data, log));
	}
	
	private String getRejectionStatus(StoredWordData data, CommLog log){
		if(!log.hasKey(data.getKey()))
			return "?";
		if(log.hasReject(data.getKey()))
			return "TRUE";
		return "FALSE";
	}

}
