package frp.main.insertModelEval;

import java.util.ArrayList;
import java.util.List;

public class CommLogMessageSet implements Comparable<CommLogMessageSet>{
	private List<String> messages = new ArrayList<String>();
	private String messagePath = null;
	private boolean hasRejectCache;
	private boolean hasRejectCache_IsSet = false;
	private int biggestHTLCache = -1;

	public void addMessage(String msg) {
		this.messages.add(msg);
	}

	public boolean hasRejectMessage() {
		if (this.hasRejectCache_IsSet)
			return this.hasRejectCache;

		for (String s : this.messages) {
			if (s.contains("rejectoverload")) {
				this.hasRejectCache = true;
				this.hasRejectCache_IsSet = true;
				return this.hasRejectCache;
			}
		}
		this.hasRejectCache = false;
		this.hasRejectCache_IsSet = true;
		return this.hasRejectCache;
	}

	@Override
	public String toString() {
		String s = "";
		for (String m : this.messages) {
			s += m + "\n";
		}
		return s;
	}
	
	@Override
	public int compareTo(CommLogMessageSet o) {
		if( o == null )
			return 1;
		if(this.getBiggestHTL() > o.getBiggestHTL())
			return 1;
		if(this.getBiggestHTL() < o.getBiggestHTL())
			return -1;
		return 0;
	}

	public String getPath() {
		if (this.messagePath != null)
			return this.messagePath;
		this.messagePath = interpretePath(this.messages);
		return this.messagePath;
	}
	
	private int getBiggestHTL(){
		return this.biggestHTLCache;
	}

	/*
	 * Possible message patterns
	 * 
	 * 1 **************** to * Key:?????????????????? *** fnpaccepted ****
	 * 
	 * 2 **************** to * Key:?????????????????? *** fnprejectloop ****
	 * 
	 * 3 **************** to * Key:?????????????????? *** rejectoverload ****
	 */
	private String interpretePath(List<String> messages) {
		try {
			List<MessageTransResponse> trans = new ArrayList<MessageTransResponse>();

			// collect all of the messages sent and their response
			MessageTransResponse currentTran = null;
			for (String s : this.messages) {

				// process response
				if (currentTran != null) {
					if (s.contains("fnpaccepted"))
						currentTran.setResponse(RESPONSE.ACCEPT);
					else if (s.contains("fnprejectloop"))
						currentTran.setResponse(RESPONSE.REJLOOP);
					else if (s.contains("rejectoverload"))
						currentTran.setResponse(RESPONSE.REJOVER);
					else
						return "Error processing path!!";

					trans.add(currentTran);
					currentTran = null;
				}

				// new message sent
				if (s.contains(" to ") && s.contains(" key:")) {
					String line = s.trim();
					line = line.replace('\t', ' ');
					String[] parsed = line.split(" ");
					int htl = Integer.parseInt(parsed[1].split(":")[1].replace(",", ""));
					currentTran = new MessageTransResponse(parsed[0],
							parsed[6].split(":")[0], htl);
					
					if(htl > this.biggestHTLCache)
						this.biggestHTLCache = htl;
				}
			}
			
			// we should have all of the messages now
			// lets put them in order
			orderMessages(trans);
			
			String s = "";
			for(MessageTransResponse tran : trans){
				s += tran.toString() + " ";
			}
			return s;
			
			// check for path consistency
		} catch (Exception ex) {
			return "Error interpreting path!!";
		}
	}
	
	private void orderMessages(List<MessageTransResponse> trans){
		MessageTransResponse prev = null;
		for(int i = 0; i < trans.size(); i++){
			int first = i;
			MessageTransResponse firstTran = trans.get(i);
			for(int j = i +1; j < trans.size(); j++){
				// use htl
				if(trans.get(j).htl > firstTran.htl){
					firstTran = trans.get(j);
					first = j;
				}
				// look at prev
				else if(prev != null && trans.get(j).getFrom().equals(prev.getTo()) && 
						!(firstTran.getFrom().equals(prev.getTo()) && firstTran.getResponse() != RESPONSE.ACCEPT)){
					firstTran = trans.get(j);
					first = j;
				}
				// just compare
				else if(trans.get(j).getResponse() == RESPONSE.ACCEPT && trans.get(j).htl == firstTran.htl && trans.get(j).getTo().equals(firstTran.getFrom())){
					firstTran = trans.get(j);
					first = j;
					j = i; // start search again
				}
			}
			
			if(first != i){
				trans.set(first, trans.get(i));
				trans.set(i, firstTran);
			}
			if(trans.get(i).getResponse() == RESPONSE.ACCEPT)
				prev = trans.get(i);
		}
	}

	public enum RESPONSE {
		ACCEPT, REJLOOP, REJOVER, ERROR
	};

	private class MessageTransResponse {
		private String from, to;
		private int htl;
		private RESPONSE response;

		public MessageTransResponse(String f, String t, int htl) {
			this.from = f.replace("192.168.0.1", "");
			this.to = t.replace("192.168.0.1", "");
			this.htl = htl;
		}

		public void setResponse(RESPONSE r) {
			this.response = r;
		}
		
		public String getTo(){ return this.to; }
		public String getFrom(){ return this.from; }
		public RESPONSE getResponse(){ return this.response;}
		
		@Override
		public String toString() {
			if(getResponse() == RESPONSE.ACCEPT)
				return getFrom() +"->("+this.htl+")"+getTo();
			return getFrom() +"<->("+this.htl+")"+getTo();
		}
	}
}
