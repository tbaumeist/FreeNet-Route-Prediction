package frp.predModelEval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

public class CommLog {
	private Hashtable<String, KeyData> keys = new Hashtable<String, KeyData>();
	private KeyData lastKeyDataHit = null;

	public void addKey(String key, String messUID) {
		if (key == null || messUID == null)
			return;
		if (key.isEmpty() || messUID.isEmpty())
			return;
		if (!this.keys.containsKey(key))
			this.keys.put(key, new KeyData());

		KeyData data = this.keys.get(key);
		data.addMessageUID(messUID);
	}

	public void addMessage(String msgUID, String message) {
		if (msgUID == null || message == null)
			return;
		if (msgUID.isEmpty() || message.isEmpty())
			return;

		// check if last hit works
		if (this.lastKeyDataHit != null) {
			if (this.lastKeyDataHit.hasMessageUID(msgUID)) {
				this.lastKeyDataHit.addMessage(msgUID, message);
				return;
			}
		}

		// different key data has the message uid
		for (KeyData data : this.keys.values()) {
			if (data.hasMessageUID(msgUID)) {
				this.lastKeyDataHit = data; // cache this key data hit
				data.addMessage(msgUID, message);
				return;
			}
		}
	}

	public boolean hasKey(String key) {
		if (key == null || key.isEmpty())
			return false;
		return this.keys.containsKey(key.toLowerCase());
	}

	public boolean hasReject(String key) {
		if (key == null || key.isEmpty())
			return false;
		if (!this.keys.containsKey(key.toLowerCase()))
			return false;
		KeyData data = this.keys.get(key.toLowerCase());
		return data.hasRejectMessage();
	}

	@Override
	public String toString() {
		String s = "";
		for (String key : this.keys.keySet()) {
			s += key + "\n";
			s += this.keys.get(key).toString();
			s += "\n\n";
		}
		return s;
	}

	public String toStringPath(String key) {
		if (key == null || key.isEmpty())
			return "";
		if (!this.keys.containsKey(key.toLowerCase()))
			return "";
		return this.keys.get(key.toLowerCase()).toStringPath();
	}

	private class KeyData {
		private Hashtable<String, CommLogMessageSet> messUIDs = new Hashtable<String, CommLogMessageSet>();

		public boolean hasMessageUID(String msgUID) {
			return this.messUIDs.containsKey(msgUID);
		}

		public void addMessageUID(String msgUID) {
			if (msgUID == null || msgUID.isEmpty())
				return;
			if (!this.messUIDs.containsKey(msgUID))
				this.messUIDs.put(msgUID, new CommLogMessageSet());
		}

		public void addMessage(String msgUID, String message) {
			if (msgUID == null || message == null)
				return;
			if (msgUID.isEmpty() || message.isEmpty())
				return;
			this.messUIDs.get(msgUID).addMessage(message.toLowerCase());
		}

		public boolean hasRejectMessage() {
			for (CommLogMessageSet ms : this.messUIDs.values()) {
				if(ms.hasRejectMessage())
					return true;
			}
			return false;
		}

		@Override
		public String toString() {
			String s = "";
			for (String uids : this.messUIDs.keySet()) {
				s += this.messUIDs.get(uids).toString() + "\n";
			}
			return s;
		}

		public String toStringPath() {
			List<String> unquiePaths = new ArrayList<String>();
			
			List<CommLogMessageSet> logs = new ArrayList<CommLogMessageSet>();
			logs.addAll(this.messUIDs.values());
			Collections.sort(logs);
					
			for (CommLogMessageSet c : logs) {
				String p = c.getPath();
				if(!unquiePaths.contains(p))
					unquiePaths.add(p);
			}
			
			String s = "";
			for (String p : unquiePaths) {
				s += p + " | ";
			}
			return s;
		}
	}
}
