package au.com.cybersearch2.log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.MemoryHandler;

public class TestLogHandler extends MemoryHandler {

	private static LogRecordHandler logRecordHandler;
	
	public TestLogHandler() {
		super(logRecordHandler, 1, Level.FINEST);
	}

	public static LogRecordHandler getLogRecordHandler() {
		return logRecordHandlerInstance();
	}

	public static LogRecordHandler logRecordHandlerInstance() {
		if (logRecordHandler == null) {
			logRecordHandler = new LogRecordHandler();
    	    try (InputStream in = TestLogHandler.class.getResourceAsStream("/logging.properties")) {
				au.com.cybersearch2.classylog.LogManager.initialize(new BufferedInputStream(in));
			} catch (IOException e) {
    	    	throw new RuntimeException("Test logging config file logging.properties not found on classpath");
			}
		}
		logRecordHandler.clear();
		return logRecordHandler;
	}
}
