package au.com.cybersearch2.log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.MemoryHandler;

public class TestLogHandler extends MemoryHandler {

	private static final String PROPERTIES_PATH = "/logging.properties";
	
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
			URL url = TestLogHandler.class.getResource(PROPERTIES_PATH);
			if (url == null)
				url = TestLogHandler.class.getResource("/target/test-classes" + PROPERTIES_PATH);
			if (url == null)
    	    	throw new RuntimeException("Test logging config file logging.properties not found on classpath");
    	    try (InputStream in = new FileInputStream(new File(url.toURI().getPath()))) {
				au.com.cybersearch2.classylog.LogManager.initialize(new BufferedInputStream(in));
			} catch (IOException e) {
    	    	throw new RuntimeException("Test logging config file logging.properties not found on classpath");
			} catch (URISyntaxException e1) {
			}
		}
		logRecordHandler.clear();
		return logRecordHandler;
	}
}
