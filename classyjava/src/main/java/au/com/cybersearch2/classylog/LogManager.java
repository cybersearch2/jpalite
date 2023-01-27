/** Copyright 2023 Andrew J Bowley

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. */
package au.com.cybersearch2.classylog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Properties;

import org.xmlpull.v1.XmlPullParserException;

import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.LocalLogBackend;
import com.j256.ormlite.logger.LogBackend;
import com.j256.ormlite.logger.LogBackendType;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;

import au.com.cybersearch2.classyjpa.global.Singleton;

/**
 * Configures {@link java.util.logging.LogManager}. This has a single instance that needs to
 * be configured by the time the first logger is created. It works with the j256 Simple Logger
 * to allow any third party logging library backend to be employed.  The default is to use
 * Java Util logging with an XML configuration format adapted from that of Log4j.
 * 
 * Initialization with a properties file is supported to facilitate testing. @see au.com.cybersearch2.log.TestLogHandler
 */
public class LogManager {

	private static final String DEFAULT_LOG_CONFIG_FILENAME = "/javalog.xml";

	private static final String XML_PARSE_ERROR = "Error \"%s\"while parsing Logging config file %s";

	private final LogBackendType logBackendType;
	private boolean isInitialized;

	/**
	 * Default constructor analyzes environment to determine which backend to use
	 */
	public LogManager() {
		logBackendType = findLogBackendType();
	}

	/** 
	 * Creates and returns logger for given class 
	 * @param clazz Class to log
	 */
	public static Logger getLogger(Class<?> clazz) {
		return getSingleton().createLogBackend(clazz);
	}

	/**
	 * Perform one-time initialization. A strategy to configure logging is employed if
	 * the Java Util logging backend is selected.
	 */
	public static void initialize() {
		getSingleton().initialzeLogging();
	}

	/**
	 * Perform one-time initialization of Java Util logging using piped properties configuration file
	 * @param in IOStream input from properties file
	 * @throws IOException
	 */
	public static void initialize(InputStream in) throws IOException {
		getSingleton().initialzeLogging(in);
	}
	
	private void initialzeLogging(InputStream in) throws IOException {
		if (isInitialized)
			return;
		java.util.logging.LogManager javaLogManager = java.util.logging.LogManager.getLogManager();
		javaLogManager.reset();
		javaLogManager.updateConfiguration(new BufferedInputStream(in), null);
		isInitialized = true;
	}

	private static LogManager getSingleton() {
		return (LogManager)Singleton.log_manager.getObject();
	}
	
	/**
	 * Return a logger associated with a particular class
	 */
	private Logger createLogBackend(Class<?> clazz) {
		if (!isInitialized)
			initialzeLogging();
		return new Logger(logBackendType.createLogBackend(clazz.getName()));
	}
	
	private synchronized void initialzeLogging() {
		if (isInitialized)
			return;
		if (logBackendType == LogBackendType.JAVA_UTIL)
			try {
			     if (!initialzeJavaUtilLogging())
			    	 System.out.println("Java util Logger configuration not completed successfully");
			} catch (IOException e) {
				e.printStackTrace();
			}
		isInitialized = true;
	}

	private boolean initialzeJavaUtilLogging() throws IOException {
		String taqLoggingConfigPath = null;
		try {
			taqLoggingConfigPath = getConfigurationPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		boolean done = false;
		if (taqLoggingConfigPath != null) {
			File loggingFile = new File(taqLoggingConfigPath);
			if (loggingFile.exists()) {
				try (InputStream is = new FileInputStream(loggingFile)) {
					readLogConfig(is, taqLoggingConfigPath);
					done = true;
				} catch (FileNotFoundException e) {
					// This should not happen as the file is supposed to exist
					System.err.println(String.format("Logging config file %s not found", taqLoggingConfigPath));
				} catch (XmlPullParserException e) {
					System.err.println(String.format(XML_PARSE_ERROR, e.getMessage(),  taqLoggingConfigPath));
				}
			}
		}
		if (!done) {
			InputStream is = this.getClass().getResourceAsStream(DEFAULT_LOG_CONFIG_FILENAME);
			if (is != null) { 
				try {
				    done = readLogConfig(is, taqLoggingConfigPath);
				} catch (XmlPullParserException e) {
					System.err.println(String.format(XML_PARSE_ERROR, e.getMessage(),  DEFAULT_LOG_CONFIG_FILENAME));
				} finally {
					is.close();
				}
			}
		}
		return done;
	}

    /**
     * Read Log4j adapted XML file and convert to properties to configure Java Util logging,
     * Return flag set true if operation is successful.
     * @param is Input stream pipe to XML file
     * @param loggingConfigPath
     * @return boolean
     * @throws XmlPullParserException
     * @throws IOException
     */
    private boolean readLogConfig(InputStream is, String loggingConfigPath) throws XmlPullParserException, IOException {
    	boolean ok = false;
		XmlConfiguration xmlConfiguration = new XmlConfiguration();
		Properties props = xmlConfiguration.parseXmlConfiguration(is);
		String propertiesPath;
		int pos = loggingConfigPath.lastIndexOf('.');
		if (pos > 0)
			propertiesPath = loggingConfigPath.substring(0, pos + 1) + "properties";
		else
			propertiesPath = loggingConfigPath + ".properties";
        try (OutputStream out = new FileOutputStream(propertiesPath)) {
        	props.store(out, "Generated Logging configuration - DO NOT EDIT");
        	try (final InputStream in = new FileInputStream(propertiesPath)) {
        		java.util.logging.LogManager javaLogManager = java.util.logging.LogManager.getLogManager();
				javaLogManager.reset();
				javaLogManager.updateConfiguration(new BufferedInputStream(in), null);
				ok = true;
        	}
        }
        return ok;
	}

	String getConfigurationPath() throws IOException {
        String fname = System.getProperty("jpalite.logging.config.file");
        if (fname == null) {
            fname = System.getProperty("user.home");
            if (fname == null)
                throw new Error("Can't find user.home ??");
            fname = Paths.get(fname, ".jpalite", "logging.xml")
                    .toAbsolutePath().normalize().toString();
        }
        return fname;
    }

	/**
	 * Return the most appropriate log backend type. Defaults to Java util logging.
	 */
	private LogBackendType findLogBackendType() {

		// See if the log-type was specified as a system property
		String logTypeString = System.getProperty(LoggerFactory.LOG_TYPE_SYSTEM_PROPERTY);
		if (logTypeString != null) {
			try {
				return LogBackendType.valueOf(logTypeString);
			} catch (IllegalArgumentException e) {
				LogBackend backend = new LocalLogBackend(LoggerFactory.class.getName());
				backend.log(Level.WARNING, "Could not find valid log-type from system property '"
						+ LoggerFactory.LOG_TYPE_SYSTEM_PROPERTY + "', value '" + logTypeString + "'");
			}
		}

		for (LogBackendType logType : LogBackendType.values()) {
			// Commons logging is a dependency of BeanUtils, so must be excluded.
			// use LOG_TYPE_SYSTEM_PROPERTY system property if CommonsLogging is required
			if (logType.isAvailable()  && (logType != LogBackendType.COMMONS_LOGGING)) {
				return logType == LogBackendType.LOCAL ? LogBackendType.JAVA_UTIL : logType;
			}
		}
		// Fall back is always JAVA_UTIL
		return LogBackendType.JAVA_UTIL;
	}

}
