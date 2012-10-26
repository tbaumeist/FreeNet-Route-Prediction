package frp.dataFileReaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import frp.routing.Node;
import frp.routing.Topology;

class TopologyFileReaderDOT implements ITopologyFileReader {

	@Override
	public boolean canRead(String fileName) throws Exception {
		try {
			File top = new File(fileName);
			if (!top.exists())
				throw new Exception("Unable to find the topology file: "
						+ fileName);

			BufferedReader in = new BufferedReader(new FileReader(top));
			String line = in.readLine();
			in.close();
			return line.toLowerCase().startsWith("digraph");
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

	private void readFile(Topology topology, String topFileName)
			throws Exception {
		try {
			File top = new File(topFileName);
			if (!top.exists())
				throw new Exception("Unable to find the topology file: "
						+ topFileName);

			BufferedReader in = new BufferedReader(new FileReader(top));
			String line;
			while ((line = in.readLine()) != null) {
				if (!line.contains("->"))
					continue;

				line = line.trim();
				line = line.replace('\t', ' ');
				// line = line.replace('-', ' ');
				String[] parsed = line.split("\"");
				if (parsed.length != 4)
					continue;

				double locA = Double.parseDouble(parsed[1].split(" ")[0]);
				String idA = parsed[1].split(" ")[1];
				double locB = Double.parseDouble(parsed[3].split(" ")[0]);
				String idB = parsed[3].split(" ")[1];

				Node nodeA = topology.findNode(locA, idA);
				Node nodeB = topology.findNode(locB, idB);
				if (nodeA == null)
					nodeA = new Node(locA, idA);
				if (nodeB == null)
					nodeB = new Node(locB, idB);

				topology.addNode(nodeA);
				topology.addNode(nodeB);

				nodeA.addNeighbor(nodeB);
			}
			in.close();
		} catch (Exception ex) {
			throw new Exception(
					"Error reading topology file. Improperly formatted. "
							+ ex.getMessage());
		}
	}
}
