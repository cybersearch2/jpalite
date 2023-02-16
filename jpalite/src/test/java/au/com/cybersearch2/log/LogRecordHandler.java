package au.com.cybersearch2.log;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LogRecordHandler extends Handler {

	private List<String> messages = new ArrayList<>();
	
	public List<String> getMessages() {
		return messages;
	}

	public boolean match(int index, String message) {
		if (message == null)
			return false;
		if (index >= messages.size())
			return false;
		return message.equals(messages.get(index));
	}

	public void printAll() {
		messages.forEach(message -> System.out.println(message));
	}
	
	public void clear() {
		messages.clear();
	}
	
	@Override
	public void publish(LogRecord record) {
		messages.add(record.getMessage());
		//System.out.println(record.getMessage());
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() throws SecurityException {
	}

}
