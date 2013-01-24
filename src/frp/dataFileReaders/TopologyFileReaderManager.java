package frp.dataFileReaders;

import frp.routing.Topology;

public class TopologyFileReaderManager {
    private ITopologyFileReader topReader[];

    public TopologyFileReaderManager() {
        topReader = new ITopologyFileReader[2];
        topReader[0] = new TopologyFileReaderDOT();
        topReader[1] = new TopologyFileReaderGML();
    }

    public Topology readFromFile(String fileName) throws Exception {
        for (ITopologyFileReader reader : topReader) {
            if (reader.canRead(fileName))
                return reader.readFromFile(fileName);
        }
        return null;
    }
}
