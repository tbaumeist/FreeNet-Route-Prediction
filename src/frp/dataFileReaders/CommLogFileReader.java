package frp.dataFileReaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import frp.main.insertModelEval.CommLog;

public class CommLogFileReader {
private String commFileName = "";
	
	public CommLogFileReader(String fileName){
		this.commFileName = fileName;
	}
	
	public CommLog readFile() throws Exception {
		CommLog log = new CommLog();
		readFromFile(log, this.commFileName);
		return log;
	}
	
	private void readFromFile(CommLog log, String fileName) throws Exception {
		try {
			File logFile = new File(fileName);
			if(!logFile.exists())
				throw new Exception("Unable to find the communication log file: "+ fileName);
			
			BufferedReader in = new BufferedReader(new FileReader(logFile));
			
			// pass one get all the unique keys
			String line;
			while ((line = in.readLine()) != null) {
				if (!line.contains("freenet.keys.nodechk"))
					continue;

				line = line.trim();
				line = line.replace('\t', ' ');
				String[] parsed = line.split(" ");
				if (parsed.length < 3)
					continue;

				// get key
				String key = "";
				for(String s : parsed){
					if(s.contains("freenet.keys.nodechk")){
						key = s.split("@")[2].split(":")[0];
						break;
					}
				}
				
				// get message uid
				String messUID = "";
				for(String s : parsed){
					if(s.contains("message_uid")){
						messUID = s.split(":")[1].replace(",", "");
						break;
					}
				}
				log.addKey(key, messUID);
			}
			in.close();
			
			
			// second pass, extract the messages
			BufferedReader in2 = new BufferedReader(new FileReader(logFile));
			while ((line = in2.readLine()) != null) {
				if (!line.contains("message_uid"))
					continue;

				line = line.trim();
				line = line.replace('\t', ' ');
				String[] parsed = line.split(" ");
				if (parsed.length < 3)
					continue;
				
				// get key
				String key = "";
				for(String s : parsed){
					if(s.contains("freenet.keys.nodechk")){
						key = "Key:"+s.split("@")[2].split(":")[0];
						break;
					}
				}
				
				// message uid
				String messUID = "";
				for(String s : parsed){
					if(s.contains("message_uid")){
						messUID = s.split(":")[1].replace(",", "");
						break;
					}
				}
				
				String message = parsed[0] +" ";
				for(int i = 2; i < parsed.length; i++){
					if(!parsed[i].contains("freenet.keys.nodechk"))
						message += parsed[i] + " ";
				}
				log.addMessage(messUID, message + key);
			}
		} catch (Exception ex) {
			throw new Exception("Error reading topology file. Improperly formatted. "+ ex.getMessage());
		}
	}
}
