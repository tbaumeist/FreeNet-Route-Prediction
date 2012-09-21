package frp.dataFileReaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import frp.routing.Node;
import frp.routing.Topology;

class TopologyFileReaderGML implements ITopologyFileReader{
	
	@Override
	public boolean canRead(String fileName) throws Exception {
		try {
			File top = new File(fileName);
			if (!top.exists())
				throw new Exception("Unable to find the topology file: "
						+ fileName);

			BufferedReader in = new BufferedReader(new FileReader(top));
			String line = in.readLine();
			return line.toLowerCase().startsWith("graph");
		} catch (Exception ex) {
			throw new Exception(
					"Error reading topology file. Improperly formatted. "
							+ ex.getMessage());
		}
	}
	
	public Topology readFromFile(String fileName) throws Exception {
		Topology topology = new Topology();
		readFile(topology, fileName);
		topology.clearOneWayConnections();
		return topology;
	}
	
	private void readFile(Topology topology, String topFileName) throws Exception {
		try {
			File top = new File(topFileName);
			if(!top.exists())
				throw new Exception("Unable to find the topology file: "+ topFileName);
			
			HashMap<Integer, Node> allNodes = new HashMap<Integer, Node>();
			
			BufferedReader in = new BufferedReader(new FileReader(top));
			String line;
			while ((line = in.readLine()) != null) {
				line = line.trim().toLowerCase();
				if (line.equals("node")){
					topology.addNode(processNode(in, allNodes));
				}else if(line.equals("edge")){
					processEdge(in, allNodes);
				}
			}
		} catch (Exception ex) {
			throw new Exception("Error reading topology file. Improperly formatted. "+ ex.getMessage());
		}
	}
	
	private Node processNode(BufferedReader in, HashMap<Integer, Node> allNodes)  throws Exception{
		DataSectionReader reader = new DataSectionReader();
		Node node = null;
		Integer id = -1;
		String line;
		while ((line = reader.readNextLine(in)) != null) {
			if(line.startsWith("label")){
				String[] locAndId = line.split("\"")[1].split(" ");
				node = new Node(Double.parseDouble(locAndId[0]), locAndId[1]);
			}else  if(line.startsWith("id")){
				id = Integer.parseInt(line.split(" ")[1]);
			}
		}
		allNodes.put(id, node);
		return node;
	}
	private void processEdge(BufferedReader in, HashMap<Integer, Node> allNodes) throws Exception{		
		DataSectionReader reader = new DataSectionReader();
		Integer sourceId = -1;
		Integer targetId = -1;
		String line;
		while ((line = reader.readNextLine(in)) != null) {
			if(line.startsWith("source")){
				sourceId = Integer.parseInt(line.split(" ")[1]);
			}else  if(line.startsWith("target")){
				targetId = Integer.parseInt(line.split(" ")[1]);
			}
		}
		
		Node sourceNode = allNodes.get(sourceId);
		Node targetNode = allNodes.get(targetId);
		sourceNode.addNeighbor(targetNode);
	}
}
