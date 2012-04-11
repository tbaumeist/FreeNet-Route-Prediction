package frp.dataFileReaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import frp.main.insertModelEval.StoredWordData;

public class WordMapFileReader {
	private File dataFile;
	private File wordFile;
	public WordMapFileReader(String fileName, String dataWordFileName) throws Exception{
		dataFile = new File(fileName);
		wordFile = new File(dataWordFileName);
		if(!dataFile.exists())
			throw new Exception("Unable to find the word map data file.");
		if(!wordFile.exists())
			throw new Exception("Unable to find the word meta data file.");
	}
	
	public List<StoredWordData> readData(PrintStream writer) throws Exception{
		List<WordOriginPair> words = new ArrayList<WordOriginPair>();
		List<WordOriginPair> removeDuplicates = new ArrayList<WordOriginPair>();
		BufferedReader wordReader = new BufferedReader(new FileReader(this.wordFile));
		
		String wordLine = "";
		while((wordLine = wordReader.readLine()) != null){
			String[] parsed = wordLine.split(":");
			if(parsed.length < 4)
				continue;
			String key = parsed[1].trim();
			key = key.split("@")[1].split(",")[0];
			String htl = "1"; // default to 1
			if(parsed.length >= 5)
				htl = parsed[4].trim();
			WordOriginPair w = new WordOriginPair(parsed[0].trim(), key, parsed[3].trim(), parsed[2].trim(), htl);
			if(!words.contains(w))
				words.add(w);
			else
				removeDuplicates.add(w);
		}
		words.removeAll(removeDuplicates);
		if(!removeDuplicates.isEmpty()){
			writer.println("Removed duplicate entries:");
			for(WordOriginPair w : removeDuplicates)
				writer.println("\t"+w.getWord());
		}
		
		// read other data file
		Hashtable<String, List<String>> storedWords = new Hashtable<String, List<String>>();
		
		BufferedReader reader = new BufferedReader(new FileReader(this.dataFile));
		String line = "";
		while((line = reader.readLine()) != null){
			line = line.replace("192.168.0.1", "").replace("\t", " ");
			String[] parsed = line.split(":");
			if(parsed.length < 4)
				continue;
			String key = parsed[1].split("@")[2].trim();
			String nodeId = parsed[3].trim();
			if(!storedWords.containsKey(key))
				storedWords.put(key, new ArrayList<String>());
			storedWords.get(key).add(nodeId);
		}
		
		List<StoredWordData> datas = new ArrayList<StoredWordData>();
		
		for(Map.Entry<String, List<String>> entry : storedWords.entrySet()){
			WordOriginPair origin = findWordOriginPair(entry.getKey(), words);
			if(origin == null)
				continue;
			datas.add(new StoredWordData(origin.getOrigin(), origin.getLocation(), origin.getWord(), origin.getKey(), origin.getHtl(), entry.getValue()));
		}
		
		return datas;
	}
	
	private WordOriginPair findWordOriginPair(String word, List<WordOriginPair> list){
		for(WordOriginPair w : list){
			if(w.getKey().equals(word))
				return w;
		}
		return null;
	}
	
	class WordOriginPair{
		private String word, origin, key, location, htl;
		public WordOriginPair(String w, String k, String o, String l, String htl){
			this.word = w;
			this.key = k;
			this.origin = o.replace("192.168.0.1", "");
			this.location = l;
			this.htl = htl;
		}
		public String getWord(){
			return this.word;
		}
		public int getHtl(){
			if(this.htl.isEmpty())
				return 1;
			return Integer.parseInt(this.htl);
		}
		public String getOrigin(){
			return this.origin;
		}
		public String getKey(){
			return this.key;
		}
		public String getLocation(){
			return this.location;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (this == obj)
				return true;

			if (!(obj instanceof WordOriginPair))
				return false;
			WordOriginPair node = (WordOriginPair) obj;
			return node.getWord().equals(getWord());
		}

		@Override
		public int hashCode() {
			return getWord().hashCode();
		}
	}
	
}

