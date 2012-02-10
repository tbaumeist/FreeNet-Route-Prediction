package frp.predModelEval;

import java.util.ArrayList;
import java.util.List;

public class StoredWordData {
	private String location, word, originNode, key;
	private List<String> nodes = new ArrayList<String>();
	private int htl;

	public StoredWordData(String origin, String loc, String word, String key, int htl,
			List<String> nodes) {
		this.originNode = origin;
		this.location = loc;
		this.word = word;
		this.key = key;
		this.htl = htl;
		this.nodes.addAll(nodes);
	}

	public String getWord() {
		return this.word;
	}
	
	public String getKey(){
		return this.key;
	}

	public String getOriginNode() {
		return this.originNode;
	}

	public String getLocation() {
		return this.location;
	}

	public List<String> getActualStorageNodes() {
		return this.nodes;
	}

	public int getHtl() {
		return this.htl;
	}

	public String getActualStorageNodesToString() {
		String s = "";
		for (String n : this.nodes) {
			s += n + " ";
		}
		return s;
	}

	@Override
	public String toString() {
		String s = this.location + " Org:" + this.originNode + " Word:"
				+ this.word;
		s += " " + getActualStorageNodesToString();
		return s;
	}
}

