package frp.main.insertModelEval;

import java.io.PrintStream;
import java.util.List;

import frp.routing.Node;
import frp.routing.Path;
import frp.routing.PathSet;
import frp.routing.Topology;

public class PathComparer {

	public void writerHeader(PrintStream s) {
		s.println("Node Count,Peer Count,HTL,Word,Word Location,Origin Node,Model Accuracy,Model Confidence,Model Path,Actual Path");
	}

	public String compareStorageNodes(Topology topology,
			int hopReset, int maxHTL, List<PathSet[]> pathSets,
			ActualRoutePath actPath) {

		StringBuilder b = new StringBuilder();
		
		PathSet ps = PathSet.findPathSet(actPath.getOriginNode(), actPath
				.getHTL(), pathSets);

		if (ps == null){
			return "Error Processing Entry";
		}
		
		Path path = ps.findPaths(actPath.getWordLocation()).get(0);
		
		int index =0;
		for( Node n : path.getNodes()){
		
			if( index >= actPath.getPath().size())
				break;
			
			String realNode = actPath.getPath().get(index);
			if(!n.getID().equals(realNode))
				break;
			
			index++;
		}
		
		int length = Math.max(actPath.getPath().size(), path.getNodes().size());
		double accuracy = index / (double)length;
		
		
		b.append(actPath.getNodeCount());
		b.append(",");
		b.append(actPath.getPeerCount());
		b.append(",");
		b.append(actPath.getHTL());
		b.append(",");
		b.append(actPath.getWord());
		b.append(",");
		b.append(actPath.getWordLocation());
		b.append(",");
		b.append(actPath.getOriginNode());
		b.append(",");
		b.append(accuracy);
		b.append(",");
		b.append(path.getPathConfidence());
		b.append(",");
		for(Node n : path.getNodes())
			b.append(n.getID() + "|");
		b.append(",");
		for(String s : actPath.getPath())
			b.append(s + "|");
		
		return b.toString();
	}
}
