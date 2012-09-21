package frp.dataFileReaders;

import frp.routing.Topology;

public interface ITopologyFileReader {
	boolean canRead(String fileName)  throws Exception;
	Topology readFromFile(String fileName) throws Exception;
}
